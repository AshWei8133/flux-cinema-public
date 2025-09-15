<template>
  <section class="redirecting">
    <p v-if="loading">正在前往最新公告…</p>
    <p v-else-if="error" class="error">{{ error }}，即將帶你回公告列表</p>
  </section>
</template>

<script setup>
/**
 * 進入此頁 → 打 API 取得「最新一筆公告」 → 立即導向 /announcements/:id
 * 若失敗 → 導回 /announcements
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import httpClient from '@/services/api'

const router = useRouter()
const loading = ref(true)
const error = ref('')

const pickList = (data) => {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data?.records)) return data.records
  return []
}

const go = async () => {
  try {
    // 以排序+size=1 取得最新一筆（注意使用 publishDate,desc）
    const { data } = await httpClient.get('/announcements', {
      params: { page: 0, size: 1, sort: 'publishDate,desc' },
    })
    const list = pickList(data)
    const first = list[0]
    const id = first?.id ?? first?.announcementId
    if (id) {
      await router.replace(`/announcements/${id}`)
      return
    }
    throw new Error('No announcement found')
  } catch (e) {
    error.value = '找不到最新公告'
    setTimeout(() => router.replace('/announcements'), 600)
  } finally {
    loading.value = false
  }
}

onMounted(go)
</script>

<style scoped>
.redirecting {
  color: #fff;
  padding: 24px;
}
.error {
  color: #ff8a8a;
}
</style>
