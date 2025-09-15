<template>
  <div class="page-container">
    <div class="content-wrapper" ref="contentWrapperRef">
      <div v-if="isLoading" class="loading-placeholder">
        <p>正在為您劃位，請稍候...</p>
      </div>

      <div v-else-if="error" class="loading-placeholder error-message">
        <p>{{ error }}</p>
      </div>

      <template v-else-if="sessionInfo">
        <SessionInfoHeader :sessionInfo="sessionInfo" />

        <SeatStatusViewer
          v-if="sessionSeats.length > 0"
          :seats="sessionSeats"
          :seat-size="dynamicSeatSize.size"
          :seat-gap="dynamicSeatSize.gap"
          :row-gap="dynamicSeatSize.rowGap"
          :row-label-width="20"
        />
      </template>
    </div>

    <div class="actions-bar">
      <el-button size="large" @click="goBack">返回</el-button>
      <el-button
        type="primary"
        size="large"
        :disabled="isLoading || error"
        @click="proceedToBooking"
        >前往訂票</el-button
      >
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 場次座位選擇頁面 (SeatSelectionPage.vue)
 *
 * - 職責：
 * 1. 從路由參數中獲取 `sessionId`。
 * 2. 透過 Pinia Store (`usePublicSeatSelectionStore`) 發起 API 請求，獲取該場次的詳細資訊和座位圖。
 * 3. 處理載入中 (loading) 和錯誤 (error) 的 UI 狀態。
 * 4. 動態計算座位圖的尺寸，以適應不同大小的螢幕。
 * 5. 將獲取到的座位資料傳遞給子組件 `SeatStatusViewer` 進行渲染。
 * 6. 提供返回和前往下一步的操作。
 *
 * - 底層邏輯：
 * 此組件是典型的「容器組件」(Container Component)，它自身不包含複雜的渲染邏輯，
 * 而是專注於「資料獲取」和「狀態管理」，並將資料傳遞給「展示組件」(Presentational Component) - SeatStatusViewer 來顯示。
 * 這種模式使得職責分離，程式碼更易於維護。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton } from 'element-plus'
import { storeToRefs } from 'pinia'
import SeatStatusViewer from '@/features/MovieSessions/components/front/SeatStatusViewer.vue'
import SessionInfoHeader from '../../components/front/SessionInfoHeader.vue'
import { usePublicSeatSelectionStore } from '../../store/usePublicSeatSelectionStore'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const route = useRoute() // 用於讀取當前路由資訊，特別是 URL 中的參數 (如 sessionId)
const router = useRouter() // 用於進行程式化的路由導航 (如返回上一頁)
const seatSelectionStore = usePublicSeatSelectionStore() // 實例化 Pinia Store，獲取狀態管理的能力

// ----------------------------------------------------------------
// Pinia Store 狀態管理 (State Management)
// ----------------------------------------------------------------
// 使用 storeToRefs 將 Store 中的 state 解構出來，同時保持其響應性。
// 如果直接用 const { sessionInfo } = seatSelectionStore，會失去響應性。
const { sessionInfo, sessionSeats, isLoading, error } = storeToRefs(seatSelectionStore)
// 直接解構 Store 中的 actions (方法)，因為它們本身就是函式，不需要保持響應性。
const { fetchSeatLayout, clearAllBookingState } = seatSelectionStore

// ----------------------------------------------------------------
// 本地響應式狀態 (Local Reactive State)
// ----------------------------------------------------------------
// 創建一個 ref 來引用模板中的 <div class="content-wrapper"> DOM 元素。
const contentWrapperRef = ref(null)
// 創建一個 ref 來存儲 contentWrapperRef 元素的寬度，用於動態計算。
const contentWrapperWidth = ref(0)

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------

/**
 * 動態計算座位尺寸、間距等。
 * 這是實現響應式座位圖的核心，它會根據容器寬度和座位數量自動縮放。
 * @returns {object} 包含 size, gap, rowGap 的物件。
 */
