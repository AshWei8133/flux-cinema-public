<!-- src/features/Members/views/MemberCoupons.vue -->
<template>
  <section class="page" v-loading="store.loading">
    <!-- 工具列 -->
    <div class="toolbar">
      <el-segmented
        class="segmented"
        v-model="store.status"
        :options="['ALL', '未使用', '已使用']"
        @change="store.reload"
      />
      <input
        v-model.trim="store.keyword"
        class="ipt"
        type="search"
        placeholder="輸入名稱或描述…"
        @keyup.enter="store.reload"
      />
      <button class="btn" :disabled="store.loading" @click="store.reload">查詢</button>
      <button class="btn reset" :disabled="store.loading" @click="resetFilters">重置</button>
    </div>

    <!-- 狀態 -->
    <div v-if="!store.loading && mappedList.length === 0" class="state">尚未有可用的優惠券</div>

    <!-- 卡片網格 -->
    <div v-else class="grid">
      <article class="card" v-for="c in mappedList" :key="c.id" :id="`mc-${c.id}`">
        <div class="thumb">
          <img
            :src="c.imageUrl || placeholder"
            :alt="c.title"
            loading="lazy"
            decoding="async"
            @error="$event.target.src = placeholder"
          />
        </div>

        <div class="body">
          <h3 class="title">{{ c.title }}</h3>

          <div class="meta">
            <!-- ✅ 用新的折扣標籤 -->
            <span class="chip" :class="c.discountTag.typeClass">{{ c.discountTag.text }}</span>
            <span v-if="c.minimumSpend" class="chip">低消 ${{ c.minimumSpend }}</span>
            <span class="chip" :class="c.status === '未使用' ? 'ok' : 'dim'">{{ c.status }}</span>
          </div>

          <!-- 序號 + 複製 -->
          <p class="serial" v-if="c.serialNumber">
            <span class="label">序號：</span>
            <code class="sn">{{ c.serialNumber }}</code>
            <el-tooltip content="複製序號" placement="top">
              <el-icon class="copy" @click="copy(c.serialNumber)"><CopyDocument /></el-icon>
            </el-tooltip>
          </p>

          <p class="excerpt" v-if="c.excerpt">{{ c.excerpt }}</p>

          <div class="actions">
            <button class="btn action" :disabled="c.status !== '未使用'" @click="goCart(c.raw)">
              前往購物
            </button>
            <a
              href="#"
              @click.prevent="scrollToBooking"
              class="btn warn"
              :disabled="c.status !== '未使用'"
              >前往訂票</a
            >
          </div>
        </div>
      </article>
    </div>

    <!-- 分頁 -->
    <div class="pager" v-if="store.total > store.size">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="store.total"
        :page-size="store.size"
        v-model:current-page="store.page"
        @current-change="store.reload"
      />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { useMemberCouponListStore } from '../../store/usePublicMemberCouponListStore'
import httpClient from '@/services/api'
import { useUiStore } from '@/stores/uiStore'

const route = useRoute()
const router = useRouter()
const store = useMemberCouponListStore()
const uiStore = useUiStore()

// ====== 工具 ======
const textOnly = (html) =>
  String(html || '')
    .replace(/<[^>]+>/g, '')
    .replace(/\s+/g, ' ')
    .trim()

