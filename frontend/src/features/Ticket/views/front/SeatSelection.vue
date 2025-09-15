<template>
  <div class="seat-selection-container">
    <div class="order-summary-bar">
      <div class="summary-section seats-info">
        <span class="summary-label">已選座位:</span>
        <div v-if="selectedSeats.length > 0" class="seats-tags">
          <div
            v-for="(chunk, index) in chunkedSelectedSeats"
            :key="`chunk-${index}`"
            class="seats-tags-row"
          >
            <span v-for="seat in chunk" :key="seat.sessionSeatId" class="seat-tag">
              {{ seat.seat.rowNumber }}排{{ seat.seat.columnNumber }}號
            </span>
          </div>
        </div>
        <div v-else class="empty-summary">尚未選擇任何座位</div>
      </div>
      <div class="summary-section progress-info">
        <div class="progress-bar">
          <div class="progress-bar-inner" :style="{ width: `${selectionProgress}%` }"></div>
        </div>
        <span :class="{ completed: isSelectionComplete }">
          {{ selectionMessage }} ({{ selectedSeats.length }} / {{ totalTicketCount }})
        </span>
      </div>
      <div class="summary-section actions">
        <button
          class="next-step-btn"
          :disabled="!isSelectionComplete || isCreatingReservation"
          @click="goToNextStep"
        >
          {{ isCreatingReservation ? '預訂中...' : '下一步' }}
        </button>
      </div>
    </div>

    <div class="seat-map-wrapper" ref="seatMapWrapperRef">
      <div
        v-if="seatsGrid.length > 0"
        class="seat-map-content"
        :style="{
          '--seat-size': `${dynamicSeatSize.size}px`,
          '--seat-gap': `${dynamicSeatSize.gap}px`,
          '--row-gap': `${dynamicSeatSize.rowGap}px`,
        }"
      >
        <div class="screen" :style="{ width: dynamicScreenWidth }">螢幕</div>
        <div class="seat-grid">
          <div v-for="(row, rowIndex) in seatsGrid" :key="rowIndex" class="seat-row">
            <span class="row-label">{{ getRowLabel(rowIndex) }}</span>
            <div
              v-for="(seat, colIndex) in row"
              :key="seat?.seat?.seatId || `empty-${rowIndex}-${colIndex}`"
              :class="getSeatClasses(seat)"
              @click="handleSeatSelect(seat)"
            >
              <template v-if="seat && seat.seat && seat.seat.seatType !== '走道'">
                <Icon :icon="getSeatIcon(seat.seat.seatType)" class="seat-icon" />
                <span class="seat-number">{{ seat.seat.columnNumber }}</span>
              </template>
            </div>
            <span class="row-label">{{ getRowLabel(rowIndex) }}</span>
          </div>

          <div class="col-labels-row">
            <span class="row-label"></span>
            <div
              v-for="colNumber in numberOfCols"
              :key="`col-label-${colNumber}`"
              class="col-label-cell"
            >
              {{ colNumber }}
            </div>
            <span class="row-label"></span>
          </div>
        </div>
        <div class="seat-legend">
          <div class="legend-item">
            <div class="seat-cell seat-status-selected">
              <Icon icon="ic:baseline-chair" class="seat-icon" />
            </div>
            <span>已選擇</span>
          </div>
          <div class="legend-item">
            <div class="seat-cell seat-status-available">
              <Icon icon="ic:baseline-chair" class="seat-icon" />
            </div>
            <span>可選座位</span>
          </div>
          <div class="legend-item">
            <div class="seat-cell seat-status-sold">
              <Icon icon="ic:baseline-chair" class="seat-icon" />
            </div>
            <span>已售出</span>
          </div>
          <div class="legend-item">
            <div class="seat-cell seat-status-available is-accessible">
              <Icon icon="mdi:wheelchair" class="seat-icon" />
            </div>
            <span class="legend-text">友善座位<br /><small>(請洽影城購買)</small></span>
          </div>
        </div>
      </div>
      <div v-else class="status-info">正在載入座位圖...</div>
    </div>
  </div>
</template>

