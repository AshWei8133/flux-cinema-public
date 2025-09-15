<template>
  <div class="overview-container">
    <h2>總覽與確認</h2>

    <div class="summary-card">
      <h3>優惠券詳細資訊</h3>

      <div class="info-item">
        <strong>活動：</strong>
        <span>{{ eventName }}</span>
      </div>

      <div class="info-item">
        <strong>類型：</strong>
        <span>{{ couponCategoryNameText }}</span>
      </div>

      <div class="info-item">
        <strong>名稱：</strong>
        <span>{{ fd.couponName || '—' }}</span>
      </div>

      <div class="info-item">
        <strong>描述：</strong>
        <span>{{ fd.couponDescription || '—' }}</span>
      </div>

      <div class="info-item">
        <strong>折扣內容：</strong>
        <span>{{ discountText }}</span>
      </div>

      <div class="info-item">
        <strong>最低消費金額：</strong>
        <span>{{ minimumSpendText }}</span>
      </div>

      <!-- 可編輯：數量 -->
      <div class="info-item editable">
        <strong>數量：</strong>
        <el-input-number
          v-model="fd.quantity"
          :min="1"
          :max="10000"
          :step="1"
          controls-position="right"
          size="small"
          @change="touch()"
        />
        <span class="hint">(至少 1，最多 99,999)</span>
      </div>

      <!-- 可編輯：每人可兌換次數 -->
      <div class="info-item editable">
        <strong>每人可兌換次數：</strong>
        <el-input-number
          v-model="fd.redeemableTimes"
          :min="1"
          :max="99"
          :step="1"
          controls-position="right"
          size="small"
          @change="touch()"
        />
        <span class="hint">(1~99)</span>
      </div>

      <!-- 可編輯：狀態 -->
      <div class="info-item editable">
        <strong>狀態：</strong>
        <el-switch
          v-model="fd.status"
          :active-value="'ACTIVE'"
          :inactive-value="'INACTIVE'"
          active-text="啟用"
          inactive-text="停用"
          @change="touch()"
        />
        <span class="status-chip" :class="statusTextClass">{{ statusText }}</span>
      </div>

      <div class="info-item">
        <strong>條件：</strong>
        <ul>
          <li v-if="fd.movieId">電影：{{ movieTitleText }}</li>
          <li v-if="fd.memberLevelId">會員等級：{{ memberLevelNameText }}</li>
          <li v-if="fd.sessionId">場次：{{ sessionNameText }}</li>
          <li v-if="!hasConditions">無額外條件</li>
        </ul>
      </div>

      <div class="info-item image-preview">
        <strong>圖片預覽：</strong>
        <img v-if="imageSrc" :src="imageSrc" alt="優惠券圖片" class="coupon-image" />
        <span v-else>無圖片</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import httpClient from '@/services/api'

/* props / emits */
const props = defineProps({ formData: { type: Object, required: true } })
const emit = defineEmits(['prev', 'issue-success'])

/* ✅ 安全包裝：永遠給 template 一個物件可讀，避免 undefined 崩潰 */
const fd = computed(() => props.formData ?? {})

/* 狀態 */
const isIssuing = ref(false)
const dirty = ref(false)
const touch = () => (dirty.value = true)

/* 文案 */
const eventName = computed(() => fd.value?.event?.title || fd.value?.eventTitle || '未選擇')

const couponCategoryNameText = computed(
  () =>
    fd.value?.couponCategoryName ||
    fd.value?.couponCategory?.couponCategoryName ||
    (fd.value?.couponCategoryId ? `#${fd.value.couponCategoryId}` : '未選擇'),
)

/* 條件名稱（先吃父層傳來的名稱欄位；沒有就 fallback） */
const movieTitleText = computed(
  () =>
    fd.value?.movieTitle ||
    fd.value?.movie?.titleLocal ||
    fd.value?.movie?.title ||
    (fd.value?.movieId ? `#${fd.value.movieId}` : '未選擇'),
)

