<template>
  <div v-if="shouldRenderComponent" class="showtimes-container" v-loading="isLoading">
    <el-alert
      v-if="!isLoading && error"
      :title="error"
      type="error"
      show-icon
      center
      :closable="false"
    />

    <div v-if="!isLoading && !error">
      <div class="filter-section">
        <label class="filter-label">電影版本</label>
        <el-select
          v-model="selectedTheaterTypeId"
          class="version-select"
          placeholder="選擇電影版本"
          size="large"
          :disabled="theaterTypes.length === 0"
        >
          <el-option
            v-for="type in theaterTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
      </div>

      <template v-if="selectedTheaterTypeId">
        <div v-if="Object.keys(groupedShowtimes).length > 0" class="showtimes-grid">
          <div v-for="(sessions, date) in groupedShowtimes" :key="date" class="date-group">
            <h3 class="date-header">{{ formatDate(date) }}</h3>
            <el-row :gutter="12">
              <el-col
                v-for="session in sessions"
                :key="session.sessionId"
                :xs="12"
                :sm="6"
                class="session-col"
              >
                <div
                  class="session-time-wrapper"
                  :class="{ 'sold-out': session.status === 'sold-out' }"
                  @click="goToSeatSelection(session)"
                >
                  <Icon
                    icon="ic:baseline-chair"
                    :class="['seat-icon', `status-${session.status}`]"
                  />
                  <span class="session-time">{{ session.time }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
        <el-empty v-else description="所選版本近期無場次" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { storeToRefs } from 'pinia'
import { usePublicMovieShowtimesStore } from '../../store/usePublicMovieShowtimesStore'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { watch } from 'vue'

// 接收從父組件(FilmDetail.vue)傳來的電影ID
const props = defineProps({
  movieId: {
    type: Number,
    required: true,
  },
})

// 建立 router 實例
const router = useRouter()

// =================================================================
// ===== Store 連接 =====
// =================================================================
const showtimesStore = usePublicMovieShowtimesStore()
const { allShowtimes, isLoading, error } = storeToRefs(showtimesStore)
const { fetchShowtimes, resetStore } = showtimesStore

// =================================================================
// ===== 本地狀態 =====
// =================================================================
const selectedTheaterTypeId = ref(null)

// =================================================================
// ===== Computed (計算屬性) =====
// =================================================================

const shouldRenderComponent = computed(() => {
  // 載入中，需要顯示 v-loading，所以要渲染
  if (isLoading.value) {
    return true
  }
  // 載入完畢但有錯誤，需要顯示 el-alert，所以要渲染
  if (error.value) {
    return true
  }
  // 載入完畢且無錯誤，只有當有場次資料時才渲染
  return allShowtimes.value.length > 0
})

const theaterTypes = computed(() => {
  if (!allShowtimes.value) return []
  const types = allShowtimes.value.reduce((acc, showtime) => {
    if (!acc.some((t) => t.value === showtime.theaterTypeId)) {
      acc.push({
        value: showtime.theaterTypeId,
        label: showtime.theaterTypeName,
      })
    }
    return acc
  }, [])
  return types.sort((a, b) => a.value - b.value)
})

const groupedShowtimes = computed(() => {
  if (!selectedTheaterTypeId.value || allShowtimes.value.length === 0) {
    return {}
  }
  const filtered = allShowtimes.value.filter((s) => s.theaterTypeId === selectedTheaterTypeId.value)
  return filtered.reduce((groups, session) => {
    const date = session.startTime.split('T')[0]
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push({
      sessionId: session.sessionId,
      time: new Date(session.startTime).toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
      }),
      status: getSeatStatus(session.availableSeats, session.totalSeats),
    })
    groups[date].sort((a, b) => a.time.localeCompare(b.time))
    return groups
  }, {})
})

// =================================================================
// ===== Helper Functions (輔助函式) =====
// =================================================================
const getSeatStatus = (available, total) => {
  if (total === 0) return 'sold-out'
  if (available === 0) {
    return 'sold-out'
  }
  if (available / total < 0.5) {
    return 'limited'
  }
  return 'plentiful'
}

const formatDate = (dateString) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)
  const date = new Date(dateString)
  date.setHours(0, 0, 0, 0)

  if (date.getTime() === today.getTime()) {
    return '今天'
  }
  if (date.getTime() === tomorrow.getTime()) {
    return '明天'
  }

  const week = ['日', '一', '二', '三', '四', '五', '六']
  const dayOfWeek = week[date.getDay()]
  return `${dateString.substring(5)} (${dayOfWeek})`
}

