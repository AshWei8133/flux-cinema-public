<template>
  <div class="timeline-wrapper">
    <div class="zoom-controls">
      <div class="action-controls">
        <el-button-group v-if="hasChanges">
          <el-button type="primary" size="default" :icon="Check" @click="saveChanges">
            儲存變更
          </el-button>
          <el-button size="default" :icon="RefreshLeft" @click="resetChanges">重置變更</el-button>
        </el-button-group>
      </div>

      <el-button-group>
        <el-button :icon="Refresh" @click="resetZoom"></el-button>
        <el-button :icon="ZoomOut" @click="zoomOut" />
        <span class="zoom-level-text">{{ (zoomLevel * 100).toFixed(0) }}%</span>
        <el-button :icon="ZoomIn" @click="zoomIn" />
      </el-button-group>
    </div>

    <div class="gantt-grid">
      <div class="theater-names-area">
        <div class="corner-spacer"></div>

        <template v-for="group in props.theatersData" :key="group.id">
          <div class="theater-group-header" @click="emit('toggleCategory', group.id)">
            <span>{{ group.name }}</span>
            <el-icon class="arrow-icon" :class="{ 'is-expanded': group.isExpanded }">
              <ArrowDownBold />
            </el-icon>
          </div>
          <template v-if="group.isExpanded">
            <div v-for="theater in group.theaters" :key="theater.id" class="theater-row-header">
              <el-icon><Film /></el-icon>
              <span>{{ theater.name }}</span>
            </div>
          </template>
        </template>
      </div>

      <div class="timeline-area-wrapper" @wheel="handleWheel">
        <div v-if="resizeTooltip.visible" class="resize-tooltip" :style="resizeTooltip.style">
          {{ resizeTooltip.content }}
        </div>

        <div class="time-labels" :style="{ width: totalWidth + 'px' }">
          <div
            v-for="time in timeSlots"
            :key="time"
            class="time-slot"
            :style="{ minWidth: slotWidth + 'px' }"
          >
            {{ time }}
          </div>
        </div>

        <div
          class="grid-overlay"
          :style="{
            minWidth: totalWidth + 'px',
            height: pseudoElementHeight + 'px',
            '--slot-width': slotWidth + 'px', // 將 slotWidth 作為 CSS 變數傳入，供 background-image 使用
          }"
        ></div>

        <div class="theater-timelines" :style="{ minWidth: totalWidth + 'px' }">
          <template v-for="group in props.theatersData" :key="group.id">
            <div class="theater-timeline theater-timeline-group"></div>
            <template v-if="group.isExpanded">
              <div
                v-for="theater in group.theaters"
                :key="theater.id"
                class="theater-timeline"
                @dragover.prevent
                @drop="handleDrop($event, theater.id)"
              >
                <el-tooltip
                  v-for="schedule in getTheaterSchedules(theater.id)"
                  :key="schedule.sessionId"
                  :content="`${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}`"
                  placement="top"
                  effect="dark"
                  :show-arrow="false"
                  :disabled="
                    !isEditable || isResizing || draggedSchedule?.sessionId === schedule.sessionId
                  "
                  :offset="12"
                  popper-class="timeline-tooltip-popper"
                >
                  <div
                    class="movie-block"
                    :class="{
                      'read-only': !isEditable,
                      'is-resizing':
                        isResizing && resizingSchedule?.sessionId === schedule.sessionId,
                      'is-dragging': draggedSchedule?.sessionId === schedule.sessionId,
                    }"
                    :style="getMovieBlockStyle(schedule)"
                    :draggable="isEditable && !isResizing"
                    @dragstart="handleDragStart($event, schedule)"
                    @dragend="handleDragEnd"
                  >
                    <span class="movie-title">{{ getMovieTitle(schedule.movieId) }}</span>
                    <button
                      v-if="isEditable"
                      class="delete-btn"
                      @click.stop="emit('deleteSchedule', schedule)"
                    >
                      <el-icon><Close /></el-icon>
                    </button>
                    <div
                      v-if="isEditable"
                      class="resize-handle left"
                      @mousedown.stop="startResize($event, schedule, 'left')"
                    >
                      <el-icon class="handle-icon"><MoreFilled /></el-icon>
                    </div>
                    <div
                      v-if="isEditable"
                      class="resize-handle right"
                      @mousedown.stop="startResize($event, schedule, 'right')"
                    >
                      <el-icon class="handle-icon"><MoreFilled /></el-icon>
                    </div>
                  </div>
                </el-tooltip>
              </div>
            </template>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// ----------------------------------------------------------------
