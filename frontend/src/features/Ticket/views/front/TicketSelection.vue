<template>
  <div class="ticket-selection-container">
    <div v-if="isPageLoading" class="status-info">讀取票價資訊中...</div>
    <div v-else-if="pageError" class="status-info error">頁面資料載入失敗：{{ pageError }}</div>
    <div v-else-if="sessionInfo" class="ticket-selection-page">
      <div class="ticket-options-container">
        <div class="current-version-info">
          您正在預訂 <strong>{{ sessionInfo.version }}</strong> 版本的電影票
        </div>

        <div v-if="!basePriceFound" class="no-tickets-info">
          <p>抱歉，此影廳類型({{ sessionInfo.version }})未設定基準票價，無法進行訂票。</p>
        </div>
        <div v-else-if="availableTickets.length === 0" class="no-tickets-info">
          <p>此影廳版本或時段未提供符合您選擇的票種。</p>
          <p v-if="paymentMethod === 'online' && !isEarlyBirdSession">
            (早場優惠票僅於每日中午 12:00 前的場次提供)
          </p>
        </div>
        <div v-for="ticket in availableTickets" :key="ticket.ticketTypeId" class="ticket-item">
          <div class="ticket-info">
            <h3 class="ticket-name">{{ ticket.name }}</h3>
            <p class="ticket-description">{{ ticket.description }}</p>
          </div>
          <div class="ticket-price">NT$ {{ ticket.price }}</div>
          <div class="ticket-quantity">
            <QuantitySelector
              v-model="selectedTickets[ticket.ticketTypeId]"
              :max="
                (selectedTickets[ticket.ticketTypeId] || 0) + (TOTAL_TICKET_LIMIT - totalTickets)
              "
            />
          </div>
        </div>
      </div>

      <div class="order-summary-container">
        <div class="summary-card">
          <h3 class="summary-title">訂單摘要</h3>
          <div class="summary-content">
            <div
              v-for="ticket in selectedTicketDetails"
              :key="ticket.ticketTypeId"
              class="summary-item"
            >
              <span>{{ ticket.name }} x {{ ticket.quantity }}</span>
              <span>NT$ {{ ticket.quantity * ticket.price }}</span>
            </div>
            <div v-if="totalTickets === 0" class="empty-summary">尚未選擇任何票券</div>
          </div>
          <div class="summary-footer">
            <div v-if="totalTickets >= TOTAL_TICKET_LIMIT" class="limit-reached-message">
              單筆訂單最多選擇 {{ TOTAL_TICKET_LIMIT }} 張票
            </div>
            <div class="total-price">
              <span>總計</span>
              <span>NT$ {{ totalPrice }}</span>
            </div>
            <button class="next-step-btn" :disabled="isNextButtonDisabled" @click="goToNextStep">
              下一步：選擇座位
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 訂票流程 - 票種選擇子頁面 (TicketSelection.vue)
 *
 * - [v8] 更新說明：
 * 1. 新增訂單總票數上限 (TOTAL_TICKET_LIMIT)。
 * 2. 修改傳遞給 QuantitySelector 的 :max 屬性，使其動態計算，實現整筆訂單的總量控制。
 * 3. 新增 UI 提示，告知使用者已達票數上限。
 */

// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'
import { storeToRefs } from 'pinia'
import QuantitySelector from '../../components/front/QuantitySelector.vue'
import { usePublicTicketStore } from '../../store/usePublicTicketStore'

// ----------------------------------------------------------------
// 初始化與實例化 (Initialization & Instantiation)
// ----------------------------------------------------------------
const route = useRoute()
const router = useRouter()
const seatSelectionStore = usePublicSeatSelectionStore()
const ticketStore = usePublicTicketStore()

// ----------------------------------------------------------------
// Pinia Store 狀態管理
// ----------------------------------------------------------------
const {
  sessionInfo,
  isLoading: isSessionLoading,
  error: sessionError,
} = storeToRefs(seatSelectionStore)