const memberLevelNameText = computed(
  () =>
    fd.value?.memberLevelName ||
    fd.value?.memberLevel?.levelName ||
    (fd.value?.memberLevelId ? `#${fd.value.memberLevelId}` : '未選擇'),
)

const sessionNameText = computed(() => {
  const f = fd.value || {}
  if (f.sessionLabel) return f.sessionLabel
  const time =
    f.sessionTime ||
    f.session?.sessionTime ||
    (f.session?.startAt ? String(f.session.startAt).slice(11, 16) : '')
  if (time) return `${movieTitleText.value} - ${time}`
  return f.session?.sessionName || (f.sessionId ? `#${f.sessionId}` : '未選擇')
})

const hasConditions = computed(
  () => !!(fd.value?.movieId || fd.value?.memberLevelId || fd.value?.sessionId),
)

/* 折扣、最低消費、狀態 */
const discountText = computed(() => {
  const t = fd.value?.discountType
  const v = fd.value?.discountAmount
  if (t === 'PERCENTAGE') return v ? `折扣 ${v}%` : '未設定'
  if (t === 'FIXED') return v ? `折抵 $${v}` : '未設定'
  return '未設定'
})

const minimumSpendText = computed(() =>
  fd.value?.minimumSpend ? `$${fd.value.minimumSpend}` : '無',
)

const statusText = computed(() => ((fd.value?.status || 'ACTIVE') === 'ACTIVE' ? '啟用' : '停用'))
const statusTextClass = computed(() =>
  (fd.value?.status || 'ACTIVE') === 'ACTIVE' ? 'is-active' : 'is-inactive',
)