// 依賴引入
// ----------------------------------------------------------------
import { ref, computed, reactive } from 'vue'
import { ElButton, ElButtonGroup, ElTooltip, ElIcon } from 'element-plus'
import {
  ZoomIn,
  ZoomOut,
  Refresh,
  Film,
  MoreFilled,
  Close,
  ArrowDownBold,
  Check,
  RefreshLeft,
} from '@element-plus/icons-vue'
import { toLocalISOString, formatTime as formatTimeUtil } from '@/utils/dateUtils.js'

// ----------------------------------------------------------------
// Props & Emits 定義
// 這是此元件與父元件溝通的橋樑
// ----------------------------------------------------------------

/**
 * defineProps：定義此元件可以從父元件接收的屬性。
 * 就像一個機器的插槽，定義了可以插入哪些類型的零件。
 */
const props = defineProps({
  theatersData: { type: Array, required: true }, // 已分組的影廳資料
  scheduleData: { type: Array, required: true }, // 所有排程的資料
  movies: { type: Array, required: true }, // 所有電影的基礎資料 (用來查找片名、片長)
  isEditable: { type: Boolean, default: true }, // 是否為可編輯模式
  hasChanges: { type: Boolean, default: false }, // 排程是否有未儲存的變更
  selectedDate: { type: String, required: true }, // 當前選擇的日期
})

/**
 * defineEmits：定義此元件可以向父元件觸發的事件。
 * 就像機器的按鈕，按下後會通知外部系統發生了某件事。
 */
const emit = defineEmits([
  'updateSchedule', // 當排程被移動或調整大小後，觸發此事件，並帶上最新的完整排程資料
  'dropMovie', // 當一個新的電影被從外部拖入時，觸發此事件
  'deleteSchedule', // 當點擊刪除按鈕時，觸發此事件
  'toggleCategory', // 當點擊影廳類型標題時，觸發此事件
  'saveChanges', // 當點擊儲存按鈕時
  'resetChanges', // 當點擊重置按鈕時
])

// ----------------------------------------------------------------
// 事件處理方法 (直接觸發 emit)
// ----------------------------------------------------------------

/**
 * 點擊儲存按鈕時，向父元件發出 'saveChanges' 事件
 */
const saveChanges = () => {
  emit('saveChanges', props.scheduleData)
}

/**
 * 點擊重置按鈕時，向父元件發出 'resetChanges' 事件
 */
const resetChanges = () => {
  emit('resetChanges')
}

// ----------------------------------------------------------------
// 樣式與顏色配置
// ----------------------------------------------------------------
const movieColorPalette = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#1abc9c',
  '#3498db',
  '#9b59b6',
  '#34495e',
  '#e74c3c',
  '#2ecc71',
  '#f1c40f',
  '#8e44ad',
  '#c0392b',
  '#7f8c8d',
]

/**
 * 根據電影 ID 生成一個固定的顏色，讓同一部電影在時間軸上顏色總是一致
 * @param {number} movieId - 電影的 ID
 * @returns {string} 顏色碼
 */
const getMovieColor = (movieId) => {
  if (!movieId) {
    return '#CCCCCC' // 如果沒有 movieId，給一個預設灰色
  }
  // 使用取模運算，確保 movieId 總能對應到顏色陣列中的一個位置
  const index = movieId % movieColorPalette.length
  return movieColorPalette[index]
}

// ----------------------------------------------------------------
// 時間軸核心設定與狀態
// ----------------------------------------------------------------
const TIMELINE_START_HOUR = 6 // 時間軸從早上 6 點開始
const TIMELINE_END_HOUR = 27 // 時間軸到凌晨 3 點結束 (24 + 3 = 27)
const TOTAL_TIMELINE_HOURS = TIMELINE_END_HOUR - TIMELINE_START_HOUR // 時間軸總時長

const draggedSchedule = ref(null) // 存放當前正在被拖曳的排程物件

// --- 縮放相關狀態 ---
const baseWidth = 65 // 基礎縮放等級下，每小時的寬度 (px)
const zoomLevel = ref(1) // 當前的縮放等級 (1 = 100%)
const minZoom = 1 // 最小縮放等級
const maxZoom = 3 // 最大縮放等級
const slotWidth = computed(() => baseWidth * zoomLevel.value) // 根據縮放等級計算出的每小時實際寬度