const {
  ticketTypes: rawTicketTypes,
  basePrices,
  isLoading: isTicketDataLoading,
  error: ticketDataError,
} = storeToRefs(ticketStore)

// ----------------------------------------------------------------
// 常數定義 (Constants)
// ----------------------------------------------------------------
// [修改重點 3] 定義一個常數來管理訂單總票數上限，方便未來修改
const TOTAL_TICKET_LIMIT = 10

// ----------------------------------------------------------------
// 本地響應式狀態 (Local Reactive State)
// ----------------------------------------------------------------
const selectedTickets = ref({})
const paymentMethod = ref(route.query.payment)
const basePriceFound = ref(true)

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------
const isPageLoading = computed(() => isSessionLoading.value || isTicketDataLoading.value)
const pageError = computed(() => sessionError.value || ticketDataError.value)

const ticketTypes = computed(() => {
  // 【修正】增加防禦性檢查，確保 rawTicketTypes.value 存在且是陣列
  if (!rawTicketTypes.value || !Array.isArray(rawTicketTypes.value)) {
    return []
  }
  return rawTicketTypes.value.map((ticket) => ({
    ...ticket,
    paymentChannel: [1, 5].includes(ticket.ticketTypeId) ? 'online' : 'counter',
    isEarlyBird: ticket.ticketTypeId === 5,
  }))
})

const isEarlyBirdSession = computed(() => {
  if (!sessionInfo.value?.showtime) return false
  const hour = new Date(sessionInfo.value.showtime).getHours()
  return hour < 12
})

const availableTickets = computed(() => {
  // 【防禦性檢查】確保計算所需的核心資料 (sessionInfo, basePrices) 都已載入。
  // 如果任何一項不存在，就立即返回空陣列，防止後續程式碼因讀取 undefined 而崩潰。
  // 當這些資料透過非同步請求回來後，Vue 的響應式系統會自動觸發此 computed 重新計算。
  if (!sessionInfo.value || !basePrices.value) {
    return []
  }

  const currentTheaterTypeId = sessionInfo.value?.theaterTypeId
  if (!paymentMethod.value || !currentTheaterTypeId) return []
  const basePrice = basePrices.value[currentTheaterTypeId]
  if (basePrice === undefined || basePrice === null) {
    basePriceFound.value = false
    return []
  }
  basePriceFound.value = true
  const potentialTickets = ticketTypes.value.filter((ticket) => {
    if (!ticket.isEnabled) return false
    if (ticket.paymentChannel !== paymentMethod.value) return false
    if (ticket.isEarlyBird && !isEarlyBirdSession.value) return false
    return true
  })
  const pricedTickets = potentialTickets.map((ticket) => {
    let finalPrice = basePrice
    switch (ticket.discountType) {
      case 'FIXED':
        finalPrice = basePrice + ticket.discountValue
        break
      case 'PERCENTAGE':
        finalPrice = basePrice * ticket.discountValue
        break
      default:
        finalPrice = basePrice
        break
    }
    finalPrice = Math.round(finalPrice)
    return {
      ticketTypeId: ticket.ticketTypeId,
      name: ticket.ticketTypeName,
      description: ticket.description,
      price: finalPrice,
    }
  })
  return pricedTickets
})

watch(
  availableTickets,
  (newTickets) => {
    const newSelection = {}
    newTickets.forEach((ticket) => {
      newSelection[ticket.ticketTypeId] = selectedTickets.value[ticket.ticketTypeId] || 0
    })
    selectedTickets.value = newSelection
  },
  { immediate: true },
)

const selectedTicketDetails = computed(() => {
  return availableTickets.value
    .map((ticket) => ({
      ...ticket,
      quantity: selectedTickets.value[ticket.ticketTypeId] || 0,
    }))
    .filter((ticket) => ticket.quantity > 0)
})

const totalTickets = computed(() => {
  return Object.values(selectedTickets.value).reduce((sum, qty) => sum + qty, 0)
})

