<!-- 定義日曆單元格的呈現內容 -->
<template>
  <!-- 定義提示框的行為和外觀 -->
  <el-popover
    :width="600"
    placement="right"
    trigger="hover"
    :show-after="500"
    popper-class="schedule-popover"
    :disabled="data.type !== 'current-month'"
    :popper-options="{
      modifiers: [
        { name: 'preventOverflow', options: { boundary: 'viewport' } },
        { name: 'flip', options: { fallbackPlacements: ['left', 'bottom', 'top'] } },
      ],
    }"
  >
    <!-- 定義提示框中的內容 -->
    <template #default>
      <!-- 整個內容的容器 -->
      <div class="popover-content">
        <!-- 顯示排片表的標題，會動態顯示當天的日期 -->
        <p class="popover-title">{{ getChineseDate(data.date) }} 排片預覽</p>
        <!-- 排片表的內容區域 -->
        <div class="schedule-grid">
          <!-- 判斷今日是否有場次，若無則顯示今日無排片 -->
          <template v-if="Object.keys(getSessions(data.date)).length > 0">
            <!-- 遍歷所有電影院類型（例如「2D廳」、「IMAX廳」），然後在每個類型下，再遍歷每個電影院的排片 -->
            <div
              v-for="(theaters, typeName) in getSessions(data.date)"
              :key="typeName"
              class="theater-type-group"
            >
              <!-- 每個影廳類型都有一個標題，點擊觸發 toggleTheaterGroup(typeName) 可以收合所屬影聽-->
              <h4 class="theater-type-title" @click="toggleTheaterGroup(typeName)">
                <span :class="['toggle-icon', { expanded: isExpanded(typeName) }]">
                  <Icon icon="mynaui:chevron-right-solid" />
                </span>
                <span class="theater-type-name">{{ typeName }}</span>
              </h4>
              <!-- 為展開和收合的動畫增加平滑的過渡效果 -->
              <transition name="fade-in-down">
                <div v-if="isExpanded(typeName)">
                  <div class="theater-list-header">
                    <span class="header-name-placeholder"></span>
                    <div class="header-time-axis">
                      <span class="header-time">早</span>
                      <span class="header-time">午</span>
                      <span class="header-time">晚</span>
                    </div>
                  </div>
                  <!-- 遍歷每個影廳（theaterName）的詳細場次（sessions） -->
                  <div
                    v-for="(sessions, theaterName) in theaters"
                    :key="theaterName"
                    class="theater-row"
                  >
                    <div class="theater-name">{{ theaterName }}</div>
                    <div class="schedule-bar-wrapper">
                      <!-- 排片時間軸樣式 -->
                      <div class="schedule-bar">
                        <!-- 分隔線區分早、午、晚 -->
                        <div class="divider-line first"></div>
                        <div class="divider-line middle"></div>
                        <template v-if="sessions.length > 0">
                          <!-- 最內層的迴圈，用來遍歷每個電影場次 -->
                          <!-- 
                            每個 <div> 代表一部電影，根據電影的開始和結束時間，
                            動態計算電影塊的 left 位置和 width 寬度，並用 CSS 變數的方式應用在 :style 上 
                            :title : 在滑鼠懸停在電影塊上時，顯示一個瀏覽器自帶的提示框，方便使用者快速查看電影名稱和時間
                          -->
                          <div
                            v-for="(session, sessionIndex) in sessions"
                            :key="sessionIndex"
                            class="movie-block"
                            :style="getMovieBlockStyle(session.startTime, session.endTime)"
                            :title="`片名: ${session.movieTitle}\n時間: ${session.startTime} - ${session.endTime}`"
                          >
                            <span class="movie-title-short">{{
                              getShortenedTitle(session.movieTitle)
                            }}</span>
                          </div>
                        </template>
                        <div v-else class="no-schedule-block">今日無排片</div>
                      </div>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </template>
          <div v-else class="no-schedule-overall">今日無排片</div>
        </div>
      </div>
    </template>

    <!-- 日曆單元格內容 -->
    <template #reference>
      <!-- 日曆單元格的容器，點選觸發導航至排片編輯 -->
      <div class="calendar-cell" @click="handleClickCell">
        <!-- 顯示當天的日期數字 -->
        <p class="date-number">
          {{ data.date.getDate() }}
        </p>
        <!-- 
          動態顯示的標籤，用來標示當天的狀態，
          這裡會根據當天的狀態，動態地套用不同的 CSS class，讓標籤顯示不同的顏色或樣式 
        -->
        <div
          v-if="getCurrentDateStatus && getCurrentDateStatus !== 'HISTORY'"
          :class="['status-tag', getCurrentDateStatus.toLowerCase()]"
        >
          {{ getStatusText(getCurrentDateStatus) }}
        </div>
      </div>
    </template>
  </el-popover>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElPopover } from 'element-plus'
import { Icon } from '@iconify/vue'

