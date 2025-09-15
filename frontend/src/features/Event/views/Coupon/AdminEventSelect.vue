<template>
  <div>
    <h2>選擇活動</h2>

    <div v-if="isLoading" class="loading-container">
      <span>載入中...</span>
    </div>

    <div v-else-if="eventsError" class="error-container">
      <el-icon><WarningFilled /></el-icon>
      <span>無法載入活動列表，請稍後再試。</span>
    </div>

    <div v-else class="event-selection">
      <el-select
        v-model="selectedEventId"
        placeholder="請選擇一個活動"
        size="large"
        clearable
        @change="handleEventChange"
      >
        <el-option
          v-for="event in events.list"
          :key="event.eventId"
          :label="event.title"
          :value="event.eventId"
        />
      </el-select>
    </div>

    <div v-if="selectedEvent" class="selected-event-preview">
      <h3 class="event-title">{{ selectedEvent.title }}</h3>

      <p class="event-date">活動日期：{{ selectedEventDateText }}</p>

      <div v-if="isImageLoading" class="no-image-placeholder">圖片載入中...</div>
      <img
        v-else-if="eventImageUrl"
        class="event-image"
        :src="eventImageUrl"
        :alt="selectedEvent.title"
      />

      <span v-else class="no-image-placeholder">無圖片</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { storeToRefs } from 'pinia'
import { useEventStore } from '@/features/Event/store/useEventStore'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import eventService from '../../services/eventService'

const emit = defineEmits(['data-change', 'next', 'prev'])

const eventStore = useEventStore()
const { events, loading: isLoading, error: eventsError } = storeToRefs(eventStore)

// 用 number|null，避免字串/數字型別比對不到
const selectedEventId = ref(null)

// 【新增】專門用來儲存圖片載入狀態和圖片 URL 的 ref
const isImageLoading = ref(false)
const eventImageUrl = ref(null) // 用於儲存安全的 Blob URL

/** 目前選取的活動物件 */
const selectedEvent = computed(() => {
  const list = events.value?.list ?? []
  if (selectedEventId.value == null) return null
  return list.find((e) => e.eventId === selectedEventId.value) || null
})

/**
 * 【核心改造】使用 watch 監聽使用者選擇的變化，並按需載入圖片
 */
watch(selectedEvent, async (newEvent) => {
  // 1. 清理上一次的圖片 URL，釋放記憶體
  if (eventImageUrl.value) {
    URL.revokeObjectURL(eventImageUrl.value)
    eventImageUrl.value = null
  }

  // 2. 如果沒有選擇新的活動 (例如：使用者清空了選擇)，則直接返回
  if (!newEvent) {
    return
  }

  // 3. 檢查新選擇的活動是否有圖片
  if (newEvent.hasImage) {
    isImageLoading.value = true // 顯示"圖片載入中..."
    try {
      // 4. 呼叫 service 安全地獲取圖片的 Blob URL
      eventImageUrl.value = await eventService.getEventImage(newEvent.eventId)
    } catch (e) {
      console.error('預覽圖片載入失敗:', e)
      ElMessage.error('預覽圖片載入失敗')
    } finally {
      isImageLoading.value = false // 隱藏"圖片載入中..."
    }
  }
})

/** 將多種可能的日期欄位，整理成顯示文字 */
const selectedEventDateText = computed(() => {
  const e = selectedEvent.value
  if (!e) return ''
  // 嘗試支援的欄位名（依你後端實際命名可精簡）
  const start =
    e.startDate || e.start_at || e.startAt || e.eventStart || e.date || e.eventDate || e.beginDate
  const end = e.endDate || e.end_at || e.endAt || e.eventEnd || e.finishDate || e.closeDate
  const startTime = e.startTime || e.timeStart
  const endTime = e.endTime || e.timeEnd

  const fmt = (d) => {
    try {
      const dt = new Date(d)
      if (!isNaN(dt)) return dt.toLocaleDateString()
    } catch {}
    return String(d)
  }

  if (start && end) {
    const s = `${fmt(start)}${startTime ? ' ' + startTime : ''}`
    const t = `${fmt(end)}${endTime ? ' ' + endTime : ''}`
    return `${s} ~ ${t}`
  }
  if (start) return `${fmt(start)}${startTime ? ' ' + startTime : ''}`
  if (startTime || endTime) return `${startTime ?? ''}${endTime ? ' ~ ' + endTime : ''}`
  return '未提供日期'
})

/** 下拉改變時，回拋資料給父層 */
const handleEventChange = (val) => {
  // 若後端 eventId 是數字但 el-select 回傳字串，可在此統一轉型
  if (typeof val === 'string') {
    const asNum = Number(val)
    if (!Number.isNaN(asNum)) selectedEventId.value = asNum
  }
  emit('data-change', {
    eventId: selectedEventId.value,
    event: selectedEvent.value,
  })
}

onMounted(() => {
  eventStore.fetchEvents()
})

/** 在元件卸載前，做最後一次記憶體清理 */
onBeforeUnmount(() => {
  if (eventImageUrl.value) {
    URL.revokeObjectURL(eventImageUrl.value)
  }
})

watch(eventsError, (err) => {
  if (err) ElMessage.error(err)
})
</script>

<style scoped>
.loading-container,
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 200px;
  color: #909399;
}
.error-container .el-icon {
  font-size: 30px;
  color: #f56c6c;
  margin-bottom: 10px;
}

.event-selection {
  margin-top: 20px;
}

.selected-event-preview {
  margin-top: 20px;
  text-align: center;
}
.event-title {
  margin: 0 0 8px;
  font-weight: 700;
  font-size: 20px;
}
.event-date {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
}

.event-image {
  max-width: 60%;
  border-radius: 8px;
  display: block;
  margin: 0 auto;
}
.no-image-placeholder {
  width: 300px;
  height: 200px;
  background: #f0f2f5;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin: 0 auto;
}
.navigation-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}
</style>