// --- 縮放控制方法 ---
const zoomIn = () => {
  zoomLevel.value = Math.min(maxZoom, zoomLevel.value + 0.2)
}
const zoomOut = () => {
  zoomLevel.value = Math.max(minZoom, zoomLevel.value - 0.2)
}
const resetZoom = () => {
  zoomLevel.value = 1
}

/**
 * 處理滑鼠滾輪事件，實現 Ctrl + 滾輪縮放
 * @param {WheelEvent} e - 滾輪事件物件
 */
const handleWheel = (e) => {
  if (!e.ctrlKey) {
    // 必須按住 Ctrl 鍵才觸發
    return
  }
  e.preventDefault() // 防止頁面預設的滾動行為
  const scaleChange = e.deltaY * -0.01 // deltaY 是滾輪滾動的幅度，轉換為縮放比例
  let newZoomLevel = zoomLevel.value + scaleChange
  // 限制縮放等級在 minZoom 和 maxZoom 之間
  zoomLevel.value = Math.min(maxZoom, Math.max(minZoom, newZoomLevel))
}

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// 這些屬性會根據依賴的狀態自動更新，非常適合用來計算動態的佈局
// ----------------------------------------------------------------

// 計算頂部時間標尺需要顯示的所有時間點
const timeSlots = computed(() => {
  const slots = []
  for (let i = TIMELINE_START_HOUR; i < TIMELINE_END_HOUR; i++) {
    const displayHour = i % 24 // 處理跨天的情況 (e.g., 25 點顯示為 01:00)
    slots.push(`${displayHour.toString().padStart(2, '0')}:00`)
  }
  return slots
})

// 計算時間軸的總寬度
const totalWidth = computed(() => timeSlots.value.length * slotWidth.value)

// 計算當前可見的總行數 (包括影廳類型標題和展開的影廳)
const visibleRowCount = computed(() => {
  return props.theatersData.reduce((count, group) => {
    // 每個 group 自身佔 1 行，如果展開了，再加上其下的影廳數量
    return count + 1 + (group.isExpanded ? group.theaters.length : 0)
  }, 0)
})

// 計算背景格線覆蓋層的總高度
const pseudoElementHeight = computed(() => visibleRowCount.value * 40) // 假設每行高度為 40px

// ----------------------------------------------------------------
// 工具/輔助方法
// ----------------------------------------------------------------

// 根據影廳 ID 篩選出屬於該影廳的所有排程
const getTheaterSchedules = (theaterId) =>
  props.scheduleData.filter((s) => s.theaterId === theaterId)

// 根據電影 ID 查找電影標題
const getMovieTitle = (movieId) => {
  const movie = props.movies.find((m) => m.id === movieId)
  return movie ? movie.titleLocal : '未知電影'
}

/**
 * 計算電影色塊 (movie-block) 的動態樣式
 * 這是此元件最核心的計算之一，決定了排程在時間軸上的位置和長度
 * @param {object} schedule - 單一排程物件
 * @returns {object} CSS 樣式物件
 */
const getMovieBlockStyle = (schedule) => {
  const start = new Date(schedule.startTime)
  const end = new Date(schedule.endTime)
  const minutesPerHour = 60

  // 1. 計算開始時間距離時間軸起點 (TIMELINE_START_HOUR) 有多少分鐘
  let startMinutes = start.getHours() * minutesPerHour + start.getMinutes()
  if (start.getHours() < TIMELINE_START_HOUR) {
    // 處理跨天場次 (如 01:00)
    startMinutes += 24 * minutesPerHour
  }
  startMinutes -= TIMELINE_START_HOUR * minutesPerHour

  // 2. 計算電影總片長 (分鐘)
  const durationMinutes = (end.getTime() - start.getTime()) / (1000 * 60)

  // 3. 根據每小時的像素寬度 (slotWidth)，換算出 left 和 width 的像素值
  const leftPx = (startMinutes / minutesPerHour) * slotWidth.value
  const widthPx = (durationMinutes / minutesPerHour) * slotWidth.value

  const backgroundColor = getMovieColor(schedule.movieId)

  // 4. 根據寬度動態調整字體大小，讓較短的色塊字體小一點
  const minFontSize = 10
  const maxFontSize = 14
  const minWidthForMaxFont = 100 // 色塊寬度達到 100px 時，使用最大字體
  const finalFontSize = Math.min(
    maxFontSize,
    Math.max(minFontSize, (widthPx / minWidthForMaxFont) * maxFontSize),
  )

  return {
    left: `${leftPx}px`,
    width: `${widthPx}px`,
    backgroundColor: backgroundColor,
    'font-size': `${finalFontSize}px`,
  }
}