// 父元件傳入資料
const props = defineProps({
  // 日曆單元格的物件【基本資訊】(date、type)
  data: {
    type: Object,
    required: true,
  },
  // 儲存日期的排片狀態
  dateStatus: {
    type: Object,
    required: true,
  },
  // 取得場次概況資料方法
  getSessions: {
    type: Function,
    required: true,
  },
  // 格式化日期為中文的方法
  getChineseDate: {
    type: Function,
    required: true,
  },
  //  將日期狀態轉換為中文文字的方法
  getStatusText: {
    type: Function,
    required: true,
  },
  // 判斷某個日期是否有排片狀態
  dateHasSchedule: {
    type: Function,
    required: true,
  },
  // 取得日曆單元格排片狀態的方法
  cellClasses: {
    type: Function,
    required: true,
  },
})

// emit
// 讓父組件可以透過點選日期單元格，定義事件
const emits = defineEmits(['date-click'])

// 數據
// 儲存所有目前處於展開狀態的電影院類型名稱。
const expandedTheaters = ref(new Set())

// 計算屬性
// 取得當前日期的狀態
const getCurrentDateStatus = computed(() => {
  const formattedDate = `${props.data.date.getFullYear()}-${(props.data.date.getMonth() + 1)
    .toString()
    .padStart(2, '0')}-${props.data.date.getDate().toString().padStart(2, '0')}`
  return props.dateStatus[formattedDate]
})

// 方法
// 判斷給定的電影院類型 typeName 是否處於展開狀態
const isExpanded = (typeName) => {
  return expandedTheaters.value.has(typeName)
}

// 切換電影院類型群組的展開或收合狀態
const toggleTheaterGroup = (typeName) => {
  if (isExpanded(typeName)) {
    expandedTheaters.value.delete(typeName)
  } else {
    expandedTheaters.value.add(typeName)
  }
}

// 處理使用者點擊日曆單元格的事件
const handleClickCell = () => {
  const status = getCurrentDateStatus.value

  // 如果狀態是 'NONE'，則直接返回，不執行任何操作(不跳轉路由)
  if (status === 'NONE') {
    console.log('此日期無排片，點擊無效。')
    return
  }

  const isHistoryDate = status === 'HISTORY'
  if (props.data.type === 'current-month') {
    emits('date-click', props.data.date, isHistoryDate)
  }
}

// 將電影名稱縮短為第一個字
const getShortenedTitle = (title = '') => title.trim().charAt(0) || ''

// 這些是常數，用來定義排片時間軸的範圍和總長度。24 + 2 表示從凌晨兩點開始
const START_HOUR = 6
const END_HOUR = 24 + 2
const TOTAL_MINUTES = (END_HOUR - START_HOUR) * 60

// 將 'HH:mm' 格式的字串轉換為從 START_HOUR 開始計算的總分鐘數
const timeToMinutes = (timeStr) => {
  const [hours, minutes] = timeStr.split(':').map(Number)
  const totalMinutes = (hours - START_HOUR) * 60 + minutes
  return hours < START_HOUR ? totalMinutes + 24 * 60 : totalMinutes
}

// 更新電影色塊的配色方案(早、午、晚)
const TIME_COLOR_MAP = [
  { start: 6, end: 12, bg: '#bbdefb', text: '#1976d2', border: '#64b5f6' }, // 淺藍
  { start: 12, end: 18, bg: '#c8e6c9', text: '#388e3c', border: '#81c784' }, // 淺綠
  { start: 18, end: 30, bg: '#e1bee7', text: '#7b1fa2', border: '#ba68c8' }, // 淺紫
]

// 根據電影的開始時間，從 TIME_COLOR_MAP 中找到對應的顏色並回傳
const getMovieBlockColor = (startTime) => {
  const [hours] = startTime.split(':').map(Number)
  const target = TIME_COLOR_MAP.find(({ start, end }) => hours >= start && hours < end)
  return {
    backgroundColor: target?.bg ?? '#f5f5f5',
    color: target?.text ?? '#424242',
    borderColor: target?.border ?? '#ccc',
  }
}

/**
 * 根據電影的開始和結束時間，計算出電影塊在時間軸上的left 位置和width 寬度，
 * 並回傳一個包含 CSS 樣式的物件。這是實現排片視覺化效果的底層邏輯
 */
const getMovieBlockStyle = (startTime, endTime) => {
  const startMinutes = timeToMinutes(startTime)
  const endMinutes = timeToMinutes(endTime)
  const leftPercentage = (startMinutes / TOTAL_MINUTES) * 100
  const widthPercentage = ((endMinutes - startMinutes) / TOTAL_MINUTES) * 100
  const colors = getMovieBlockColor(startTime)
  return {
    left: `${leftPercentage}%`,
    width: `${widthPercentage}%`,
    ...colors,
  }
}
</script>

<style scoped>
@import '@/styles/moviesessions/_calendar-popover.css';
/* 這裡只保留日曆單元格自身的樣式 */
.calendar-cell {
  height: 100%;
  padding: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  transition: background-color 0.2s;
  position: relative;
  box-sizing: border-box;
}
.date-number {
  margin: 0;
  align-self: flex-end;
  font-size: 50%;
}
.date-number.is-history-number {
  color: #303133 !important;
  font-weight: normal;
  cursor: pointer !important;
}
.status-tag {
  align-self: center;
  margin-top: 5px;
  font-size: 12px;
  color: white;
  border-radius: 4px;
  padding: 2px 6px;
  white-space: nowrap;
}
.completed {
  background-color: #67c23a;
}
.pending {
  background-color: #e6a23c;
}
.today {
  background-color: #c8c100;
}
.none {
  display: none;
}
</style>
