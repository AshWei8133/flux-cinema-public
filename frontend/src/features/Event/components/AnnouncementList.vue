<template>
  <div class="announcement-card">
    <h3>最新消息</h3>

    <div v-if="loading" class="state">載入中…</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <ul v-else class="announcement-list">
      <li v-for="item in mixedList" :key="item.uid">
        <span class="tag" :class="item.type">
          {{ item.type === 'event' ? '活動' : '公告' }}
        </span>

        <a href="#" @click.prevent="goDetail(item)">{{ item.title }}</a>

        <span class="date">{{ fmtDate(item.date) }}</span>
      </li>
    </ul>

    <router-link to="/announcements" class="more-link"> 查看更多公告 &rarr; </router-link>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import eventPublicService from '@/features/Event/services/eventPublicService'
import announcementPublicService from '../services/AnnouncementPublicService'
import httpClient from '@/services/api'

const router = useRouter()

const loading = ref(true)
const error = ref('')
const announcement2 = ref([]) // 公告兩筆
const event2 = ref([]) // 活動兩筆

// ============ 小工具 ============
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
// 公告轉換
const adaptAnnouncement = (r) => ({
  uid: `a-${r.id ?? r.announcementId ?? r.announcement_id}`,
  id: String(r.id ?? r.announcementId ?? r.announcement_id ?? ''),
  type: 'system', // 你的設計：公告=系統
  title: r.title ?? r.announcementTitle ?? r.name ?? r.subject ?? '',
  date: r.publishDate ?? r.publish_date ?? r.date ?? r.createdAt ?? r.created_at ?? null,
})
// 活動轉換
const adaptEvent = (r) => ({
  uid: `e-${r.id ?? r.eventId ?? r.event_id}`,
  id: String(r.id ?? r.eventId ?? r.event_id ?? ''),
  type: 'event',
  title: r.title ?? r.name ?? '',
  // 依據你的後端欄位填：startDate / publishDate / createdAt ...
  date:
    r.startDate ??
    r.start_date ??
    r.publishDate ??
    r.publish_date ??
    r.createdAt ??
    r.created_at ??
    null,
})

// 合併排序：日期新到舊
const mixedList = computed(() => {
  const list = [...announcement2.value, ...event2.value]
  return list
    .filter((x) => !!x.title)
    .sort((a, b) => {
      const ta = Date.parse(String(a.date || '')) || 0
      const tb = Date.parse(String(b.date || '')) || 0
      return tb - ta
    })
})

// ============ 請求兩邊各 2 筆 ============
async function fetchAnnouncements() {
  const res = await announcementPublicService.list({ page: 0, size: 2, sort: 'publishDate,desc' })
  const content = Array.isArray(res?.content) ? res.content : Array.isArray(res) ? res : []
  announcement2.value = content.map(adaptAnnouncement)
}
async function fetchEvents() {
  // 注意：請把 sort 改成你後端接受的日期欄位（常見 startDate 或 publishDate）
  const res = await eventPublicService.list({ page: 0, size: 2, sort: 'startDate,desc' })
  const content = Array.isArray(res?.content) ? res.content : Array.isArray(res) ? res : []
  event2.value = content.map(adaptEvent)
}

// 點擊導頁
function goDetail(item) {
  if (item.type === 'event') {
    if (item.id) router.push({ path: `/events/${item.id}` })
    else router.push({ path: `/events` })
  } else {
    if (item.id) router.push({ path: `/announcements/${item.id}` })
    else router.push({ path: `/announcements` })
  }
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([fetchAnnouncements(), fetchEvents()])
  } catch (e) {
    console.error('[HomeNews] fetch error', e)
    error.value = '無法載入最新消息'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.announcement-card {
  background-color: #2a2a2a;
  padding: 30px;
  border-radius: 8px;
}
h3 {
  font-size: 24px;
  margin: 0 0 20px;
  border-left: 4px solid #e50914;
  padding-left: 15px;
  color: #fff;
}
.state {
  color: #bbb;
}
.error {
  color: #ff8a8a;
}
.announcement-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.announcement-list li {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #444;
}
.announcement-list li:last-child {
  border-bottom: none;
}
.tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 15px;
  flex-shrink: 0;
}
.tag.event {
  background-color: #e50914;
  color: #fff;
}
.tag.system {
  background-color: #3b82f6;
  color: #fff;
}
a {
  color: #fff;
  text-decoration: none;
  flex-grow: 1;
  transition: color 0.3s;
}
a:hover {
  color: #e50914;
}
.date {
  color: #888;
  font-size: 14px;
}
.more-link {
  display: block;
  text-align: right;
  margin-top: 20px;
  color: #e50914;
  text-decoration: none;
  font-weight: bold;
}
</style>