<script setup>
// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'
import { storeToRefs } from 'pinia'
import { Icon } from '@iconify/vue'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const router = useRouter()
const route = useRoute()
const seatSelectionStore = usePublicSeatSelectionStore()

// ----------------------------------------------------------------
// Pinia Store 狀態管理 (State Management)
// ----------------------------------------------------------------
// 【修改】從 store 中多解構出 paymentMethod
const { sessionSeats, ticketOrder, selectedSeats, totalTicketCount, paymentMethod } =
  storeToRefs(seatSelectionStore)

// 【修改】解構出新的 action
const { toggleSeatSelection, createReservation } = seatSelectionStore

// ----------------------------------------------------------------
// 本地元件狀態 (Local Component State)
// ----------------------------------------------------------------
const seatMapWrapperRef = ref(null)
const seatMapWrapperWidth = ref(0)
let resizeObserver = null
// 【新增】用於控制按鈕載入狀態的 ref
const isCreatingReservation = ref(false)

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------

const seatsGrid = computed(() => {
  if (!sessionSeats.value || sessionSeats.value.length === 0) return []
  const grid = []
  let maxRow = 0,
    maxCol = 0
  sessionSeats.value.forEach((s) => {
    if (s && s.seat) {
      const rowIndex = s.seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0)
      const colIndex = s.seat.columnNumber - 1
      maxRow = Math.max(maxRow, rowIndex)
      maxCol = Math.max(maxCol, colIndex)
    }
  })
  for (let i = 0; i <= maxRow; i++) {
    grid.push(Array(maxCol + 1).fill(null))
  }
  sessionSeats.value.forEach((s) => {
    if (s && s.seat) {
      const rowIndex = s.seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0)
      const colIndex = s.seat.columnNumber - 1
      if (grid[rowIndex] !== undefined) {
        grid[rowIndex][colIndex] = s
      }
    }
  })
  return grid
})

const numberOfCols = computed(() => {
  return seatsGrid.value[0]?.length || 0
})

const dynamicSeatSize = computed(() => {
  if (sessionSeats.value.length === 0 || seatMapWrapperWidth.value === 0) {
    return { size: 28, gap: 5, rowGap: 8 }
  }
  const maxCol = sessionSeats.value.reduce((max, s) => Math.max(max, s.seat.columnNumber || 0), 0)
  const rowLabelWidth = 20
  const totalLabelsWidth = rowLabelWidth * 2
  const padding = 40
  const availableWidth = seatMapWrapperWidth.value - totalLabelsWidth - padding
  const gapRatio = 5 / 32
  let calculatedSize = availableWidth / (maxCol + (maxCol > 1 ? (maxCol - 1) * gapRatio : 0))
  calculatedSize = Math.max(14, Math.min(36, calculatedSize))
  return {
    size: Math.floor(calculatedSize),
    gap: Math.max(2, Math.floor(calculatedSize * gapRatio)),
    rowGap: Math.max(4, Math.floor(calculatedSize * gapRatio * 1.5)),
  }
})

const dynamicScreenWidth = computed(() => {
  if (numberOfCols.value === 0) return '90%'
  const totalSeatsWidth = numberOfCols.value * dynamicSeatSize.value.size
  const totalGapsWidth =
    numberOfCols.value > 1 ? (numberOfCols.value - 1) * dynamicSeatSize.value.gap : 0
  const totalWidth = totalSeatsWidth + totalGapsWidth
  return `${totalWidth}px`
})

const isSeatSelected = (seat) => {
  if (!seat) return false
  return selectedSeats.value.some((s) => s.sessionSeatId === seat.sessionSeatId)
}

const isSelectionComplete = computed(() => {
  return totalTicketCount.value > 0 && selectedSeats.value.length === totalTicketCount.value
})

const selectionMessage = computed(() => {
  if (totalTicketCount.value === 0) return '請先選擇票種'
  if (isSelectionComplete.value) return '座位已選滿'
  const remaining = totalTicketCount.value - selectedSeats.value.length
  return `請再選擇 ${remaining} 個座位`
})

const selectionProgress = computed(() => {
  if (totalTicketCount.value === 0) return 0
  return (selectedSeats.value.length / totalTicketCount.value) * 100
})

