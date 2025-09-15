<template>
  <div class="seat-editor-layout">
    <div class="seat-editor-controls">
      <el-form :inline="true" class="controls-form">
        <el-form-item label="總排數">
          <el-input-number v-model="rows" :min="0" :max="26" />
        </el-form-item>
        <el-form-item label="總列數">
          <el-input-number v-model="cols" :min="0" />
        </el-form-item>
        <el-form-item label="座位設定">
          <el-radio-group v-model="currentDrawMode">
            <el-radio-button value="一般座位">一般座位</el-radio-button>
            <el-radio-button value="友善座位">友善座位</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="resetSeats">重新生成座位</el-button>
        </el-form-item>
      </el-form>
      <div class="tip-text">操作提示：左鍵點擊為座位設定，右鍵點擊為走道。</div>
    </div>

    <div
      class="seat-map-container"
      v-if="seatsGrid.length > 0"
      ref="seatMapRef"
      :style="{
        '--seat-size': seatSize + 'px',
        '--seat-gap': seatGap + 'px',
        '--row-gap': rowGap + 'px',
        '--row-label-width': rowLabelWidth + 'px',
      }"
      @contextmenu.prevent
    >
      <div class="screen">螢幕</div>
      <div class="seat-grid">
        <div v-for="(row, rowIndex) in seatsGrid" :key="rowIndex" class="seat-row">
          <span
            class="row-label"
            @click="setRowNormal(rowIndex)"
            @contextmenu.prevent="setRowAisle(rowIndex)"
            >{{ getRowLabel(rowIndex) }}</span
          >
          <div
            v-for="(seat, colIndex) in row"
            :key="colIndex"
            :class="['seat-cell', `seat-type-${seat.seatType}`]"
            @click="setSeatType(rowIndex, colIndex)"
            @contextmenu.prevent="setAisle(rowIndex, colIndex)"
          >
            <template v-if="seat.seatType !== '走道'">
              <Icon
                :icon="getSeatIcon(seat.seatType)"
                class="seat-icon"
                :style="{ color: getSeatColor(seat.seatType) }"
              />
              <span class="seat-number">{{ seat.columnNumber }}</span>
            </template>
          </div>
          <span
            class="row-label"
            @click="setRowNormal(rowIndex)"
            @contextmenu.prevent="setRowAisle(rowIndex)"
            >{{ getRowLabel(rowIndex) }}</span
          >
        </div>
        <div class="col-labels-row">
          <span class="row-label"></span>
          <div
            v-for="colIndex in cols"
            :key="colIndex"
            class="col-label-cell"
            @click="setColumnNormal(colIndex - 1)"
            @contextmenu.prevent="setColumnAisle(colIndex - 1)"
          >
            {{ colIndex }}
          </div>
          <span class="row-label"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'

// ==== 1. 響應式數據 (Reactive Data) ====
const rows = ref(7)
const cols = ref(18)
const seatsGrid = ref([])
const currentDrawMode = ref('一般座位')
const seatMapRef = ref(null)

const SEAT_GAP_RATIO = 0.05
const ROW_GAP_RATIO = 0.08
const ROW_LABEL_WIDTH = 20

const seatSize = ref(40)
const seatGap = ref(seatSize.value * SEAT_GAP_RATIO)
const rowGap = ref(seatSize.value * ROW_GAP_RATIO)
const rowLabelWidth = ref(ROW_LABEL_WIDTH)

const MIN_SEAT = 16
const MAX_SEAT = 40

// 移除 min-height 和 max-height，讓容器恢復彈性

// ==== 2. 輔助函式 (Utility Functions) ====
const debounce = (fn, delay = 100) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }
}

const getRowLabel = (index) => {
  return String.fromCharCode('A'.charCodeAt(0) + index)
}