const totalPrice = computed(() => {
  return selectedTicketDetails.value.reduce(
    (total, ticket) => total + ticket.price * ticket.quantity,
    0,
  )
})

const isNextButtonDisabled = computed(() => totalTickets.value === 0 || !basePriceFound.value)

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------
function goToNextStep() {
  if (isNextButtonDisabled.value) return
  if (totalTickets.value > TOTAL_TICKET_LIMIT) {
    alert(`單筆訂單總票數不可超過 ${TOTAL_TICKET_LIMIT} 張！`)
    return
  }

  // 1. 組合訂單資訊物件
  const orderDetails = {
    tickets: selectedTicketDetails.value,
    totalCount: totalTickets.value,
    totalPrice: totalPrice.value,
  }

  // 2. 呼叫 store 的 action 將資訊存起來
  seatSelectionStore.setTicketOrder(orderDetails)

  // 3. 前往座位選擇頁面
  router.push({
    name: 'SeatSelection', // 確保你的 router 有設定這個 name
    params: { sessionId: route.params.sessionId },
    query: route.query,
  })
}

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------
onMounted(() => {
  ticketStore.fetchAllTicketTypes()
  ticketStore.fetchBasePrices()
})
</script>

<style scoped>
/* 樣式完全保留，只需新增提示訊息的樣式 */
.status-info {
  width: 100%;
  padding: 60px 20px;
  text-align: center;
  font-size: 18px;
  color: #aaa;
  background-color: #2a2a2a;
  border-radius: 8px;
  border: 1px dashed #444;
}
.status-info.error {
  color: #ff8a80;
  border-color: #ff8a80;
}
.ticket-selection-page {
  display: flex;
  gap: 24px;
  width: 100%;
}
.ticket-options-container {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.current-version-info {
  text-align: center;
  padding: 12px;
  background-color: rgba(229, 9, 20, 0.1);
  border: 1px solid rgba(229, 9, 20, 0.3);
  color: #f0f0f0;
  border-radius: 8px;
  margin-bottom: 8px;
}
.ticket-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #2a2a2a;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #3c3c3c;
  transition:
    border-color 0.3s,
    box-shadow 0.3s;
}
.ticket-item:hover {
  border-color: #e50914;
  box-shadow: 0 4px 15px rgba(229, 9, 20, 0.2);
}
.ticket-info {
  flex-grow: 1;
}
.ticket-name {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #fff;
}
.ticket-description {
  margin: 0;
  font-size: 14px;
  color: #aaa;
}
.ticket-price {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  flex-shrink: 0;
  width: 100px;
  text-align: right;
}
.ticket-quantity {
  flex-shrink: 0;
}
.no-tickets-info {
  background-color: #2a2a2a;
  padding: 40px;
  border-radius: 8px;
  text-align: center;
  color: #aaa;
  border: 1px dashed #444;
}
.order-summary-container {
  flex: 2;
}
.summary-card {
  background-color: #2a2a2a;
  border-radius: 8px;
  border: 1px solid #3c3c3c;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.summary-title {
  padding: 16px 20px;
  margin: 0;
  font-size: 18px;
  color: #fff;
  background-color: #333;
  border-bottom: 1px solid #3c3c3c;
}
.summary-content {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  color: #ccc;
}
.empty-summary {
  text-align: center;
  color: #777;
  padding: 30px 0;
}
.summary-footer {
  padding: 20px;
  border-top: 1px solid #3c3c3c;
  background-color: #333;
}
.total-price {
  display: flex;
  justify-content: space-between;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16px;
}
.next-step-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: bold;
  background-color: #e50914;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.next-step-btn:hover:not(:disabled) {
  background-color: #f40612;
}
.next-step-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
  opacity: 0.7;
}

/* [修改重點 4] 新增提示訊息的樣式 */
.limit-reached-message {
  color: #e6a23c; /* Element Plus 的 warning 顏色 */
  font-size: 14px;
  text-align: center;
  margin-bottom: 12px;
}
</style>