// 格式化時間，方便顯示
const formatTime = (dateString) => formatTimeUtil(dateString)

// ----------------------------------------------------------------
// 調整大小 (Resize) 功能的相關狀態與方法
// ----------------------------------------------------------------
const isResizing = ref(false) // 是否正在調整大小
const resizingSchedule = ref(null) // 正在被調整大小的排程物件
const resizeDirection = ref('') // 調整的方向 ('left' or 'right')
const resizeTooltip = reactive({
  // 跟隨滑鼠的時間提示框的狀態
  visible: false,
  content: '',
  style: { top: '0px', left: '0px' },
})

/**
 * 開始調整大小
 * @param {MouseEvent} e - 滑鼠事件
 * @param {object} schedule - 要被調整的排程
 * @param {string} direction - 調整方向
 */
const startResize = (e, schedule, direction) => {
  if (!props.isEditable) return
  e.preventDefault()
  isResizing.value = true
  resizingSchedule.value = { ...schedule } // 複製一份，避免直接修改原始資料
  resizeDirection.value = direction
  resizeTooltip.visible = true
  // 在 document 上註冊事件，確保滑鼠移出元素也能繼續拖曳
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize, { once: true }) // once: true 表示事件觸發一次後自動移除
}

/**
 * 處理調整大小過程中的滑鼠移動
 * @param {MouseEvent} e - 滑鼠事件
 */
const handleResize = (e) => {
  if (!isResizing.value) return
  // 使用 requestAnimationFrame 優化效能，避免過於頻繁的計算和重繪
  requestAnimationFrame(() => {
    // 1. 獲取滑鼠在時間軸容器內的相對 X 座標
    const timelineWrapper = e.target.closest('.timeline-area-wrapper')
    if (!timelineWrapper) return
    const rect = timelineWrapper.getBoundingClientRect()
    const scrollLeft = timelineWrapper.scrollLeft
    const mouseX = e.clientX - rect.left + scrollLeft

    // 2. 根據 X 座標計算出對應的時間點
    const pixelsPerMinute = slotWidth.value / 60
    const totalMinutesFromStart = mouseX / pixelsPerMinute
    const snappedMinutes = Math.round(totalMinutesFromStart / 5) * 5 // 每 5 分鐘吸附一次
    let newTime = new Date(
      new Date(`${props.selectedDate}T00:00:00`).getTime() +
        TIMELINE_START_HOUR * 3600 * 1000 +
        snappedMinutes * 60 * 1000,
    )

    // 3. 根據調整的方向（左或右），更新排程的 startTime 或 endTime
    const originalScheduleIndex = props.scheduleData.findIndex(
      (s) => s.sessionId === resizingSchedule.value.sessionId,
    )
    if (originalScheduleIndex === -1) return
    const newSchedules = [...props.scheduleData]
    const scheduleToUpdate = { ...newSchedules[originalScheduleIndex] }
    const movie = props.movies.find((m) => m.id === scheduleToUpdate.movieId)
    if (!movie) return
    const minDurationMinutes = movie.durationMinutes

    const originalStartTime = new Date(scheduleToUpdate.startTime)
    const originalEndTime = new Date(scheduleToUpdate.endTime)

    if (resizeDirection.value === 'left') {
      const maxStartTime = new Date(originalEndTime.getTime() - minDurationMinutes * 60 * 1000)
      if (newTime > maxStartTime) newTime = maxStartTime // 不能晚於 (結束時間 - 最小片長)
      scheduleToUpdate.startTime = toLocalISOString(newTime)
    } else {
      // 'right'
      const minEndTime = new Date(originalStartTime.getTime() + minDurationMinutes * 60 * 1000)
      if (newTime < minEndTime) newTime = minEndTime // 不能早於 (開始時間 + 最小片長)
      scheduleToUpdate.endTime = toLocalISOString(newTime)
    }

    // 4. 更新提示框的位置和內容
    const mouseY = e.clientY - rect.top
    resizeTooltip.style.top = `${mouseY - 35}px`
    resizeTooltip.style.left = `${mouseX}px`
    resizeTooltip.content = formatTime(newTime)

    // 5. 觸發 updateSchedule 事件，將更新後的排程資料傳給父元件
    newSchedules[originalScheduleIndex] = scheduleToUpdate
    emit('updateSchedule', newSchedules)
  })
}