// ==== 3. 核心邏輯函式 (Core Logic Functions) ====
const generateSeats = (reset = false) => {
  const newRows = rows.value
  const newCols = cols.value
  const oldSeatsGrid = seatsGrid.value

  const newSeatsGrid = []
  for (let i = 0; i < newRows; i++) {
    const row = []
    for (let j = 0; j < newCols; j++) {
      if (!reset && oldSeatsGrid[i] && oldSeatsGrid[i][j]) {
        row.push(oldSeatsGrid[i][j])
      } else {
        row.push({
          seatType: '一般座位',
          rowNumber: getRowLabel(i),
          columnNumber: j + 1,
          isActive: true,
        })
      }
    }
    newSeatsGrid.push(row)
  }
  seatsGrid.value = newSeatsGrid
}

const recalcSeatSize = async () => {
  await nextTick()
  const el = seatMapRef.value
  if (!el || seatsGrid.value.length === 0) return

  const containerWidth = el.clientWidth

  // 關鍵修改：獲取 el-dialog 的實際可用高度
  const dialogContentEl = el.closest('.el-dialog__body')
  // 使用 el-dialog 的 clientHeight，若無則使用視窗高度的 80% 作為備援
  const dialogContentHeight = dialogContentEl
    ? dialogContentEl.clientHeight
    : window.innerHeight * 0.8

  // 計算座位圖可以使用的最大高度
  const padding = 20
  const screenHeight = 30
  const colLabelHeight = 20
  const seatGridGap = 20
  const availableH = dialogContentHeight - padding * 2 - screenHeight - colLabelHeight - seatGridGap

  const totalRows = rows.value
  const totalCols = cols.value

  // 將可用高度納入計算
  const availableW = containerWidth - padding * 2 - rowLabelWidth.value * 2
  const sizeFromWidth = availableW / (totalCols + (totalCols - 1) * SEAT_GAP_RATIO)

  // 使用實際可用高度來計算高度可以給出的座位尺寸
  const sizeFromHeight = availableH / (totalRows + (totalRows - 1) * ROW_GAP_RATIO)

  let finalSize = Math.floor(Math.min(sizeFromWidth, sizeFromHeight))
  finalSize = Math.max(MIN_SEAT, Math.min(MAX_SEAT, finalSize))

  if (isNaN(finalSize)) {
    finalSize = MIN_SEAT
  }

  if (finalSize !== seatSize.value) {
    seatSize.value = finalSize
    seatGap.value = finalSize * SEAT_GAP_RATIO
    rowGap.value = finalSize * ROW_GAP_RATIO
  }
}

const debouncedRecalc = debounce(recalcSeatSize, 160)

/**
 * @description 將後端回傳的座位資料清單，轉換成前端繪製座位圖所需的二維網格
 * @param existingSeats 後端回傳的座位資料清單
 */
