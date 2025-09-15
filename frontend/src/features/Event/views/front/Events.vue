<template>
  <section class="page">
    <!-- 小型幻燈片（最新活動 5 張圖） -->
    <div class="mini-hero" v-if="slides.length">
      <div class="mini-hero-track" :style="{ transform: `translateX(-${current * 100}%)` }">
        <div
          class="mini-hero-item"
          v-for="(s, i) in slides"
          :key="s.uid"
          @click="goDetail({ id: s.id, type: 'event' })"
        >
          <img :src="s.image" :alt="s.title" @error="$event.target.src = placeholder" />
        </div>
      </div>
      <div class="mini-hero-dots">
        <button
          v-for="(s, i) in slides"
          :key="`dot-${i}`"
          :class="{ active: i === current }"
          @click="current = i"
          aria-label="slide dot"
        />
      </div>
    </div>

    <!-- 篩選工具列 -->
    <div class="toolbar">
      <input
        v-model.trim="keyword"
        class="ipt"
        type="search"
        placeholder="輸入關鍵字…"
        @keyup.enter="reload(0)"
      />
      <input v-model="startDateFrom" class="ipt" type="date" @change="reload(0)" />
      <input v-model="startDateTo" class="ipt" type="date" @change="reload(0)" />
      <button class="btn" :disabled="loading" @click="reload(0)">查詢</button>
      <button class="btn reset" :disabled="loading" @click="resetFilters">重置</button>
    </div>

    <!-- 狀態 -->
    <div v-if="loading" class="state">載入中…</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <!-- 卡片網格 -->
    <div v-else class="grid">
      <article
        class="card"
        v-for="e in rows"
        :key="e.id"
        @click="goDetail({ id: e.id, type: 'event' })"
      >
        <div class="thumb">
          <img
            :src="e.imageUrl || placeholder"
            :alt="e.title"
            loading="lazy"
            decoding="async"
            @error="$event.target.src = placeholder"
          />
        </div>
        <div class="body">
          <h3 class="title">{{ e.title }}</h3>
          <div class="meta">{{ rangeText(e.startDate, e.endDate) }}</div>
          <p class="excerpt" v-if="e.excerpt">{{ e.excerpt }}</p>
        </div>
      </article>

      <div v-if="!rows.length" class="state">目前沒有活動。</div>
    </div>

    <!-- 分頁 -->
    <div class="pager" v-if="totalPages > 1">
      <button class="pg" :disabled="page <= 0" @click="reload(page - 1)">上一頁</button>
      <span class="pginfo">{{ page + 1 }} / {{ totalPages }}</span>
      <button class="pg" :disabled="page >= totalPages - 1" @click="reload(page + 1)">
        下一頁
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import eventPublicService from '@/features/Event/services/eventPublicService'
import httpClient from '@/services/api'
const router = useRouter()

// ====== 狀態 ======
const loading = ref(true)
const error = ref('')
const rows = ref([]) // 卡片資料
const page = ref(0)
const size = ref(12)
const totalPages = ref(0)

// 幻燈片
const slides = ref([])
const current = ref(0)
let timer = null

// 篩選
const keyword = ref('')
const startDateFrom = ref('')
const startDateTo = ref('')