/* 圖片處理（支援 base64 / dataURL / HEX / bytes array） */
const hexToUint8 = (hex) => {
  const clean = String(hex).trim().replace(/^0x/i, '').replace(/\s+/g, '')
  if (!clean) return new Uint8Array()
  if (clean.length % 2 !== 0) throw new Error('HEX 長度需為偶數')
  const out = new Uint8Array(clean.length / 2)
  for (let i = 0; i < clean.length; i += 2) out[i / 2] = parseInt(clean.slice(i, i + 2), 16)
  return out
}
const b64ToUint8 = (b64) => {
  let clean = String(b64)
    .trim()
    .replace(/^data:[^,]+,/, '')
    .replace(/\s+/g, '')
  clean = clean.replace(/-/g, '+').replace(/_/g, '/')
  const pad = clean.length % 4
  if (pad) clean += '='.repeat(4 - pad)
  const bin = atob(clean)
  const out = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i)
  return out
}
const uint8ToBase64 = (u8) => {
  let s = ''
  for (let i = 0; i < u8.length; i++) s += String.fromCharCode(u8[i])
  return btoa(s)
}
const sniffMime = (bytes) => {
  const isPNG =
    bytes.length >= 8 &&
    bytes[0] === 0x89 &&
    bytes[1] === 0x50 &&
    bytes[2] === 0x4e &&
    bytes[3] === 0x47 &&
    bytes[4] === 0x0d &&
    bytes[5] === 0x0a &&
    bytes[6] === 0x1a &&
    bytes[7] === 0x0a
  if (isPNG) return 'image/png'
  const isJPG = bytes.length >= 2 && bytes[0] === 0xff && bytes[1] === 0xd8
  if (isJPG) return 'image/jpeg'
  return 'application/octet-stream'
}
const toImageSrc = (raw) => {
  if (!raw) return ''
  if (Array.isArray(raw)) {
    const bytes = new Uint8Array(raw)
    const mime = sniffMime(bytes)
    return `data:${mime};base64,${uint8ToBase64(bytes)}`
  }
  const s = String(raw).trim()
  if (s.startsWith('data:image/')) return s
  if (/^https?:\/\//i.test(s)) return s
  if (/^0x[0-9a-f]+$/i.test(s)) {
    const bytes = hexToUint8(s)
    const mime = sniffMime(bytes)
    return `data:${mime};base64,${uint8ToBase64(bytes)}`
  }
  if (/^[A-Za-z0-9+/=_\-\s]+$/.test(s)) {
    try {
      const bytes = b64ToUint8(s)
      const mime = sniffMime(bytes)
      return `data:${mime};base64,${uint8ToBase64(bytes)}`
    } catch {}
  }
  return ''
}
const anyImageCandidate = computed(
  () =>
    fd.value?.couponImageBase64 ??
    fd.value?.couponImage ??
    fd.value?.coupon_image ??
    fd.value?.imageBase64 ??
    fd.value?.image ??
    fd.value?.imageUrl ??
    fd.value?.image_url ??
    null,
)
const imageSrc = computed(() => toImageSrc(anyImageCandidate.value))

/* 驗證 & 發放 */
const validate = async () => {
  // ✅ 先就位，避免 TDZ
  const f = fd.value || {}

  // --- 基本驗證 ---
  if (!f.eventId) throw new Error('請先選擇活動')
  if (!f.couponCategoryId) throw new Error('請先選擇優惠券類型')
  if (!f.couponName) throw new Error('請輸入優惠券名稱')

  if (f.discountType && (f.discountAmount == null || f.discountAmount === '')) {
    throw new Error('請填寫折扣數值')
  }

  // --- 每人可兌換次數驗證 ---
  const times = Number(f.redeemableTimes ?? 1)
  if (!Number.isInteger(times) || times < 1 || times > 99) {
    throw new Error('每人可兌換次數需為 1~99 的整數')
  }
  f.redeemableTimes = times

  // --- 數量驗證（你新加的欄位） ---
  const qty = Number(f.quantity ?? 1)
  if (!Number.isInteger(qty) || qty < 1 || qty > 99999) {
    throw new Error('數量需為 1~99,999 的整數')
  }
  f.quantity = qty

  // --- 狀態正規化 ---
  f.status = f.status === 'INACTIVE' ? 'INACTIVE' : 'ACTIVE'

  return true
}

const issue = async () => {
  await validate()
  isIssuing.value = true
  try {
    const f = fd.value
    const payload = {
      eventId: f.eventId,
      couponCategoryId: f.couponCategoryId,
      couponName: f.couponName,
      couponDescription: f.couponDescription,
      discountType: f.discountType || null,
      discountAmount: f.discountType ? f.discountAmount : null,
      minimumSpend: f.minimumSpend ?? 0,
      redeemableTimes: f.redeemableTimes ?? 1,
      quantity: f.quantity ?? 1,
      status: f.status || 'ACTIVE',
      couponImageBase64: f.couponImageBase64 || null,
      movieId: f.movieId || null,
      memberLevelId: f.memberLevelId || null,
      sessionId: f.sessionId || null,
      sessionDate: f.sessionDate || null,
    }
    const created = await httpClient.post('/admin/coupons', payload)
    ElMessage.success('優惠券發放成功！')
    emit('issue-success', created)
    return created
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '發放失敗'
    ElMessage.error(msg)
    throw new Error(msg)
  } finally {
    isIssuing.value = false
  }
}

defineExpose({ validate, issue })
</script>

<style scoped>
.overview-container {
  padding: 20px;
}
.summary-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
  max-width: 800px;
  margin: 0 auto;
}
.summary-card h3 {
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.info-item {
  margin-bottom: 14px;
}
.info-item strong {
  margin-right: 10px;
  color: #555;
  font-weight: 600;
}
.info-item ul {
  list-style-type: disc;
  padding-left: 20px;
  margin-top: 5px;
}
.image-preview {
  margin-top: 20px;
}
.coupon-image {
  max-width: 800px;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 10px;
}
.editable .hint {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.status-chip {
  margin-left: 10px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.status-chip.is-active {
  background: #e8f4ff;
  color: #1677ff;
}
.status-chip.is-inactive {
  background: #fff1f0;
  color: #ff4d4f;
}
</style>