const absUrl = (u) => {
  if (!u) return ''
  if (/^https?:\/\//i.test(u)) return u
  if (u.startsWith('//')) return `${location.protocol}${u}`
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  const rel = u.startsWith('/') ? u : `/${u}`
  return `${base}${rel}`
}

/** 圖片策略：base64 > 欄位 URL > 備援 API */
const finalImageUrl = (memberCouponId, base64Str, fieldUrl) => {
  if (base64Str) return `data:image/*;base64,${base64Str}`
  if (fieldUrl) {
    const abs = absUrl(String(fieldUrl))
    return `${abs}?v=${Date.now()}`
  }
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  return `${base}/member-coupons/${memberCouponId}/image?v=${Date.now()}`
}

/** 折扣標籤：與你在前台公共券一致的規則 */
function computeDiscountTag(row) {
  const rawType = String(row.discountType || row.type || '').toUpperCase()
  const amt = Number(row.discountAmount ?? row.discount_amount ?? row.amount ?? row.value)
  const pct = Number(row.discountPercent ?? row.percent ?? row.discountRate ?? row.rate)
  const minSpend = Number(row.minimumSpend ?? row.minimum_spend)
  const cat = String(row.couponCategory || row.coupon_category || '')
  const text = `${row.couponName || ''} ${row.couponDescription || ''}`

  // 文案線索
  const hasPercentText = /(\d+(\.\d+)?)\s*折|折扣|%|％/.test(text)
  const hasFixedText = /折抵|滿|現折|減|元|折百/.test(text)

  // 判斷類型（優先度：rawType > 百分比欄位 > 文案百分比 > 固定金額/文案固定 > 類別）
  let type = rawType
  if (!type) {
    if (Number.isFinite(pct) && pct > 0) type = 'PERCENTAGE'
    else if (hasPercentText) type = 'PERCENTAGE'
    else if (hasFixedText || (Number.isFinite(minSpend) && Number.isFinite(amt) && amt > 0))
      type = 'FIXED'
    else if (cat.includes('折價')) type = 'FIXED'
  }

  // 轉百分比：支援 0.8 / 8 / 95 三種寫法 → 80/80/95
  const toPercent = (x) => (x <= 1 ? x * 100 : x <= 10 ? x * 10 : x <= 100 ? x : NaN)

  // 輸出
  if (type === 'FIXED' && Number.isFinite(amt) && amt > 0) {
    return { type: 'warning', typeClass: 'warn', text: `折抵 ${Math.abs(amt)} 元` }
  }
  if (type === 'PERCENTAGE') {
    let payPercent = NaN
    if (Number.isFinite(pct) && pct > 0) payPercent = toPercent(pct)
    else if (hasPercentText && Number.isFinite(amt) && amt > 0) payPercent = toPercent(amt)

    if (Number.isFinite(payPercent)) {
      const pretty = String(Math.round(payPercent * 10) / 10).replace(/\.0$/, '')
      return { type: 'success', typeClass: 'success', text: `折扣 ${pretty}％` } // ✅ 不再顯示 9.5 折
    }
  }
  return { type: 'info', typeClass: 'dim', text: '基準票價' }
}

/** 將 store.list 映射為卡片資料（加入 discountTag） */
const mappedList = computed(() =>
  (store.list || []).map((r) => {
    const imgField =
      r.couponImageUrl ||
      r.imageUrl ||
      r.coverUrl ||
      r.bannerUrl ||
      r.thumbnailUrl ||
      r.image ||
      r.cover ||
      r.banner ||
      r.image_path ||
      r.imagePath ||
      ''

    const discountTag = computeDiscountTag(r)

    return {
      id: String(r.memberCouponId ?? r.id ?? r.member_coupon_id),
      title: r.couponName ?? r.name ?? '—',
      status: r.status ?? '—',
      minimumSpend: r.minimumSpend ?? null,
      excerpt: textOnly(r.couponDescription ?? r.description ?? ''),
      imageUrl: finalImageUrl(r.memberCouponId, r.couponImageBase64, imgField),
      serialNumber: r.serialNumber ?? r.serial_number ?? '',
      discountTag, // ✅ 新增
      raw: r,
    }
  }),
)

async function copy(text) {
  if (!text) {
    ElMessage.warning('沒有可複製的序號')
    return
  }
  try {
    await navigator.clipboard.writeText(String(text))
    ElMessage.success('序號已複製')
  } catch {
    const ta = document.createElement('textarea')
    ta.value = String(text)
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success('序號已複製')
  }
}

// 導頁
function goCart(c) {
  router.push({ name: 'Cart', query: { memberCouponId: c.memberCouponId } })
}
function goBooking(c) {
  const sessionId = c.sessionId
  router.push({
    name: 'SeatSelection',
    params: { sessionId },
    query: { memberCouponId: c.memberCouponId },
  })
}

async function scrollToBooking() {
  if (route.name !== 'Home') {
    await router.push({ name: 'Home' })
  }
  uiStore.setScrollTarget('#booking-section')
}

// 重置
function resetFilters() {
  store.keyword = ''
  store.status = 'ALL'
  store.reload()
}

// lifecycle
onMounted(async () => {
  try {
    await store.reload()
  } catch (e) {
    ElMessage.error('載入我的優惠券失敗，請稍後再試')
    return
  }

  // 領券導流提示
  const from = route.query.from
  const cid = Number(route.query.couponId)
  if (from === 'claim' && cid) {
    await nextTick()
    const just = store.list.find((x) => x.couponId === cid)
    if (just) {
      ElMessage.success(`已領取：${just.couponName}，可直接使用`)
    }
  }
})
</script>

<style scoped>
.page {
  color: #fff;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 工具列 */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.segmented {
  background-color: silver;
  color: black;
}
.ipt {
  background: #111;
  color: #eee;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 8px 10px;
  min-width: 240px;
}
.btn {
  padding: 8px 14px;
  border: 1px solid #333;
  border-radius: 8px;
  background: #1f1f1f;
  color: #eee;
  cursor: pointer;
}
.btn.reset {
  background: #333;
  color: #ff8a8a;
}

/* 卡片網格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}
.card {
  background: #1f1f1f;
  border: 1px solid #333;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}
.card:hover {
  transform: translateY(-2px);
  transition: transform 0.15s ease;
}
.thumb {
  height: 260px;
  background: #0d0d0d;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb img {
  width: 95%;
  height: 95%;
  object-fit: cover;
  display: block;
}
.body {
  padding: 12px;
}
.title {
  margin: 0 0 6px;
  font-size: 16px;
  line-height: 1.4;
}
.meta {
  color: #9aa0a6;
  font-size: 12px;
  margin-bottom: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  color: #cfcfcf;
  border-radius: 999px;
  padding: 3px 8px;
}
.chip.ok {
  color: #9be59b;
  border-color: #2f4f2f;
}
.chip.dim {
  color: #9aa0a6;
}

/* ✅ 折扣色系：百分比(success) / 固定金額(warn) */
.chip.success {
  color: #a8f0b6;
  border-color: #2e5b3a;
}
.chip.warn {
  color: #ffd29a;
  border-color: #6b4e2a;
}

.excerpt {
  color: #cfcfcf;
  font-size: 14px;
  line-height: 1.6;
  min-height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 序號樣式 */
.serial {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 6px 0 10px;
  color: #f6d28b;
}
.serial .label {
  color: #f6d28b;
}
.serial .sn {
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
  color: #ffe9b6;
}
.serial .copy {
  cursor: pointer;
}
.serial .copy:hover {
  opacity: 0.9;
}

/* 行為按鈕 */
.actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.btn.action {
  font-size: 16px;
  background: #595757;
}
.btn.warn {
  background: #493324;
}
.btn.reset:hover {
  background: #444;
}
a.btn {
  text-decoration: none; /* 移除底線 */
  color: inherit; /* 跟隨按鈕樣式的文字顏色 */
}

/* 空狀態 / 分頁 */
.state {
  color: #9aa0a6;
  text-align: center;
  padding: 24px 8px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