const dynamicSeatSize = computed(() => {
  // 如果沒有座位資料或還沒取到容器寬度，返回一個預設值。
  if (sessionSeats.value.length === 0 || contentWrapperWidth.value === 0) {
    return { size: 32, gap: 5, rowGap: 8 }
  }
  // 1. 從所有座位中，找出最大的欄位數，以確定影廳的寬度。
  const maxCol = sessionSeats.value.reduce((max, s) => Math.max(max, s.seat.columnNumber || 0), 0)
  // 2. 定義左右兩側排號標籤的固定寬度。
  const rowLabelWidth = 20
  const totalLabelsWidth = rowLabelWidth * 2
  // 3. 根據預設尺寸，計算出座位與間距的比例。
  const gapRatio = 5 / 32
  // 4. 計算出實際可用於繪製座位的總寬度。
  const availableWidth = contentWrapperWidth.value - totalLabelsWidth
  // 5. 核心演算法：根據可用寬度、總欄數和間距比例，計算出單個座位的理論尺寸。
  let calculatedSize = availableWidth / (maxCol + (maxCol > 1 ? (maxCol - 1) * gapRatio : 0))
  // 6. 限制座位尺寸在一個合理的範圍內 (最小16px，最大36px)，避免過大或過小。
  calculatedSize = Math.max(16, Math.min(36, calculatedSize))

  // 7. 返回最終計算出的、取整數的尺寸和間距。
  return {
    size: Math.floor(calculatedSize),
    gap: Math.floor(calculatedSize * gapRatio),
    rowGap: Math.floor(calculatedSize * gapRatio * 1.2),
  }
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 處理返回按鈕的點擊事件，導航回歷史紀錄的上一頁。
 */
const goBack = () => {
  router.back()
}

/**
 * 處理此頁面的「前往訂票」按鈕點擊事件。
 * 直接使用當前路由中的 sessionId 導航到訂票流程。
 */
function proceedToBooking() {
  const sessionId = route.params.sessionId
  if (sessionId) {
    console.log(`從座位圖頁面進入訂票流程，場次 ID: ${sessionId}`)
    router.push({
      name: 'BookingPaymentMethod', // 跳轉到訂票流程的第一步
      params: { sessionId: sessionId }, // 將當前的 sessionId 傳遞過去
    })
  } else {
    // 正常情況下不應發生，因為能進入此頁面就一定有 sessionId
    alert('發生錯誤，缺少場次資訊，請返回重試。')
    console.error('在座位圖頁面點擊訂票時，無法從路由中獲取 sessionId')
  }
}

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------

let resizeObserver = null // 將 resizeObserver 實例宣告在外部，以便在 onBeforeUnmount 中可以存取到。

/**
 * Vue 組件掛載到 DOM 後執行。
 * 主要用於：
 * 1. 獲取初始資料。
 * 2. 設置事件監聽或觀察器。
 */
onMounted(() => {
  // 從當前路由的 URL 參數中，獲取 `sessionId`。
  const sessionId = route.params.sessionId
  if (sessionId) {
    // 如果 sessionId 存在，就觸發 Pinia Store 的 action 來從後端獲取資料。
    fetchSeatLayout(sessionId)
  } else {
    // 如果 URL 中沒有 sessionId，這是一個錯誤狀態，應在控制台報錯。
    console.error('未在路由中找到 sessionId！')
  }

  // 使用瀏覽器的 ResizeObserver API 來監聽 content-wrapper 元素的尺寸變化。
  // 這是實現響應式佈局的現代高效方法，比傳統的 window.onresize 事件性能更好。
  if (contentWrapperRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      // 當被觀察的元素尺寸變化時，這個回呼函式會被觸發。
      if (entries[0]) {
        // 更新 contentWrapperWidth 的值，這將自動觸發 dynamicSeatSize 計算屬性的重新計算。
        contentWrapperWidth.value = entries[0].contentRect.width
      }
    })
    resizeObserver.observe(contentWrapperRef.value)
  }
})

/**
 * Vue 組件即將從 DOM 中卸載前執行。
 * 主要用於清理工作，防止記憶體洩漏 (memory leak)。
 */
onBeforeUnmount(() => {
  // 組件離開時，呼叫 Store 的 action 清空當前的場次資料。
  // 這樣可以避免使用者返回後再次進入時，短暫地看到上一次的舊資料。
  clearAllBookingState()

  // 停止對 DOM 元素的尺寸監聽，釋放 ResizeObserver 佔用的資源。
  // 如果不這麼做，即使組件銷毀了，觀察器可能還在背景運行，造成記憶體洩漏。
  if (resizeObserver && contentWrapperRef.value) {
    resizeObserver.unobserve(contentWrapperRef.value)
  }
})
</script>

<style scoped>
/* 頁面最外層容器，設定基本佈局和背景色 */
.page-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 24px;
  background-color: #000;
  color: #fff;
  min-height: 100vh;
}
/* 主要內容區塊的寬度限制和佈局 */
.content-wrapper {
  width: 100%;
  max-width: 90%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 底部操作按鈕列的樣式 */
.actions-bar {
  display: flex;
  gap: 16px;
}
/* 載入中和錯誤提示的佔位樣式 */
.loading-placeholder {
  padding: 50px;
  text-align: center;
  color: #909399;
}
/* 使用 :deep() 選擇器來「穿透」子組件的 scoped CSS 限制。
  這允許我們在這個父組件中，定義一些可以影響到 SeatStatusViewer 子組件內部樣式的 CSS 變數。
  這是一種父子組件樣式通信的常用技巧。
*/
:deep(.legend-item .seat-cell) {
  width: var(--seat-size);
  height: var(--seat-size);
  display: flex;
  justify-content: center;
  align-items: center;
}
:deep(.legend-item .seat-icon) {
  font-size: calc(var(--seat-size) * 0.8);
}
:deep(.legend-item .seat-status-available .seat-icon) {
  color: #617cc5;
}
:deep(.legend-item .seat-status-sold .seat-icon),
:deep(.legend-item .seat-status-reserved .seat-icon) {
  color: #e50914;
}
:deep(.legend-item .seat-status-reserved .seat-icon) {
  color: #e50914;
}
:deep(.legend-item .is-accessible .seat-icon) {
  color: #dcdcdc !important;
}
</style>
