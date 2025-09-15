<template>
  <section class="page">
    <div class="container">
      <div class="topbar">
        <router-link class="back" to="/announcements">← 回公告列表</router-link>
      </div>

      <div v-if="loading" class="state">載入中…</div>
      <div v-else-if="error" class="state error">{{ error }}</div>

      <article v-else class="article">
        <h1 class="title">{{ row.title }}</h1>
        <div class="meta">
          <span class="date">{{ fmtDate(row.publishDate) }}</span>
        </div>

        <div v-if="imgUrl" class="hero">
          <img :src="imgUrl" alt="公告圖片" class="hero-img" />
        </div>

        <div class="content" v-html="safeContent"></div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import httpClient from '@/services/api'
import announcementPublicService from '../../services/AnnouncementPublicService'

const route = useRoute()
const id = String(route.params.id ?? '')

const loading = ref(true)
const error = ref('')
const row = ref({})
const imgUrl = ref('')

// ===== Utils =====
const fmtDate = (s) => {
  if (!s) return ''
  const t = Date.parse(String(s).replace(/\//g, '-'))
  if (Number.isNaN(t)) return String(s)
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const absUrl = (u) => {
  if (!u) return ''
  if (/^https?:\/\//i.test(u)) return u
  if (u.startsWith('//')) return `${location.protocol}${u}`
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  const rel = u.startsWith('/') ? u : `/${u}`
  return `${base}${rel}`
}
const adapt = (r) => {
  const id = r.id ?? r.announcementId ?? r.announcement_id
  const publishDate =
    r.publishDate ?? r.publish_date ?? r.date ?? r.createdAt ?? r.created_at ?? null
  return {
    id: String(id),
    title: r.title ?? r.announcementTitle ?? r.name ?? r.subject ?? '',
    publishDate,
    content: r.content ?? r.body ?? '',
    // 常見圖片欄位擇一
    imageUrl:
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
      r.cover_path ||
      r.coverPath ||
      '',
  }
}
const safeContent = computed(() => row.value?.content ?? '')

// ===== Data fetch =====
async function fetchDetailPreferSingle() {
  // 1) 若列表頁預先帶了 prefetch，先渲染（加速首屏）
  const prefetch = history.state?.prefetch
  if (prefetch) row.value = adapt(prefetch)

  // 2) 嘗試「單筆詳情」端點
  try {
    const data = await announcementPublicService.getById(id)
    row.value = adapt(data)
    return true
  } catch (e) {
    // 若後端暫無單筆端點或 404，再回退用列表掃描
    console.debug('[DETAIL] getById fallback to list scan:', e?.response?.status || e?.message)
    try {
      const first = await announcementPublicService.list({
        page: 0,
        size: 10,
        sort: 'publishDate,desc',
      })
      const content = Array.isArray(first?.content)
        ? first.content
        : Array.isArray(first)
          ? first
          : []
      let hit = content.find(
        (it) => String(it.id ?? it.announcementId ?? it.announcement_id) === id,
      )
      if (!hit && Number.isInteger(first?.totalPages)) {
        for (let p = 1; p < first.totalPages; p++) {
          /* eslint-disable no-await-in-loop */
          const pageData = await announcementPublicService.list({
            page: p,
            size: first.size ?? 10,
            sort: 'publishDate,desc',
          })
          const arr = Array.isArray(pageData?.content) ? pageData.content : []
          hit = arr.find((it) => String(it.id ?? it.announcementId ?? it.announcement_id) === id)
          if (hit) break
          if (pageData?.last === true) break
        }
      }
      if (!hit) throw new Error('not-found')
      row.value = adapt(hit)
      return true
    } catch (e2) {
      console.error('[DETAIL] not found:', e2)
      throw e2
    }
  }
}

async function fetchImage() {
  // 1) 先用 blob 端點（公開）
  try {
    const blob = await announcementPublicService.getImage(row.value.id)
    if (blob instanceof Blob && blob.size > 0) {
      imgUrl.value = URL.createObjectURL(blob)
      return
    }
  } catch (e) {
    // 靜默 fallback
  }
  // 2) 退回物件中的圖片欄位（若存在）
  if (row.value.imageUrl) {
    imgUrl.value = absUrl(String(row.value.imageUrl))
  } else {
    imgUrl.value = ''
  }
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    await fetchDetailPreferSingle()
    await fetchImage()
  } catch (e) {
    error.value = '找不到該公告'
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
  padding-inline: 16px;
  padding-block: 24px;
}
@media (min-width: 768px) {
  .page {
    padding-inline: 24px;
  }
}
@media (min-width: 1024px) {
  .page {
    padding-inline: 40px;
  }
}
.container {
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
.content :deep(p) {
  line-height: 1.9;
  margin: 0 0 14px;
}
.hero {
  margin: 12px 0 20px;
}
.hero-img {
  width: 40%;
  height: 40%;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #333;
}
@media (max-width: 640px) {
  .article {
    padding: 18px;
  }
  .title {
    font-size: 22px;
  }
}
</style>