const loadSeatsToGrid = (existingSeats) => {
  // 1. 判斷是否存在現有座位資料
  // 如果座位清單是空的，表示為全新影廳或沒有設定座位，須建立全新的網格
  if (!existingSeats || existingSeats.length === 0) {
    // 建立全新的預設座位網格
    generateSeats(true)
  } else {
    // 2. 根據後端資料，計算網格尺寸
    // 找出最大行號及列號，確認繪製網格的尺寸

    // 最大行數判斷
    const maxRow = existingSeats.reduce((max, seat) => {
      // 確保 行號是單一字母 如'A'
      if (typeof seat.rowNumber === 'string' && seat.rowNumber.length === 1) {
        // 將 'A' 轉換為索引 0 ， 'B' 為 1 後續依此類推
        const rowIndex = seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0)
        return Math.max(max, rowIndex + 1) // 找出最大行數
      }
      return max
    }, 0)

    // 最大列述判斷
    const maxCol = existingSeats.reduce((max, seat) => {
      // 找出最大列號
      return Math.max(max, seat.columnNumber || 0)
    }, 0)

    // 將計算出的最大行列數設定為 rows 、 cols 響應式數據
    rows.value = maxRow > 0 ? maxRow : 7
    cols.value = maxCol > 0 ? maxCol : 18

    // 3. 建立全新的二維網格(列 * 行)，準備供後續呈現座位圖
    /* 
      Array.from({ length: rows.value }, (_, i) : _ 
        => 陣列的元素值，其中底線表示「我會忽略這個參數，我不打算在函式裡面使用它」、i => 陣列的索引值
    */
    const newSeatsGrid = Array.from({ length: rows.value }, (_, i) =>
      // 初始狀態下將每個格子先設定為「走道」，並賦予基本屬性
      Array.from({ length: cols.value }, (_, j) => ({
        seatType: '走道',
        isActive: false,
        rowNumber: getRowLabel(i), // 根據索引賦予正確的行號
        columnNumber: j + 1, // 根據索引賦予正確的列號
      })),
    )

    // 4. 利用後端資料填充上述二維網格
    existingSeats.forEach((seat) => {
      const rowIndex = seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0)
      const colIndex = seat.columnNumber - 1

      // 檢查索引是否有效，避免因不乾淨的資料而報錯
      if (rowIndex >= 0 && rowIndex < rows.value && colIndex >= 0 && colIndex < cols.value) {
        // 使用展開運算子(...)將後端資料的 seatType 和 isActive 覆蓋到新建立的物件上。
        newSeatsGrid[rowIndex][colIndex] = { ...newSeatsGrid[rowIndex][colIndex], ...seat }
      }
    })

    // 5. 更新響應式變數
    seatsGrid.value = newSeatsGrid
  }
}

const init = (existingSeats) => {
  loadSeatsToGrid(existingSeats)
  nextTick(() => {
    debouncedRecalc()
  })
}

// ==== 4. 生命週期鉤子與監聽器 (Lifecycle Hooks & Watchers) ====
watch([rows, cols], () => {
  generateSeats()

  // 保持這行，讓 el-dialog 重新計算
  window.dispatchEvent(new Event('resize'))

  nextTick(() => {
    recalcSeatSize()
  })
})

