<template>
  <section class="page">
    <div class="toolbar">
      <input
        v-model.trim="keyword"
        class="search"
        type="search"
        placeholder="輸入關鍵字搜尋公告…"
        @keyup.enter="fetchList(0)"
      />
      <button class="btn" :disabled="loading" @click="fetchList(0)">搜尋</button>
    </div>

    <div v-if="loading" class="state">載入中…</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <template v-else>
      <ul v-if="rows.length" class="cards">
        <li
          v-for="a in rows"
          :key="a.id"
          class="card"
          @click="goDetail(a)"
          role="button"
          tabindex="0"
        >
          <div class="thumb" v-if="a.imageUrl">
            <img :src="a.imageUrl" alt="" />
          </div>
          <div class="body">
            <h3 class="title">{{ a.title }}</h3>
            <div class="meta">
              <span class="date">{{ fmtDate(a.publishDate) }}</span>
            </div>
            <p class="excerpt">{{ a.excerpt }}</p>
          </div>
        </li>
      </ul>

      <div v-else class="state">沒有符合條件的公告。</div>

      <div class="pager" v-if="totalPages > 1">
        <button class="pg" :disabled="page <= 0" @click="fetchList(page - 1)">上一頁</button>
        <span class="pginfo">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="pg" :disabled="page >= totalPages - 1" @click="fetchList(page + 1)">
          下一頁
        </button>
      </div>
    </template>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import httpClient from '@/services/api'
import announcementPublicService from '@/features/Event/services/AnnouncementPublicService'
announcementPublicService
const router = useRouter()

const loading = ref(true)
const error = ref('')
const rows = ref([])
const keyword = ref('')
const page = ref(0)
const size = ref(12)
const totalPages = ref(0)

// Utils
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
  const imageField =
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
    r.coverPath
  const publishDate =
    r.publishDate ?? r.publish_date ?? r.date ?? r.createdAt ?? r.created_at ?? null
  const text = (r.content ?? r.body ?? '').replace(/<[^>]+>/g, '') // 去標籤取摘要
  return {
    id: String(id),
    title: r.title ?? r.announcementTitle ?? r.name ?? r.subject ?? '',
    publishDate,
    imageUrl: imageField ? absUrl(String(imageField)) : '',
    excerpt: text.length > 120 ? `${text.slice(0, 120)}…` : text,
  }
}

async function fetchList(toPage = 0) {
  loading.value = true
  error.value = ''
  try {
    page.value = toPage
    const data = await announcementPublicService.list({
      page: page.value,
      size: size.value,
      sort: 'publishDate,desc',
      keyword: keyword.value || undefined,
    })
    // 攔截器已回 data，嘗試識別 Page 物件
    const content = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
    rows.value = content.map(adapt)
    totalPages.value = Number.isInteger(data?.totalPages) ? data.totalPages : 1
  } catch (e) {
    console.error('[LIST] error', e)
    error.value = '無法載入公告列表'
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  // 帶 prefetch 秒開；路由請確保 path: '/announcements/:id'
  router.push({
    path: `/announcements/${item.id}`,
    state: { prefetch: item },
  })
}

onMounted(() => {
  fetchList(0)
})
</script>

<style scoped>
.page {
  color: #fff;
  padding: 24px;
  width: 80%;
  margin: 0 auto;
}

/* 工具列維持原外觀 */
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.search {
  flex: 1;
  max-width: 360px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #333;
  background: #111;
  color: #eee;
}
.btn {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #333;
  background: #1f1f1f;
  color: #eee;
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 內容狀態 */
.state {
  color: #bbb;
}
.error {
  color: #ff8a8a;
}

/* === 列表樣式（非卡片）=== */
.cards {
  list-style: none;
  padding: 0;
  margin: 0;
  display: block; /* 由網格改為普通列表 */
}

/* 每列：左圖右文 */
.card {
  display: grid;
  /* grid-template-columns: 600px 1fr; */
  gap: 12px;
  align-items: center;
  padding: 12px 8px;
  background: transparent; /* 取消卡片底 */
  border: none; /* 取消卡片框線 */
  border-bottom: 1px solid #333; /* 列表分隔線 */
  border-radius: 0; /* 取消圓角 */
  box-shadow: none; /* 移除陰影 */
  cursor: pointer;
}
.card:first-child {
  border-top: 1px solid #333; /* 讓列表有上下邊界一致感 */
}

/* 列表 hover/focus 效果 */
.card:hover,
.card:focus,
.card:focus-within {
  background: #161616;
  transform: none; /* 移除卡片位移效果 */
  transition: background 0.15s ease;
  outline: none;
}

/* 縮圖：固定高，保留比例 */
.thumb {
  height: 100px;
  overflow: hidden;
  background: #0d0d0d;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px; /* 列表保留小圓角縮圖 */
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 文字區 */
.body {
  padding: 0; /* 列表不需要額外內距 */
}
.title {
  margin: 0 0 4px;
  font-size: 18px;
  line-height: 1.35;
  color: #e9eaeb; /* 確保不是黑字 */
  font-weight: 700;
}
.meta {
  color: #9aa0a6;
  font-size: 12px;
  margin-bottom: 6px;
}
.excerpt {
  color: #cfcfcf;
  font-size: 14px;
  line-height: 1.6;
  margin: 0; /* 與列表排版對齊 */
}

/* 響應式：窄螢幕改成上下排列 */
@media (max-width: 720px) {
  .card {
    grid-template-columns: 1fr;
    align-items: start;
  }
  .thumb {
    height: 180px;
  }
}

/* 分頁維持原暗色風格 */
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
