<template>
  <div class="page-container">
    <div class="booking-wrapper">
      <BookingProgressBar :current-step="$route.meta.step || 1" />

      <SessionInfoHeader v-if="sessionInfo" :sessionInfo="sessionInfo" />

      <router-view />
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 訂票流程的佈局組件 (BookingLayout.vue)
 *
 * - 職責：
 * 1. 作為所有訂票步驟頁面的父容器，提供統一的佈局和外觀。
 * 2. 顯示流程共用的 UI 元素：進度條 (`BookingProgressBar`) 和場次資訊 (`SessionInfoHeader`)。
 * 3. 透過 <router-view> 渲染當前步驟對應的子路由組件。
 * 4. (未來) 負責從 Pinia Store 獲取並管理整個訂票流程所需的共享數據。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { onMounted, onBeforeUnmount } from 'vue'
import BookingProgressBar from '../../components/front/BookingProgressBar.vue'
import SessionInfoHeader from '@/features/MovieSessions/components/front/SessionInfoHeader.vue'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const route = useRoute() // 用於讀取當前路由資訊，特別是 URL 中的 sessionId
const seatSelectionStore = usePublicSeatSelectionStore() // 實例化 Store

// ----------------------------------------------------------------
// Pinia Store 狀態管理 (State Management)
// ----------------------------------------------------------------
// 從 Store 中獲取所需的狀態 (state) 和方法 (actions)
// 我們在這裡也需要 isLoading 和 error 來控制 UI 顯示
const { sessionInfo, isLoading, error } = storeToRefs(seatSelectionStore)
const { fetchSeatLayout, clearAllBookingState } = seatSelectionStore

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------

/**
 * onMounted 生命週期鉤子
 * 當 BookingLayout 組件第一次被掛載到畫面上時，
 * 就會從 URL 讀取 sessionId，並觸發 Store 的 action 去後端獲取資料。
 */
onMounted(() => {
  // 從當前路由的 URL 參數中，獲取 `sessionId`
  const sessionId = route.params.sessionId
  if (sessionId) {
    fetchSeatLayout(sessionId)
  } else {
    // 如果 URL 中沒有 sessionId，這是一個嚴重的錯誤狀態
    console.error('BookingLayout: 未在路由中找到 sessionId！')
  }
})

/**
 * onBeforeUnmount 生命週期鉤子
 * 當使用者離開整個訂票流程 (例如點擊Logo回到首頁) 時，
 * 這個組件會被卸載，我們就清空 Store 中的資料，避免資料污染。
 */
onBeforeUnmount(() => {
  // 呼叫 Store 的 action 清空當前的場次資料
  clearAllBookingState()
})
</script>

<style scoped>
/* 頁面容器 */
.page-container {
  padding: 24px;
  background-color: #181818;
  color: #fff;
  min-height: 100vh;
  display: flex;
  justify-content: center;
}

/* 訂票流程的主要內容包裝器 */
.booking-wrapper {
  width: 100%;
  max-width: 90%;
  display: flex;
  flex-direction: column;
  gap: 24px; /* 統一設定元件之間的間距 */
  background-color: #1f1f1f; /* 與外層區隔 */
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
}
</style>
