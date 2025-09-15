<template>
  <section class="page">
    <div class="topbar">
      <router-link class="back" to="/events">← 回活動列表</router-link>
    </div>

    <div v-if="loading" class="state">載入中…</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <article v-else class="article">
      <h1 class="title">{{ row.title }}</h1>
      <div class="meta">{{ rangeText(row.startDate, row.endDate) }}</div>

      <div v-if="imgUrl" class="hero"><img :src="imgUrl" alt="" /></div>

      <div class="content" v-html="row.content"></div>
    </article>
  </section>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import eventPublicService from '@/features/Event/services/eventPublicService'
import httpClient from '@/services/api'
const route = useRoute()
const id = String(route.params.id ?? '')

const loading = ref(true)
const error = ref('')
const row = ref({})
const imgUrl = ref('')

// utils
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
const adapt = (r) => {
  const id = r.id ?? r.eventId ?? r.event_id
  const img =
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
    id: String(id),
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
    content: r.content ?? r.description ?? '',
    imageUrl: img ? absUrl(String(img)) : '',
    base64ImageString: r.base64ImageString ?? r.base64_image_string ?? null,
  }
}
const toDataUrl = (b64, mime = 'image/*') => `data:${mime};base64,${b64}`

async function fetchDetail() {
  // 先吃 prefetch（從列表點進來秒開）
  const pre = history.state?.prefetch
  if (pre) row.value = adapt(pre.raw || pre)

  // 以單筆覆寫
  const data = await eventPublicService.getById(id)
  row.value = adapt(data)
}

async function resolveImage() {
  // 1) 若後端回了 Base64，直接用 data URL
  if (row.value.base64ImageString) {
    imgUrl.value = toDataUrl(row.value.base64ImageString)
    return
  }
  // 2) 若有絕對/相對 URL 欄位，直接用
  if (row.value.imageUrl) {
    imgUrl.value = row.value.imageUrl
    return
  }
  // 3)（可選）最後才嘗試 blob 端點；讓 404 靜默
  try {
    const blob = await eventPublicService.getImage(row.value.id, { silent404: true })
    if (blob instanceof Blob && blob.size > 0) {
      imgUrl.value = URL.createObjectURL(blob)
      return
    }
  } catch (_) {
    /* 靜默 */
  }
  imgUrl.value = ''
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    await fetchDetail()
    await resolveImage()
  } catch (e) {
    console.error('[EventDetail] error', e)
    error.value = '找不到該活動'
  } finally {
    loading.value = false
  }
})
onBeforeUnmount(() => {
  if (imgUrl.value?.startsWith('blob:')) URL.revokeObjectURL(imgUrl.value)
})
</script>

<style scoped>
.page {
  color: #fff;
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}
.topbar {
  margin-bottom: 16px;
}
.back {
  color: #9aa0a6;
  text-decoration: none;
}
.back:hover {
  color: #e50914;
}
.state {
  color: #bbb;
}
.error {
  color: #ff8a8a;
}
.article {
  background: #1f1f1f;
  border: 1px solid #333;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}
.title {
  margin: 0 0 8px;
  font-size: 28px;
  line-height: 1.35;
}
.meta {
  color: #9aa0a6;
  font-size: 14px;
  margin-bottom: 16px;
}
.hero {
  margin: 12px 0 20px;
}
.hero img {
  width: 80%;
  max-height: 80%;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #333;
}
.content :deep(p) {
  line-height: 1.9;
  margin: 0 0 14px;
}
</style>