/**
 * 停止調整大小
 */
const stopResize = () => {
  isResizing.value = false
  resizingSchedule.value = null
  resizeTooltip.visible = false
  document.removeEventListener('mousemove', handleResize)
}

// ----------------------------------------------------------------
// 拖放 (Drag & Drop) 功能的相關狀態與方法
// ----------------------------------------------------------------
/**
 * 開始拖曳一個已有的排程
 * @param {DragEvent} e
 * @param {object} schedule
 */
const handleDragStart = (e, schedule) => {
  if (!props.isEditable) return
  const dragOffsetX = e.offsetX // 記錄滑鼠在色塊內的 X 偏移量
  const dataToTransfer = {
    ...schedule,
    isSchedule: true, // 標記這是時間軸上的排程，而非外部電影
    dragOffsetX: dragOffsetX,
    isCopy: e.ctrlKey, // 如果按住 Ctrl 拖曳，則為複製模式
  }

  e.dataTransfer.setData('application/json', JSON.stringify(dataToTransfer))
  e.dataTransfer.effectAllowed = 'move'
  draggedSchedule.value = schedule // 記錄正在拖曳的排程
  document.addEventListener('dragover', handleAutoScroll) // 啟用邊緣自動滾動
}

/**
 * 拖曳結束 (無論成功或取消)
 */
const handleDragEnd = () => {
  draggedSchedule.value = null
  document.removeEventListener('dragover', handleAutoScroll)
  stopScroll() // 停止自動滾動
}

/**
 * 處理拖放結束事件 (drop)。
 * 這是最複雜的邏輯，需要區分是從外部拖入新電影，還是移動/複製時間軸上已有的排程。
 * @param {DragEvent} e
 * @param {number} theaterId
 */
const handleDrop = (e, theaterId) => {
  if (!props.isEditable) return
  stopScroll()
  const droppedData = e.dataTransfer.getData('application/json')
  if (!droppedData) return

  try {
    const droppedItem = JSON.parse(droppedData)

    // 1. 計算放置點在時間軸上的時間
    const timelineAreaWrapper = e.target.closest('.timeline-area-wrapper')
    if (!timelineAreaWrapper) return
    const rect = timelineAreaWrapper.getBoundingClientRect()
    const scrollLeft = timelineAreaWrapper.scrollLeft
    const dropX = e.clientX - rect.left + scrollLeft
    const dragOffsetX = droppedItem.dragOffsetX || 0
    const adjustedDropX = dropX - dragOffsetX
    const finalDropX = Math.max(0, adjustedDropX)
    const minutesFromStart = (finalDropX / totalWidth.value) * (TOTAL_TIMELINE_HOURS * 60)
    const snappedMinutesFromStart = Math.round(minutesFromStart / 5) * 5

    const baseDate = new Date(`${props.selectedDate}T00:00:00`)
    baseDate.setHours(TIMELINE_START_HOUR, 0, 0, 0)
    const startTime = new Date(baseDate.getTime() + snappedMinutesFromStart * 60 * 1000)

    // 2. 根據拖曳的資料類型進行不同處理
    if (droppedItem.isSchedule) {
      // 如果是移動/複製已有的排程
      const durationMinutes =
        (new Date(droppedItem.endTime) - new Date(droppedItem.startTime)) / (1000 * 60)
      const endTime = new Date(startTime.getTime() + durationMinutes * 60 * 1000)

      if (droppedItem.isCopy) {
        // 複製模式
        const newSchedule = {
          sessionId: Date.now(), // 產生臨時 ID
          movieId: droppedItem.movieId,
          theaterId: theaterId,
          startTime: toLocalISOString(startTime),
          endTime: toLocalISOString(endTime),
        }
        emit('updateSchedule', [...props.scheduleData, newSchedule])
      } else {
        // 移動模式
        const updatedSchedules = props.scheduleData.map((s) =>
          s.sessionId === droppedItem.sessionId
            ? {
                ...s,
                theaterId,
                startTime: toLocalISOString(startTime),
                endTime: toLocalISOString(endTime),
              }
            : s,
        )
        emit('updateSchedule', updatedSchedules)
      }
    } else {
      // 如果是從外部拖入新電影
      emit('dropMovie', {
        movieId: droppedItem.id,
        theaterId: theaterId,
        dropTime: toLocalISOString(startTime),
      })
    }
  } catch (error) {
    console.warn('拖曳的資料格式有誤:', error)
  }
}

