<template>
  <div class="payment-result-page">
    <div class="result-card">
      <div class="icon-wrapper failure">
        <Icon icon="mdi:close-circle-outline" />
      </div>
      <h1 class="title">付款失敗</h1>
      <p class="subtitle">哎呀！在處理您的付款時發生問題。</p>

      <div class="order-info">
        <div class="info-row">
          <span class="label">訂單編號</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-row" v-if="errorMessage">
          <span class="label">失敗原因</span>
          <span class="value error-message">{{ errorMessage }}</span>
        </div>
      </div>

      <div class="actions">
        <button class="action-btn primary" @click="retryPayment">返回訂單頁面重試</button>
        <button class="action-btn secondary" @click="goToHome">返回首頁</button>
      </div>
      <p class="support-text">
        如果問題持續發生，請<a @click="contactSupport">聯繫客服</a>並提供您的訂單編號。
      </p>
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 付款失敗結果頁面 (PaymentFailure.vue)
 *
 * - 職責：
 * 1. 告知使用者付款失敗，並顯示從綠界回傳的錯誤訊息。
 * 2. 顯示相關的訂單編號，方便使用者查詢。
 * 3. [重要] 不清除 Pinia 狀態，讓使用者有機會返回上一步重新付款。
 * 4. 提供重新付款或返回首頁的選項。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'
import { storeToRefs } from 'pinia'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const route = useRoute()
const router = useRouter()
const seatSelectionStore = usePublicSeatSelectionStore()

// ----------------------------------------------------------------
// Pinia Store 狀態管理
// ----------------------------------------------------------------
// 從 store 中獲取需要的資料，以便能跳轉回確認頁面
const { sessionInfo, paymentMethod } = storeToRefs(seatSelectionStore)

// ----------------------------------------------------------------
// 本地響應式狀態 (Local Reactive State)
// ----------------------------------------------------------------
const orderId = ref('')
const errorMessage = ref('')

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------
// 透過 computed 屬性安全地獲取 sessionId
const currentSessionId = computed(() => sessionInfo.value?.sessionId)

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------
onMounted(() => {
  orderId.value = route.query.orderId || 'N/A'
  errorMessage.value = route.query.message || '未提供失敗原因，請聯繫客服。'

  // [重要] 在此頁面，我們 *不* 清除訂票狀態 (clearAllBookingState)。
  // 這樣才能保留訂單的所有資訊 (如選擇的票種、座位等)，
  // 讓使用者有機會點擊按鈕返回上一步重新付款。
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 返回訂單確認頁面，讓使用者重試付款
 */
function retryPayment() {
  // 檢查 Pinia 中是否還保留著場次 ID
  if (currentSessionId.value) {
    // 如果有，就跳轉回訂單確認頁
    router.push({
      name: 'BookingConfirmation', // 確保您的路由有名稱 'BookingConfirmation'
      params: { sessionId: currentSessionId.value },
      query: { payment: paymentMethod.value }, // 將付款方式也帶回去
    })
  } else {
    // 如果 Pinia 狀態已遺失 (例如使用者關閉分頁後才點擊連結回來)
    // 就無法安全地返回上一步，此時引導使用者回首頁
    alert('您的訂票資訊已過期，無法返回訂單。請重新開始訂票流程。')
    goToHome()
  }
}

/**
 * 放棄訂單，返回首頁
 */
function goToHome() {
  // 如果使用者選擇放棄並返回首頁，這時才需要清除訂票狀態
  seatSelectionStore.clearAllBookingState()
  router.push({ name: 'Home' }) // 假設您的首頁路由名稱為 'Home'
}

/**
 * 聯繫客服 (示意)
 */
function contactSupport() {
  alert(`請聯繫客服並提供您的訂單編號：${orderId.value}`)
  // 這裡可以換成更正式的聯繫方式，例如跳轉到聯繫頁面或顯示 email
}
</script>

<style scoped>
/* 樣式與成功頁面共用，僅針對錯誤訊息做微調 */

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

.icon-wrapper.failure {
  color: #e50914; /* 使用專案主色調紅色來表示失敗/錯誤 */
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
  gap: 16px; /* 增加間距避免文字重疊 */
}

.label {
  color: #aaa;
  flex-shrink: 0; /* 避免 label 被壓縮 */
}

.value {
  font-weight: bold;
  color: #fff;
  background-color: #333;
  padding: 4px 12px;
  border-radius: 4px;
  letter-spacing: 1px;
}

.value.error-message {
  color: #e6a23c; /* 警告黃色，用於顯示錯誤訊息 */
  background-color: rgba(230, 162, 60, 0.1);
  text-align: right;
  line-height: 1.5;
  white-space: normal; /* 允許多行 */
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

.action-btn.primary {
  background-color: #e50914;
  color: #fff;
}
.action-btn.primary:hover {
  background-color: #f40612;
  transform: translateY(-2px);
}

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

.support-text {
  font-size: 14px;
  color: #888;
  margin-top: 16px;
}

.support-text a {
  color: #617cc5;
  text-decoration: underline;
  cursor: pointer;
}
</style>