// ====== 工具 ======
const fmtDate = (s) => {
  if (!s) return ''
  const t = Date.parse(String(s).replace(/\//g, '-'))
  if (Number.isNaN(t)) return String(s)
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const rangeText = (a, b) => [fmtDate(a), fmtDate(b)].filter(Boolean).join(' ~ ')
const absUrl = (u) => {
  if (!u) return ''
  if (/^https?:\/\//i.test(u)) return u
  if (u.startsWith('//')) return `${location.protocol}${u}`
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  const rel = u.startsWith('/') ? u : `/${u}`
  return `${base}${rel}`
}
const textOnly = (html) =>
  String(html || '')
    .replace(/<[^>]+>/g, '')
    .replace(/\s+/g, ' ')
    .trim()

// ====== 工具 ======

// 組成最終圖片 URL 的邏輯：base64 > 欄位 URL > blob 端點（加快取破壞器）
const finalImageUrl = (id, base64Str, fieldUrl) => {
  if (base64Str) return `data:image/*;base64,${base64Str}`

  if (fieldUrl) {
    const abs = absUrl(String(fieldUrl))
    return `${abs}?v=${Date.now()}` // 避免快取
  }

  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  return `${base}/events/${id}/image?v=${Date.now()}`
}

const adapt = (r) => {
  const id = String(r.id ?? r.eventId ?? r.event_id)

  // 優先 Base64
  const b64 = r.base64ImageString ?? r.base64_image_string ?? null

  // 其他可能的欄位
  const imgField =
    r.imageUrl ||
    r.coverUrl ||
    r.bannerUrl ||
    r.pictureUrl ||
    r.thumbnailUrl ||
    r.image ||
    r.cover ||
    r.banner ||
    r.pic ||
    r.image_path ||
    r.imagePath ||
    ''

  return {
    id,
    title: r.title ?? r.name ?? '',
    startDate:
      r.startDate ??
      r.start_date ??
      r.beginDate ??
      r.begin_date ??
      r.publishDate ??
      r.publish_date ??
      null,
    endDate: r.endDate ?? r.end_date ?? r.finishDate ?? r.finish_date ?? null,

    // 統一用 finalImageUrl
    imageUrl: finalImageUrl(id, b64, imgField),

    excerpt: textOnly(r.content ?? r.description ?? ''),
    raw: r,
  }
}

const placeholder =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="600" height="800"><rect width="100%" height="100%" fill="#222"/><text x="50%" y="50%" fill="#666" dominant-baseline="middle" text-anchor="middle" font-size="24">No Image</text></svg>',
  )

// ====== 請求 ======
async function reload(toPage = 0) {
  loading.value = true
  error.value = ''
  try {
    page.value = toPage
    const res = await eventPublicService.list({
      page: page.value,
      size: size.value,
      sort: 'startDate,desc',
      keyword: keyword.value || undefined,
      startDateFrom: startDateFrom.value || undefined,
      startDateTo: startDateTo.value || undefined,
    })
    const content = Array.isArray(res?.content) ? res.content : Array.isArray(res) ? res : []
    rows.value = content.map(adapt)
    totalPages.value = Number.isInteger(res?.totalPages) ? res.totalPages : 1

    // 幻燈片取前 4 張（新到舊）
    slides.value = rows.value.slice(0, 5).map((e) => ({
      uid: `s-${e.id}`,
      id: e.id,
      title: e.title,
      startDate: e.startDate,
      endDate: e.endDate,
      image: e.imageUrl || placeholder,
    }))
    startAutoplay()
  } catch (e) {
    console.error('[Events] list error', e)
    error.value = '無法載入活動列表'
  } finally {
    loading.value = false
  }
}

function startAutoplay() {
  stopAutoplay()
  if (slides.value.length <= 1) return
  timer = setInterval(() => {
    current.value = (current.value + 1) % slides.value.length
  }, 3500)
}
function stopAutoplay() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function goDetail(item) {
  router.push({
    path: `/events/${item.id}`,
    state: { prefetch: rows.value.find((r) => r.id === item.id) },
  })
}

onMounted(() => reload(0))
onBeforeUnmount(() => stopAutoplay())
</script>

<style scoped>
.page {
  color: #fff;
  padding: 24px;
  max-width: 1350px;
  margin: 0 auto;
}

/* 迷你幻燈片 */
.mini-hero {
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  border: 1px solid #333;
  margin-bottom: 18px;
}
.mini-hero-track {
  display: flex;
  transition: transform 0.4s ease;
}
.mini-hero-item {
  min-width: 100%;
  position: relative;
  cursor: pointer;
}
.mini-hero-item img {
  width: 100%;
  height: auto;
  object-fit: cover;
  display: block;
}
.mini-hero-dots {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 10px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
.mini-hero-dots button {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: none;
  background: #666;
  opacity: 0.7;
  cursor: pointer;
}
.mini-hero-dots button.active {
  background: #e50914;
  opacity: 1;
}

/* 工具列 */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.ipt {
  background: #111;
  color: #eee;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 8px 10px;
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
.btn.reset:hover {
  background: #444;
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
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}
.card:hover {
  transform: translateY(-2px);
  transition: transform 0.15s ease;
}
.thumb {
  height: 300px;
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
}
.excerpt {
  color: #cfcfcf;
  font-size: 14px;
  line-height: 1.6;
}

/* 分頁 */
.pager {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
  margin-top: 16px;
}
.pg {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #333;
  background: #121212;
  color: #eee;
}
.pginfo {
  color: #9aa0a6;
}
</style>
