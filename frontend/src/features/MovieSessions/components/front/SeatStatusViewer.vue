<template>
  <div
    class="seat-map-container"
    v-if="seatsGrid.length > 0"
    :style="{
      '--seat-size': `${seatSize}px`,
      '--seat-gap': `${seatGap}px`,
      '--row-gap': `${rowGap}px`,
      '--row-label-width': `${rowLabelWidth}px`,
    }"
  >
    <div class="screen" :style="{ width: screenWidth }">螢幕</div>

    <div class="seat-grid">
      <div v-for="(row, rowIndex) in seatsGrid" :key="rowIndex" class="seat-row">
        <span class="row-label">{{ getRowLabel(rowIndex) }}</span>

        <div
          v-for="(seat, colIndex) in row"
          :key="seat?.seat?.seatId || `empty-${rowIndex}-${colIndex}`"
          :class="[
            'seat-cell',
            `seat-status-${seat?.status?.toLowerCase()}`,
            { 'is-accessible': seat?.seat?.seatType === '友善座位' },
          ]"
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
        <div v-for="colIndex in numberOfCols" :key="colIndex" class="col-label-cell">
          {{ colIndex }}
        </div>
        <span class="row-label"></span>
      </div>
    </div>

    <div class="seat-legend">
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
</template>

<script setup>
/**
 * @fileoverview
 * 座位圖顯示器 (SeatStatusViewer.vue)
 *
 * - 職責：
 * 這是一個純粹的「展示型組件」(Presentational Component)。它的職責非常單純：
 * 接收一個一維的座位陣列 (`seats`) 以及一些尺寸設定 (`seatSize` 等)，
 * 然後將其視覺化地渲染成一個二維的座位網格圖。
 * 它不包含任何業務邏輯或 API 請求，只負責「看」，因此具有高度的可重用性。
 *
 * - 底層邏輯：
 * 核心是 `seatsGrid` 計算屬性，它將來自後端的、可能不規則的一維座位列表，
 * 轉換成一個規則的二維矩陣（用 `null` 填充不存在座位的位置），
 * 使得模板中可以使用簡單的兩層 `v-for` 迴圈來輕鬆渲染出整個影廳的佈局。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { computed, toRefs } from 'vue'
import { Icon } from '@iconify/vue'

// ----------------------------------------------------------------
// 組件屬性定義 (Props Definition)
// ----------------------------------------------------------------

/**
 * @props
 * @property {Array} seats - 必需。從後端獲取的一維座位物件陣列。
 * @property {Number} seatSize - 座位圖示的尺寸（像素）。
 * @property {Number} seatGap - 座位之間的水平間距（像素）。
 * @property {Number} rowGap - 座位列之間的垂直間距（像素）。
 * @property {Number} rowLabelWidth - 左右兩側排號標籤的寬度（像素）。
 */
const props = defineProps({
  seats: { type: Array, required: true },
  seatSize: { type: Number, default: 32 },
  seatGap: { type: Number, default: 5 },
  rowGap: { type: Number, default: 8 },
  rowLabelWidth: { type: Number, default: 20 },
})

// 使用 toRefs 將 props 解構為 ref 物件，以便在 script 中使用時保持響應性。
// 這也讓 computed 屬性可以正確地依賴 props 的變化而重新計算。
const { seats, seatSize, seatGap, rowGap, rowLabelWidth } = toRefs(props)

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------

/**
 * 將一維的 `seats` 陣列轉換為二維的網格陣列。
 * 這是實現不規則影廳佈局渲染的關鍵。
 * @returns {Array<Array<object|null>>} 代表座位佈局的二維陣列。
 */
const seatsGrid = computed(() => {
  // 如果沒有傳入 seats prop，則返回空陣列。
  if (!seats.value || seats.value.length === 0) return []

  const grid = []
  let maxRow = 0,
    maxCol = 0

  // 第一步：遍歷所有座位，找出最大的行號和列號，以確定網格的總尺寸。
  seats.value.forEach((s) => {
    if (s && s.seat && s.seat.rowNumber && s.seat.columnNumber) {
      const rowIndex = s.seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0) // 'A'->0, 'B'->1 ...
      const colIndex = s.seat.columnNumber - 1 // 1->0, 2->1 ...
      maxRow = Math.max(maxRow, rowIndex)
      maxCol = Math.max(maxCol, colIndex)
    }
  })

  // 第二步：根據最大行列數，建立一個填滿 `null` 的二維陣列作為基礎畫布。
  for (let i = 0; i <= maxRow; i++) {
    grid.push(Array(maxCol + 1).fill(null))
  }

  // 第三步：再次遍歷所有座位，將每個座位物件放入 `grid` 畫布的正確位置，覆蓋掉原來的 `null`。
  seats.value.forEach((s) => {
    if (s && s.seat && s.seat.rowNumber && s.seat.columnNumber) {
      const rowIndex = s.seat.rowNumber.charCodeAt(0) - 'A'.charCodeAt(0)
      const colIndex = s.seat.columnNumber - 1
      if (grid[rowIndex]) {
        grid[rowIndex][colIndex] = s
      }
    }
  })

  return grid
})