const sortedSelectedSeats = computed(() => {
  return [...selectedSeats.value].sort((a, b) => {
    const rowA = a.seat.rowNumber
    const rowB = b.seat.rowNumber
    const colA = a.seat.columnNumber
    const colB = b.seat.columnNumber
    if (rowA < rowB) return -1
    if (rowA > rowB) return 1
    return colA - colB
  })
})

const chunkedSelectedSeats = computed(() => {
  const chunks = []
  const seats = sortedSelectedSeats.value
  const chunkSize = 5
  for (let i = 0; i < seats.length; i += chunkSize) {
    chunks.push(seats.slice(i, i + chunkSize))
  }
  return chunks
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

function handleSeatSelect(seat) {
  if (
    !seat ||
    !seat.seat ||
    seat.status.toUpperCase() !== 'AVAILABLE' ||
    seat.seat.seatType === '友善座位'
  ) {
    if (seat && seat.seat.seatType === '友善座位') {
      console.log('友善座位請洽影城現場購買。')
    }
    return
  }
  toggleSeatSelection(seat)
}

function getSeatClasses(seat) {
  if (!seat || !seat.seat) return ['seat-cell', 'is-empty']
  return [
    'seat-cell',
    `seat-status-${seat.status.toLowerCase()}`,
    { 'is-accessible': seat.seat.seatType === '友善座位' },
    { selected: isSeatSelected(seat) },
    {
      selectable: seat.status.toUpperCase() === 'AVAILABLE' && seat.seat.seatType !== '友善座位',
    },
  ]
}

const getRowLabel = (index) => String.fromCharCode('A'.charCodeAt(0) + index)

const getSeatIcon = (seatType) => (seatType === '友善座位' ? 'mdi:wheelchair' : 'ic:baseline-chair')

/**
 * 【核心修改】處理「下一步」按鈕的點擊事件，在導航前呼叫後端 API
 */
async function goToNextStep() {
  if (!isSelectionComplete.value || isCreatingReservation.value) {
    // 如果座位未選滿，或正在建立訂單中，則不執行任何操作
    return
  }

  // 1. 進入載入狀態，防止使用者重複點擊
  isCreatingReservation.value = true

  try {
    // 2. 準備要發送到後端的 payload (請求主體)
    const payload = {
      sessionId: parseInt(route.params.sessionId),
      seatIds: selectedSeats.value.map((s) => s.sessionSeatId),
      paymentMethod: paymentMethod.value,
      // 將票券詳情轉換為後端 DTO 需要的格式
      tickets: ticketOrder.value.tickets.map((t) => ({
        ticketTypeId: t.ticketTypeId,
        quantity: t.quantity,
        unitPrice: t.price,
      })),
    }

    // 3. 呼叫 store 的 action 來建立臨時預訂
    const result = await createReservation(payload)

    if (result.success) {
      // 4. 如果後端成功建立預訂，才導航到確認頁面
      router.push({
        name: 'BookingConfirmation',
        params: { sessionId: route.params.sessionId },
        query: route.query,
      })
    } else {
      // 5. 如果後端失敗 (例如座位被搶走)，顯示後端傳來的錯誤訊息
      alert(result.message)
    }
  } catch (error) {
    // 這個 catch 主要捕捉網路問題或程式碼本身的錯誤
    console.error('goToNextStep 發生未知錯誤:', error)
    alert('發生未知錯誤，請重試。')
  } finally {
    // 6. 無論成功或失敗，最後都解除載入狀態
    isCreatingReservation.value = false
  }
}

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------

onMounted(() => {
  if (seatMapWrapperRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      if (entries[0]) {
        seatMapWrapperWidth.value = entries[0].contentRect.width
      }
    })
    resizeObserver.observe(seatMapWrapperRef.value)
  }
})

onBeforeUnmount(() => {
  if (resizeObserver && seatMapWrapperRef.value) {
    resizeObserver.unobserve(seatMapWrapperRef.value)
  }
})
</script>

