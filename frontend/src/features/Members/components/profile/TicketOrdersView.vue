<template>
  <div class="tabbed-booking-history">
    <!-- Tab Navigation -->
    <div class="tabs">
      <button @click="currentTab = 'paid'" :class="{ active: currentTab === 'paid' }">
        已付款
      </button>
      <button @click="currentTab = 'pending'" :class="{ active: currentTab === 'pending' }">
        待付款
      </button>
      <button @click="currentTab = 'cancelled'" :class="{ active: currentTab === 'cancelled' }">
        已取消
      </button>
      <button @click="currentTab = 'past'" :class="{ active: currentTab === 'past' }">
        歷史紀錄
      </button>
    </div>

    <!-- Content Area -->
    <div class="tab-content">
      <!-- Loading Spinner -->
      <div v-if="isLoadingHistory" class="loading-container">
        <LoadingSpinner />
        <p>正在載入您的訂票紀錄...</p>
      </div>

      <!-- Error Message -->
      <div v-else-if="historyError" class="no-records">
        <p>⚠️</p>
        <h2>載入失敗</h2>
        <p>{{ historyError }}</p>
      </div>

      <!-- No Records Message -->
      <div v-else-if="!filteredHistory.length" class="no-records">
        <p>🎬</p>
        <h2>{{ noRecordsMessage }}</h2>
        <p>這裡沒有符合條件的訂單。</p>
      </div>

      <!-- Filtered History List -->
      <div v-else class="history-list">
        <div class="list-header">
          <span class="header-item movie-details">電影資訊</span>
          <span class="header-item session-details">開演時間 / 影廳與座位</span>
          <span class="header-item order-details">訂單狀態與金額</span>
        </div>
        <ul>
          <li v-for="order in filteredHistory" :key="order.ticketOrderId" class="order-item">
            <div class="item-section movie-details">
              <h3 class="movie-title">{{ order.movieTitle }}</h3>
              <span class="order-date">下單於 {{ formatDateTime(order.createdTime) }}</span>
            </div>
            <div class="item-section session-details">
              <p>{{ formatDateTime(order.startTime) }}</p>
              <p>{{ order.theaterName }} / {{ order.seatNumber }}</p>
            </div>
            <div class="item-section order-details">
              <span :class="getStatusClass(order.status)">{{ translateStatus(order.status) }}</span>
              <span class="total-amount">${{ order.totalAmount }}</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePublicMemberProfileStore } from '@/features/Members/store/usePublicMemberProfileStore'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

// --- Store ---
const memberProfileStore = usePublicMemberProfileStore()
const { ticketOrderHistory, isLoadingHistory, historyError } = storeToRefs(memberProfileStore)
const { fetchTicketOrderHistory } = memberProfileStore

// --- Local State for UI ---
const currentTab = ref('paid') // Default to the 'paid' tab

// --- Lifecycle ---
onMounted(() => {
  fetchTicketOrderHistory()
})

// --- Computed Properties for Filtering ---
const filteredHistory = computed(() => {
  const now = new Date()
  if (!ticketOrderHistory.value) return []

  switch (currentTab.value) {
    case 'paid': // Upcoming paid tickets
      return ticketOrderHistory.value.filter(
        (o) => o.status === 'PAID' && new Date(o.startTime) > now,
      )
    case 'pending':
      return ticketOrderHistory.value.filter((o) => o.status === 'PENDING')
    case 'cancelled':
      return ticketOrderHistory.value.filter(
        (o) => o.status === 'CANCELLED' || o.status === 'REFUNDED',
      )
    case 'past': // Past paid tickets
      return ticketOrderHistory.value.filter(
        (o) => (o.status === 'PAID' || o.status === 'COMPLETED') && new Date(o.startTime) <= now,
      )
    default:
      return []
  }
})

const noRecordsMessage = computed(() => {
  switch (currentTab.value) {
    case 'paid':
      return '沒有已付款的電影票'
    case 'pending':
      return '沒有待付款的訂單'
    case 'cancelled':
      return '沒有已取消的訂單'
    case 'past':
      return '沒有歷史訂單紀錄'
    default:
      return '尚無紀錄'
  }
})

// --- Helper Functions ---
const translateStatus = (status) => {
  const statusMap = {
    PAID: '已付款',
    PENDING: '待付款',
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    REFUNDED: '已退款',
  }
  return statusMap[status] || status
}

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return 'N/A'
  return new Date(dateTimeString).toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getStatusClass = (status) => {
  switch (status) {
    case 'PAID':
    case 'COMPLETED':
      return 'status-badge status-completed'
    case 'CANCELLED':
    case 'REFUNDED':
      return 'status-badge status-cancelled'
    case 'PENDING':
      return 'status-badge status-pending'
    default:
      return 'status-badge'
  }
}
</script>

<style scoped>
.tabbed-booking-history {
  padding: 2rem;
  background-color: #1c1c1e;
  color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-height: 100vh;
}

.list-header {
  display: flex;
  justify-content: space-between;
  padding: 0 1.5rem;
  margin-bottom: 1rem;
  color: #8d8d92;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: uppercase;
}

.header-item {
  flex: 1;
}

.header-item.session-details,
.header-item.order-details {
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #fff;
}

.tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid #3a3a3c;
}

.tabs button {
  padding: 0.75rem 0.5rem;
  border: none;
  background: none;
  color: #8d8d92;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    color 0.2s ease,
    border-bottom 0.2s ease;
  border-bottom: 3px solid transparent;
}

.tabs button.active {
  color: #fff;
  border-bottom-color: #e50914;
}

.loading-container,
.no-records {
  text-align: center;
  padding: 4rem 0;
  color: #8d8d92;
}

.no-records h2 {
  font-size: 1.5rem;
  color: #fff;
  margin: 1rem 0 0.5rem;
}
.no-records p:first-of-type {
  font-size: 2.5rem;
}

.history-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #2c2c2e;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1rem;
}

.item-section {
  flex: 1;
}
.item-section p {
  margin: 0.25rem 0;
  color: #aeaeb2;
  font-size: 0.95rem;
}
.movie-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
  margin: 0 0 0.5rem 0;
}
.order-date {
  font-size: 0.8rem;
  color: #8d8d92;
}
.session-details {
  text-align: center;
}
.order-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-weight: 500;
  font-size: 0.85rem;
  color: #fff;
  min-width: 60px;
  text-align: center;
}

.status-completed {
  background-color: #34c759;
}
.status-cancelled {
  background-color: #ff3b30;
}
.status-pending {
  background-color: #ff9500;
}

.total-amount {
  font-size: 1.2rem;
  font-weight: 600;
  color: #fff;
}
</style>
