<template>
  <div class="payment-method-selection">
    <div class="main-content-wrapper">
      <div class="payment-options">
        <ul>
          <li>
            <a @click="selectPaymentMethod('online')">
              <div class="icon-wrapper">
                <Icon icon="mdi:credit-card-outline" />
              </div>
              <div class="text-wrapper">
                <h1>
                  <strong>線上即時付款</strong><br />
                  一般 / 早場優惠票
                </h1>
                <h2>GENERAL / EARLY BIRD</h2>
              </div>
            </a>
          </li>
          <li>
            <a @click="selectPaymentMethod('counter')">
              <div class="icon-wrapper">
                <Icon icon="mdi:storefront-outline" />
              </div>
              <div class="text-wrapper">
                <h1>
                  <strong>影城臨櫃付款</strong><br />
                  學生票 / 愛心票 / 敬老票 / 節慶活動優惠票券
                </h1>
                <h2>STUDENT / CONCESSION / SENIOR TICKET / PROMOTIONAL VOUCHER</h2>
              </div>
            </a>
          </li>
        </ul>
      </div>

      <div class="vertical-divider"></div>

      <div class="info-sidebar">
        <span class="info-link" @click="ticketInfoModalVisible = true">
          <Icon icon="mdi:help-circle-outline" /> 票種介紹
        </span>
        <span class="info-link" @click="versionInfoModalVisible = true">
          <Icon icon="mdi:help-circle-outline" /> 版本介紹
        </span>
      </div>
    </div>

    <div class="booking-notices">
      <h3>注意事項</h3>
      <ul>
        <li>線上即時付款將於交易後，立即由您的信用卡帳戶中進行扣款。</li>
        <li>
          「團體票 / 愛心票 / 敬老票」無法和「一般電影票種 / 早場優惠票 / 學生票 /
          軍警票」同時訂票，請分次訂購。
        </li>
        <li>為維護顧客權益，惡意佔位或影響他人正常訂位使用者，本影城保有調整或取消訂位之權利。</li>
      </ul>
    </div>

    <TicketInfoModal v-model="ticketInfoModalVisible" />
    <VersionInfoModal v-model="versionInfoModalVisible" />
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 訂票流程 - 付款方式選擇子頁面 (PaymentMethodSelection.vue)
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import TicketInfoModal from '../../components/front/TicketInfoModal.vue'
import VersionInfoModal from '@/features/Theaters/components/front/VersionInfoModal.vue'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const router = useRouter()
const route = useRoute()
const seatSelectionStore = usePublicSeatSelectionStore()

// ----------------------------------------------------------------
// 本地響應式狀態 (Local Reactive State)
// ----------------------------------------------------------------
const ticketInfoModalVisible = ref(false)
const versionInfoModalVisible = ref(false)
// [重構] 所有跟 Modal 相關的資料和狀態都已移至子組件

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------
function selectPaymentMethod(method) {
  // 從當前路由獲取 sessionId
  const sessionId = route.params.sessionId

  // 在導航到下一步之前，先呼叫 store 的 action 將支付方式記錄下來
  seatSelectionStore.setPaymentMethod(method)

  // 導航到下一步，並將付款方式作為 query string 附加在 URL 後面
  // 例如： /booking/123/tickets?payment=online
  router.push({
    name: 'TicketSelection', // 跳轉到我們定義的路由名稱
    params: { sessionId },
    query: { payment: method }, // 'online' 或 'counter'
  })
}
</script>

<style scoped>
/* 頁面根容器 */
.payment-method-selection {
  width: 100%;
}

/* 主內容 Flexbox 容器 */
.main-content-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 付款選項容器 (左側區塊) */
.payment-options {
  flex: 1;
  min-width: 0;
}

.payment-options ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 單個選項的樣式 (核心) */
.payment-options li a {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  text-decoration: none;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #2a2a2a;
}

.payment-options li a:hover {
  border-color: #e50914;
  background-color: #555; /* 滑鼠懸停時顏色再亮一點 */
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.4);
}

.icon-wrapper {
  font-size: 40px;
  color: #ccc;
  flex-shrink: 0;
  transition: color 0.3s ease;
}

.payment-options li a:hover .icon-wrapper {
  color: #e50914;
}

.text-wrapper {
  flex-grow: 1;
}

.payment-options h1 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #fff;
  line-height: 1.4;
}
.payment-options h1 strong {
  font-size: 20px;
}
.payment-options h2 {
  margin: 0;
  font-size: 12px;
  color: #888;
  font-weight: normal;
  letter-spacing: 0.5px;
}

/* 垂直分隔線 */
.vertical-divider {
  width: 1px;
  background-color: #444;
  align-self: stretch;
  min-height: 150px;
  opacity: 0.6;
}

/* 資訊介紹側邊欄 (右側區塊) */
.info-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex-basis: 150px;
  flex-shrink: 0;
  padding-top: 20px;
}

.info-link {
  color: #ccc;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: color 0.3s;
}
.info-link:hover {
  color: #e50914;
}

/* 底部注意事項區塊 */
.booking-notices {
  margin-top: 30px; /* 增加與上方內容的間距 */
}

.booking-notices h3 {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 11px;
  color: #f0f0f0;
  display: inline-block;
  padding-bottom: 4px;
}

.booking-notices ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  counter-reset: notices-counter;
}

.booking-notices li {
  font-size: 14px;
  color: #aaa;
  line-height: 1.6;
  padding-left: 25px;
  position: relative;
  counter-increment: notices-counter;
}

.booking-notices li::before {
  content: counter(notices-counter) '.';
  position: absolute;
  left: 0;
  top: 0;
  font-weight: bold;
  width: 20px;
  text-align: right;
  margin-right: 5px;
}
</style>