// ----------------------------------------------------------------
// 拖曳時邊緣自動滾動功能
// ----------------------------------------------------------------
let scrollInterval = null
const scrollSpeed = 15 // 滾動速度
const scrollThreshold = 50 // 觸發滾動的邊緣寬度 (px)

const startScroll = (element, direction) => {
  if (scrollInterval) return // 如果已經在滾動，則不重複啟動
  scrollInterval = setInterval(() => {
    if (direction === 'left') element.scrollLeft -= scrollSpeed
    else if (direction === 'right') element.scrollLeft += scrollSpeed
  }, 20)
}

const stopScroll = () => {
  if (scrollInterval) {
    clearInterval(scrollInterval)
    scrollInterval = null
  }
}

const handleAutoScroll = (e) => {
  const timelineWrapper = document.querySelector('.timeline-area-wrapper')
  if (!timelineWrapper) {
    stopScroll()
    return
  }
  const rect = timelineWrapper.getBoundingClientRect()
  if (e.clientX < rect.left + scrollThreshold) {
    // 滑鼠在左邊緣
    startScroll(timelineWrapper, 'left')
  } else if (e.clientX > rect.right - scrollThreshold) {
    // 滑鼠在右邊緣
    startScroll(timelineWrapper, 'right')
  } else {
    // 滑鼠在中間區域
    stopScroll()
  }
}
</script>

<style scoped>
/* `scoped` 屬性確保這裡的 CSS 只會應用於這個元件，不會影響到其他元件 */

.action-controls {
  min-width: 170px;
  display: flex;
  justify-content: flex-start;
}
.theater-group-header {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  font-weight: bold;
  background-color: #e9ebee;
  border-bottom: 1px solid #dcdfe6;
  border-top: 1px solid #dcdfe6;
  cursor: pointer;
  user-select: none; /* 防止點擊時選中文本 */
  margin-top: -1px; /* 讓相鄰的 border-top 和 border-bottom 重疊，看起來是 1px */
}
.theater-group-header:hover {
  background-color: #e4e7ed;
}
.arrow-icon {
  transition: transform 0.3s ease;
  transform: rotate(-90deg); /* 預設收合是 -90 度 */
}
.arrow-icon.is-expanded {
  transform: rotate(0deg); /* 展開時轉回來 */
}
.theater-row-header {
  padding-left: 12px;
  justify-content: flex-start;
}
.theater-timeline-group {
  background-color: #e9ebee;
  border-bottom: 1px solid #dcdfe6;
  border-top: 1px solid #dcdfe6;
  margin-top: -1px;
}
/* 使用 CSS 變數，方便統一管理顏色和樣式 */
/* :root {
  
} */
.timeline-wrapper {
  --el-border-color-lighter: #e4e7ed;
  --el-bg-color-page: #faf7f7e8;
  --el-bg-color-overlay: #f0f2f5;
  --el-text-color-secondary: #666;
  --el-box-shadow-light: 0 2px 5px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--el-bg-color-page);
}
.zoom-controls {
  display: flex;
  align-items: center;
  padding: 8px;
  background-color: #e9e9e9;
  border-bottom: 1px solid var(--el-border-color-lighter);
  justify-content: space-between;
}

.zoom-controls .el-button-group {
  display: flex;
  align-items: center;
}