/**
 * 計算網格的總欄數。
 * @returns {number} 總欄數。
 */
const numberOfCols = computed(() => seatsGrid.value[0]?.length || 0)

/**
 * 計算螢幕示意圖的寬度，使其與座位網格的總寬度保持一致。
 * @returns {string} 帶有 'px' 單位元的寬度字串。
 */
const screenWidth = computed(() => {
  if (!seatsGrid.value || seatsGrid.value.length === 0) return '80%'
  const cols = numberOfCols.value
  const totalWidth = cols * seatSize.value + (cols > 1 ? (cols - 1) * seatGap.value : 0)
  return `${totalWidth}px`
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 將數字索引轉換為字母排號。
 * @param {number} index - 列的索引 (0, 1, 2, ...)。
 * @returns {string} 對應的字母 ('A', 'B', 'C', ...)。
 */
const getRowLabel = (index) => String.fromCharCode('A'.charCodeAt(0) + index)

/**
 * 根據座位類型返回對應的 iconify 圖示名稱。
 * @param {string} seatType - 座位類型，例如 '友善座位'。
 * @returns {string} 圖示名稱。
 */
const getSeatIcon = (seatType) => (seatType === '友善座位' ? 'mdi:wheelchair' : 'ic:baseline-chair')
</script>

<style scoped>
/* 整個組件的樣式都基於由 props 傳入的 CSS 自訂屬性，
  這使得組件的尺寸、間距等外觀可以完全由父組件控制，實現了高度的客製化能力。
*/

/* 座位圖容器的基礎佈局 */
.seat-map-container {
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

/* 螢幕樣式 */
.screen {
  height: 30px;
  background-color: rgba(255, 255, 255, 0.25);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.6);
  color: #fff;
  text-align: center;
  line-height: 30px;
  font-size: 1.2em;
  border-radius: 8px;
  margin-bottom: 10px;
  width: 100%;
  max-width: var(--max-screen-width, 100%);
}

/* 座位網格佈局 */
.seat-grid {
  display: flex;
  flex-direction: column;
  gap: var(--row-gap); /* 使用 CSS 變數控制列間距 */
  align-items: center;
  width: 100%;
}

/* 每一列座位的佈局 */
.seat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--seat-gap); /* 使用 CSS 變數控制座位間距 */
  width: 100%;
}

/* 排號標籤樣式 (A, B, C...) */
.row-label {
  font-weight: bold;
  color: #b8b9bb;
  width: var(--row-label-width); /* 使用 CSS 變數控制寬度 */
  text-align: center;
  user-select: none;
  flex-shrink: 0;
}

/* 單個座位單元格的樣式 */
.seat-cell {
  flex: 1 1 0;
  aspect-ratio: 1 / 1; /* 維持 1:1 的長寬比，確保是正方形 */
  max-width: var(--seat-size); /* 使用 CSS 變數控制最大寬度 */
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
  position: relative;
}

/* 座位圖示樣式 */
.seat-icon {
  width: 80%;
  height: 80%;
}

/* 疊加在圖示上方的座位號碼 */
.seat-number {
  font-size: calc(var(--seat-size) * 0.3); /* 字體大小也根據座位尺寸動態計算 */
  color: #fff;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: bold;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.8);
  pointer-events: none; /* 讓滑鼠事件可以穿透數字，點擊到下方的圖示 */
}

/* -- 不同座位狀態的顏色定義 -- */
.seat-status-available .seat-icon {
  color: #617cc5; /* 可選座位 */
}
.seat-status-sold .seat-icon,
.seat-status-reserved .seat-icon {
  color: #e50914; /* 已售出 和 已預訂 的座位都顯示為紅色 */
}
.seat-status-aisle {
  background: transparent; /* 走道，完全透明 */
}
.is-accessible .seat-icon {
  color: #dcdcdc !important; /* 友善座位，使用 !important 提高優先級，覆蓋其他狀態顏色 */
}
/* -- -------------------- -- */

/* 底部欄號標籤列的樣式 */
.col-labels-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--seat-gap);
  width: 100%;
  margin-top: calc(var(--row-gap) / 2);
}
/* 單個欄號的樣式 */
.col-label-cell {
  flex: 1 1 0;
  max-width: var(--seat-size);
  height: var(--row-label-width);
  color: #b8b9bb;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  user-select: none;
}
/* 圖例區域樣式 */
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
</style>