let resizeObserver = null
onMounted(() => {
  const seatMapEl = seatMapRef.value
  if (window.ResizeObserver && seatMapEl) {
    resizeObserver = new ResizeObserver(() => {
      debouncedRecalc()
    })
    // 觀察 el-dialog 的內容區域，而不只是 seat-map-container
    const dialogContentEl = seatMapEl.closest('.el-dialog__body')
    if (dialogContentEl) {
      resizeObserver.observe(dialogContentEl)
    } else {
      resizeObserver.observe(seatMapEl)
    }
  } else {
    window.addEventListener('resize', debouncedRecalc)
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  else window.removeEventListener('resize', debouncedRecalc)
})

// ==== 5. 座位樣式和操作相關函式 (Seat Styling and Operation Functions) ====
const getSeatIcon = (seatType) => {
  switch (seatType) {
    case '一般座位':
      return 'ic:baseline-chair'
    case '友善座位':
      return 'mdi:wheelchair'
    default:
      return ''
  }
}

const getSeatColor = (seatType) => {
  switch (seatType) {
    case '一般座位':
      return '#484891'
    case '友善座位':
      return '#9D9D9D'
    default:
      return 'transparent'
  }
}

const setSeatType = (rowIndex, colIndex) => {
  const seat = seatsGrid.value[rowIndex][colIndex]
  seat.seatType = currentDrawMode.value
  seat.isActive = true
}

const setAisle = (rowIndex, colIndex) => {
  const seat = seatsGrid.value[rowIndex][colIndex]
  seat.seatType = '走道'
  seat.isActive = false
}

const setRowAisle = (rowIndex) => {
  seatsGrid.value[rowIndex].forEach((seat) => {
    seat.seatType = '走道'
    seat.isActive = false
  })
}

const setRowNormal = (rowIndex) => {
  seatsGrid.value[rowIndex].forEach((seat) => {
    seat.seatType = '一般座位'
    seat.isActive = true
  })
}

const setColumnAisle = (colIndex) => {
  seatsGrid.value.forEach((row) => {
    if (row[colIndex]) {
      row[colIndex].seatType = '走道'
      row[colIndex].isActive = false
    }
  })
}

const setColumnNormal = (colIndex) => {
  seatsGrid.value.forEach((row) => {
    if (row[colIndex]) {
      row[colIndex].seatType = '一般座位'
      row[colIndex].isActive = true
    }
  })
}

const resetSeats = () => {
  seatsGrid.value.forEach((row) => {
    row.forEach((seat) => {
      seat.seatType = '一般座位'
      seat.isActive = true
    })
  })
}

const saveSeats = () => {
  const flattenedSeats = seatsGrid.value.flat()
  const activeSeats = flattenedSeats.filter((seat) => seat.seatType !== '走道')
  return {
    seats: activeSeats,
    totalSeats: activeSeats.length,
  }
}

// ==== 6. 暴露公共方法 (Exposing Public Methods) ====
defineExpose({
  saveSeats,
  init,
})
</script>

<style scoped>
.seat-editor-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.seat-editor-controls {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  flex-shrink: 0;
}

.controls-form {
  margin-bottom: 0;
}

.tip-text {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.seat-map-container {
  --seat-size: 40px;
  --seat-gap: 8px;
  --row-gap: 10px;
  --row-label-width: 20px;

  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #1a1818;
  flex: 1;
  overflow: auto;
  max-height: none;
  width: 100%;
  box-sizing: border-box;
}

.screen {
  width: 80%;
  height: 30px;
  background: #909399;
  color: #fff;
  text-align: center;
  line-height: 30px;
  font-size: 1.2em;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
  flex-shrink: 0;
}

.seat-grid {
  display: flex;
  flex-direction: column;
  gap: var(--row-gap);
  width: 100%;
  align-items: center;
  flex: 1;
}

.seat-row {
  display: flex;
  align-items: center;
  gap: var(--seat-gap);
  flex-shrink: 0;
}

.row-label {
  font-weight: bold;
  color: #b8b9bb;
  width: var(--row-label-width);
  text-align: right;
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;
}
.row-label:last-child {
  text-align: left;
}

.col-labels-row {
  display: flex;
  align-items: center;
  gap: var(--seat-gap);
  flex-shrink: 0;
  margin-top: var(--row-gap);
}

.col-label-cell {
  width: var(--seat-size);
  height: var(--row-label-width);
  color: #b8b9bb;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: clamp(10px, calc(var(--row-label-width) * 0.6), 14px);
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;
}

.seat-cell {
  position: relative;
  width: var(--seat-size);
  height: var(--seat-size);
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.15s ease-in-out;
  flex-shrink: 0;
  user-select: none;
}

.seat-cell:not(:last-child) {
  margin-right: 0;
}

.seat-icon {
  font-size: clamp(20px, calc(var(--seat-size) * 0.9), 40px);
  transition: transform 0.15s ease-in-out;
}

.seat-number {
  font-size: clamp(8px, calc(var(--seat-size) * 0.25), 12px);
  color: #fff;
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  font-weight: bold;
  text-shadow:
    0 0 2px #333,
    0 0 2px #333,
    0 0 2px #333,
    0 0 2px #333;
  color: #fff;
  z-index: 2;
}

.seat-type-走道 {
  cursor: not-allowed;
  background-color: transparent;
}
.seat-type-走道 .seat-icon,
.seat-type-走道 .seat-number {
  display: none;
}
.seat-type-走道:hover {
  transform: none;
}

.seat-cell:hover .seat-icon {
  transform: scale(1.1);
  filter: brightness(1.2);
}
.seat-cell:hover .seat-number {
  color: #333;
}

.seat-type-走道:hover .seat-icon,
.seat-type-走道:hover .seat-number {
  transform: none;
  filter: none;
  display: none;
}
</style>