.zoom-level-text {
  font-weight: bold;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  width: 50px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.gantt-grid {
  display: flex; /* 左右佈局的關鍵 */
}
.theater-names-area {
  flex-shrink: 0; /* 防止左側區域被壓縮 */
  width: 150px; /* 左側固定寬度 */
  border-right: 1px solid var(--el-border-color-lighter);
}
.corner-spacer {
  height: 40px; /* 與時間標尺等高 */
  background-color: var(--el-bg-color-overlay);
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.theater-row-header {
  height: 40px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
.timeline-area-wrapper {
  flex: 1; /* 佔據剩餘所有寬度 */
  overflow-x: auto; /* 核心！讓時間軸可以橫向滾動 */
  position: relative; /* 作為內部絕對定位元素的基準 */
}
.time-labels {
  display: flex;
  height: 40px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  position: sticky; /* 關鍵！讓時間標尺在滾動時固定在頂部 */
  top: 0;
  z-index: 20;
  background-color: var(--el-bg-color-overlay);
}
.time-slot {
  line-height: 40px;
  font-size: 14px;
  font-weight: bold;
  color: var(--el-text-color-secondary);
  flex-shrink: 0; /* 防止被壓縮 */
  box-sizing: border-box;
}
.theater-timelines {
  position: relative;
  display: flex;
  flex-direction: column;
}
.theater-timeline {
  position: relative; /* 作為電影色塊絕對定位的基準 */
  height: 40px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
/* 背景格線的實現 */
.grid-overlay {
  position: absolute;
  top: 40px;
  left: 0;
  z-index: 15;
  pointer-events: none; /* 讓滑鼠事件可以穿透此層 */
  /* 使用重複線性漸層畫出垂直線，比 DOM 元素高效 */
  background-image: repeating-linear-gradient(
    to right,
    var(--el-border-color-lighter) 0,
    var(--el-border-color-lighter) 1px,
    transparent 1px,
    transparent var(--slot-width) /* 線的間距由 JS 傳入的 CSS 變數控制 */
  );
}
.movie-block {
  position: absolute; /* 相對於 theater-timeline 定位 */
  top: 5px;
  height: calc(100% - 10px);
  border-radius: 4px;
  opacity: 0.9;
  cursor: move;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px;
  box-shadow: var(--el-box-shadow-light);
  user-select: none;
  transition:
    box-shadow 0.2s ease,
    opacity 0.2s ease;
  box-sizing: border-box;
}
.movie-block:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}
.movie-block.is-resizing {
  transition: none; /* 調整大小時取消動畫，避免卡頓 */
}
.movie-block.is-dragging {
  opacity: 0.4; /* 拖曳時變半透明 */
  box-shadow: none;
}
.movie-title {
  color: #fff;
  font-weight: bold;
  white-space: nowrap; /* 強制不換行 */
  overflow: hidden; /* 超出部分隱藏 */
  text-overflow: ellipsis; /* 超出部分顯示省略號 */
  padding-right: 10px; /* 避免文字和刪除按鈕重疊 */
}
.movie-block.read-only {
  cursor: default;
  pointer-events: none; /* 不可編輯時，禁用所有滑鼠事件 */
  opacity: 0.6;
}
.delete-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  background-color: rgba(0, 0, 0, 0.4);
  color: white;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  visibility: hidden; /* 平時隱藏 */
  opacity: 0;
  transition:
    opacity 0.2s ease,
    background-color 0.2s ease;
  z-index: 25;
}
.delete-btn:hover {
  background-color: rgba(245, 108, 108, 0.8);
}
/* 滑鼠懸停在電影色塊上時，顯示刪除按鈕 */
.movie-block:hover .delete-btn {
  visibility: visible;
  opacity: 1;
}
.delete-btn .el-icon {
  font-size: 10px;
}
.resize-handle {
  position: absolute;
  top: 0;
  height: 100%;
  width: 10px;
  z-index: 20;
  cursor: ew-resize; /* 顯示左右拖曳的游標 */
  display: flex;
  align-items: center;
  justify-content: center;
}
.resize-handle.left {
  left: 0;
}
.resize-handle.right {
  right: 0;
}
.resize-handle .handle-icon {
  visibility: hidden; /* 平時隱藏圖示 */
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}
/* 滑鼠懸停在電影色塊上時，顯示調整大小的圖示 */
.movie-block:hover .resize-handle .handle-icon {
  visibility: visible;
}
/* 調整大小期間顯示的時間提示工具 */
.resize-tooltip {
  position: absolute;
  background-color: #303133;
  color: white;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 3000;
  pointer-events: none;
  transform: translateX(-50%); /* 讓提示框的中心對準滑鼠 */
  white-space: nowrap;
}
/* :global() 選擇器可以穿透 scoped 的限制，去修改全域的 class */
/* 這裡用來微調 Element Plus Tooltip 的樣式 */
:global(.timeline-tooltip-popper) {
  padding: 6px 10px !important;
  font-size: 12px !important;
  line-height: 1.2 !important;
}
</style>
