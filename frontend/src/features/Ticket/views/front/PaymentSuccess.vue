<template>
  <div class="payment-result-page">
    <div class="result-card">
      <div class="icon-wrapper success">
        <Icon icon="mdi:check-circle-outline" />
      </div>
      <h1 class="title">付款成功！</h1>
      <p class="subtitle">您的訂單已確認，感謝您在 Flux Cinema 訂票。</p>

      <div class="order-info">
        <div class="info-row">
          <span class="label">訂單編號</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <!-- <p class="notice">
          詳細訂單資訊與電子票券(QR Code)已寄送至您的會員信箱，您也可以隨時在「會員中心 >
          我的訂單」中查看。
        </p> -->
      </div>

      <div class="actions">
        <button class="action-btn primary" @click="goToMemberOrders">查看我的訂單</button>
        <button class="action-btn secondary" @click="goToHome">返回首頁</button>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 付款成功結果頁面 (PaymentSuccess.vue)
 *
 * - 職責：
 * 1. 顯示一個成功的、令人安心的介面給使用者。
 * 2. 從 URL query 參數中讀取並顯示訂單編號。
 * 3. 清除 Pinia 中儲存的訂票流程狀態，避免使用者返回上一頁造成資料混亂。
 * 4. 提供清晰的下一步操作按鈕 (查看訂單 / 返回首頁)。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const route = useRoute()
const router = useRouter()
const seatSelectionStore = usePublicSeatSelectionStore()

// ----------------------------------------------------------------
// 本地響應式狀態 (Local Reactive State)
// ----------------------------------------------------------------
const orderId = ref('')

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------
onMounted(() => {
  // 從路由的 query string 中獲取訂單編號
  orderId.value = route.query.orderId || 'N/A'

  // [重要] 付款流程已成功結束，必須清除 Pinia 中的訂票暫存狀態。
  // 這可以防止使用者透過瀏覽器返回按鈕回到已完成的訂單流程中，造成混亂或重複操作。
  seatSelectionStore.clearAllBookingState()
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 導航至會員的訂單列表頁面
 */
function goToMemberOrders() {
  router.push({
    name: 'MemberProfile', // 目標路由名稱:會員中心
    query: { view: 'ticketOrders' }, // 附帶的查詢參數，告訴 MemberProfile 頁面要顯示 ticketOrders
  })
}

/**
 * 導航至網站首頁
 */
function goToHome() {
  router.push({ name: 'Home' }) // 假設您的首頁路由名稱為 'Home'
}
</script>

<style scoped>
/* 整體頁面容器 */
.payment-result-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #181818;
  color: #fff;
  padding: 24px;
  box-sizing: border-box;
}

/* 結果卡片 */
.result-card {
  width: 100%;
  max-width: 550px;
  background-color: #1f1f1f;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

/* 圖標容器 */
.icon-wrapper {
  font-size: 80px;
  line-height: 1;
  margin-bottom: 10px;
}

.icon-wrapper.success {
  color: #28a745; /* 綠色，代表成功 */
}

.icon-wrapper.failure {
  color: #dc3545; /* 紅色，代表失敗 */
}

/* 標題與副標題 */
.title {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
  color: #f5f5f5;
}

.subtitle {
  font-size: 16px;
  color: #aaa;
  margin: 0;
  max-width: 400px;
  line-height: 1.6;
}

/* 訂單資訊區塊 */
.order-info {
  width: 100%;
  border-top: 1px solid #3c3c3c;
  border-bottom: 1px solid #3c3c3c;
  padding: 24px 0;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  padding: 0 10px;
}

.label {
  color: #aaa;
}

.value {
  font-weight: bold;
  color: #fff;
  background-color: #333;
  padding: 4px 12px;
  border-radius: 4px;
  letter-spacing: 1px;
}

.notice {
  font-size: 14px;
  color: #888;
  text-align: left;
  line-height: 1.7;
  padding: 0 10px;
  margin-top: 8px;
}

/* 操作按鈕區塊 */
.actions {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  width: 100%;
}

.action-btn {
  flex-grow: 1;
  padding: 12px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 主要按鈕 (參考您的 next-step-btn) */
.action-btn.primary {
  background-color: #e50914;
  color: #fff;
}
.action-btn.primary:hover {
  background-color: #f40612;
  transform: translateY(-2px);
}

/* 次要按鈕 */
.action-btn.secondary {
  background-color: transparent;
  color: #ccc;
  border: 1px solid #555;
}
.action-btn.secondary:hover {
  background-color: #333;
  border-color: #777;
  color: #fff;
}
</style>