<style scoped>
/* 整體容器 */
.seat-selection-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}
/* 上方訂單摘要列 */
.order-summary-bar {
  width: 100%;
  background-color: #2a2a2a;
  border: 1px solid #3c3c3c;
  border-radius: 8px;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  box-sizing: border-box;
}
.summary-section {
  display: flex;
  align-items: center;
  gap: 12px;
}
.summary-label {
  font-size: 14px;
  color: #aaa;
  flex-shrink: 0;
}
.seats-info {
  flex: 1.5;
  min-width: 0;
  align-items: center;
}
.progress-info {
  flex: 1;
  min-width: 0;
}
.actions {
  flex-shrink: 0;
  align-items: center;
}
.seats-tags {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}
.seats-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.seat-tag {
  background-color: #e67e22;
  color: #fff;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 13px;
  white-space: nowrap;
}
.empty-summary {
  font-size: 14px;
  color: #777;
}
.progress-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.progress-info span {
  font-size: 14px;
  font-weight: bold;
  color: #e6a23c;
}
.progress-info span.completed {
  color: #67c23a;
}
.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #444;
  border-radius: 3px;
}
.progress-bar-inner {
  height: 100%;
  background-color: #e50914;
  border-radius: 3px;
  transition: width 0.4s ease;
}
.next-step-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: bold;
  background-color: #e50914;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}
.next-step-btn:hover:not(:disabled) {
  background-color: #f40612;
}
.next-step-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
  opacity: 0.7;
}
.seat-map-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.seat-map-content {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background-color: #242424;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
}
.screen {
  height: 25px;
  background-color: #fff;
  color: #000;
  text-align: center;
  line-height: 25px;
  font-size: 1em;
  border-radius: 100px / 40px;
  border-bottom: 5px solid #ccc;
  box-shadow: 0 5px 20px rgba(255, 255, 255, 0.4);
  margin-bottom: 20px;
  transition: width 0.3s ease;
}
.seat-grid {
  display: flex;
  flex-direction: column;
  gap: var(--row-gap);
}
.seat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--seat-gap);
}
.row-label {
  font-weight: bold;
  color: #b8b9bb;
  width: 20px;
  text-align: center;
  user-select: none;
  flex-shrink: 0;
}
.seat-cell {
  width: var(--seat-size);
  height: var(--seat-size);
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
  border-radius: 4px;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
  position: relative;
}
.seat-icon {
  width: 85%;
  height: 85%;
}
.seat-number {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: calc(var(--seat-size) * 0.35);
  color: #fff;
  font-weight: bold;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.8);
  pointer-events: none;
}
.is-empty {
  background: transparent;
}
.seat-status-available .seat-icon {
  color: #617cc5;
}
.seat-status-available.selectable {
  cursor: pointer;
}
.seat-status-available.selectable:hover {
  background-color: rgba(97, 124, 197, 0.3);
  transform: scale(1.1);
}
.seat-status-sold .seat-icon,
.seat-status-reserved .seat-icon {
  color: #e50914;
}
.seat-status-sold,
.seat-status-reserved,
.is-accessible {
  cursor: not-allowed;
}
.is-accessible .seat-icon {
  color: #dcdcdc !important;
}
.seat-cell.selected {
  background-color: #e67e22;
  transform: scale(1.1);
  box-shadow: 0 0 10px rgba(230, 126, 34, 0.7);
}
.seat-cell.selected .seat-icon {
  color: #ffffff;
}
.seat-cell.selected .seat-number {
  color: #000000;
  text-shadow: none;
}
.seat-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px 24px;
  margin-top: 20px;
  color: #fff;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.legend-text small {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.legend-item .seat-cell {
  width: 20px;
  height: 20px;
}
.status-info {
  width: 100%;
  padding: 60px 20px;
  text-align: center;
  font-size: 18px;
  color: #aaa;
}
.col-labels-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--seat-gap);
  width: 100%;
  margin-top: calc(var(--row-gap) / 2);
  box-sizing: border-box;
}
.col-label-cell {
  color: #b8b9bb;
  font-weight: bold;
  font-size: calc(var(--seat-size) * 0.4);
  max-width: var(--seat-size);
  flex: 1 1 0;
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
}
</style>