// =================================================================
// ===== Methods =====
// =================================================================
function goToSeatSelection(session) {
  if (session.status === 'sold-out') {
    ElMessage({
      message: '此場次座位已售完',
      type: 'warning',
    })
    return
  }
  console.log(`準備跳轉至劃位頁面，場次 ID: ${session.sessionId}`)
  router.push({
    name: 'SeatSelectionPage',
    params: { sessionId: session.sessionId },
  })
}

// =================================================================
// ===== Lifecycle Hooks (生命週期鉤子) =====
// =================================================================
// 【還原】使用最單純的 onMounted
onMounted(async () => {
  // 因為每次 ID 變化都會觸發一次全新的掛載，
  // 所以在這裡直接獲取資料就是最正確的做法。
  await fetchShowtimes(props.movieId)

  // 獲取完畢後，如果新的電影有場次版本，自動選擇第一個
  if (theaterTypes.value.length > 0) {
    selectedTheaterTypeId.value = theaterTypes.value[0].value
  }
})

onUnmounted(() => {
  resetStore()
})
</script>

<style scoped>
/* 整體容器樣式 */
.showtimes-container {
  padding: 24px;
  background-color: #1a1a1a;
  border-radius: 12px;
  border: 1px solid #2d2d2d;
  margin-top: 20px;
  /* 設定一個最小高度，防止載入時容器因為沒內容而塌陷 */
  min-height: 250px;
}
/* 篩選區塊樣式 */
.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-label {
  font-size: 0.95rem;
  font-weight: 500;
  color: #d1d5db;
  margin-right: 12px;
}
/* 限制 el-select 的寬度 */
.version-select {
  width: 240px;
}
@media (max-width: 768px) {
  .version-select {
    width: 100%;
  }
}
:deep(.version-select .el-select__wrapper) {
  background-color: #1f2937;
  border: 1px solid #374151;
  border-radius: 8px;
  color: #e5e7eb;
}
:deep(.version-select:hover .el-select__wrapper) {
  border-color: #3b82f6;
  background-color: #111827;
}
:deep(.version-select .el-select__placeholder) {
  color: #9ca3af;
}
:deep(.version-select .el-input__wrapper) {
  padding: 6px 12px;
}
/* 日期群組樣式 */
.date-group {
  margin-bottom: 24px;
}
.date-group:last-child {
  margin-bottom: 0;
}
.date-header {
  font-size: 1.125rem;
  font-weight: 600;
  color: #f3f4f6;
  margin-bottom: 16px;
  padding-bottom: 4px;
  border-bottom: 1px solid #4b5563;
}
/* 單一場次的 column 樣式 */
.session-col {
  margin-bottom: 16px;
}
/* 場次時間外層容器的樣式 */
.session-time-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.session-time-wrapper:hover {
  background-color: #1f2937;
  transform: translateY(-2px);
}
.session-time-wrapper:hover .session-time {
  color: #93c5fd;
}
/* 場次時間文字 */
.session-time {
  font-size: 1.1rem;
  font-weight: 500;
  letter-spacing: 0.5px;
  color: #e5e7eb;
  transition: color 0.3s ease;
}
/* 座椅 icon 的樣式 */
.seat-icon {
  font-size: 24px;
  transition: color 0.3s ease;
}
.status-plentiful {
  color: #3b82f6;
}
.status-limited {
  color: #f97316;
}
.status-sold-out {
  color: #ef4444;
}
/* Element Plus 組件樣式覆蓋 (無變動) */
:deep(.el-select .el-input__wrapper) {
  background-color: #1f2937;
  border-radius: 8px;
  border: 1px solid #374151;
}
:deep(.el-select:hover .el-input__wrapper) {
  border-color: #3b82f6;
}
:deep(.el-select .el-input__inner) {
  color: #e5e7eb;
}
:deep(.el-empty__description) {
  color: #9ca3af;
  font-size: 0.95rem;
}
/* 為已售完的場次加上特定樣式 */
.session-time-wrapper.sold-out {
  cursor: not-allowed;
  opacity: 0.5;
}
/* 移除已售完場次的 hover 效果，避免誤導使用者 */
.session-time-wrapper.sold-out:hover {
  background-color: transparent;
  transform: none;
}
.session-time-wrapper.sold-out:hover .session-time {
  color: #e5e7eb;
}

/* 為 v-loading 轉圈圈動畫客製化深色主題樣式 */
:deep(.el-loading-mask) {
  background-color: rgba(26, 26, 26, 0.7); /* 半透明的深色遮罩，更有質感 */
  border-radius: 12px; /* 讓遮罩的圓角和容器一致 */
}
:deep(.el-loading-spinner .path) {
  stroke: #e5e7eb; /* 將轉圈圈的顏色改為淺灰色 */
}
</style>
