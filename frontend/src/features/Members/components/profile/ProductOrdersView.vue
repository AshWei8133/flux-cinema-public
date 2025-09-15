<template>
  <div class="orders-history-container">
    <!-- Tab Navigation -->
    <div class="tabs">
      <button @click="selectedStatus = 'ALL'" :class="{ active: selectedStatus === 'ALL' }">
        全部訂單
      </button>
      <button
        @click="selectedStatus = 'PROCESSING'"
        :class="{ active: selectedStatus === 'PROCESSING' }"
      >
        處理中
      </button>
      <button
        @click="selectedStatus = 'COMPLETED'"
        :class="{ active: selectedStatus === 'COMPLETED' }"
      >
        已完成
      </button>
      <button
        @click="selectedStatus = 'CANCELLED'"
        :class="{ active: selectedStatus === 'CANCELLED' }"
      >
        已取消
      </button>
    </div>

    <!-- Content Area -->
    <div class="content-area">
      <div v-if="isLoading" class="loading-container">
        <LoadingSpinner />
        <p>正在載入您的購買紀錄...</p>
      </div>
      <div v-else-if="error" class="info-container">
        <p class="icon">⚠️</p>
        <h2>載入失敗</h2>
        <p>{{ error }}</p>
      </div>
      <div v-else-if="!filteredOrders.length" class="info-container">
        <p class="icon">🛍️</p>
        <h2>{{ noRecordsMessage }}</h2>
        <p>這裡沒有符合條件的訂單。</p>
      </div>

      <!-- Orders Table -->
      <div v-else class="table-wrapper">
        <table class="orders-table">
          <thead>
            <tr>
              <th>訂單編號</th>
              <th>訂單時間</th>
              <th>摘要</th>
              <th>總金額</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.orderId">
              <td>{{ order.orderNumber }}</td>
              <td>{{ formatDateTime(order.orderTime) }}</td>
              <td>{{ getOrderSummary(order.orderDetails) }}</td>
              <td>NT$ {{ order.finalPaymentAmount.toLocaleString() }}</td>
              <td>
                <span :class="getStatusClass(order.orderStatus)">{{
                  translateStatus(order.orderStatus)
                }}</span>
              </td>
              <td>
                <button class="details-btn" @click="showOrderDetails(order)">查看詳情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Details Dialog (Modal) -->
    <el-dialog v-model="isDialogVisible" title="訂單詳情" width="60%" class="details-dialog">
      <div v-if="selectedOrder" class="dialog-content">
        <div class="order-summary">
          <p><strong>訂單編號:</strong> {{ selectedOrder.orderNumber }}</p>
          <p><strong>訂單時間:</strong> {{ formatDateTime(selectedOrder.orderTime) }}</p>
          <p><strong>付款方式:</strong> {{ translateMethod(selectedOrder.paymentMethod) }}</p>
          <p>
            <strong>訂單狀態:</strong>
            <span :class="getStatusClass(selectedOrder.orderStatus)">{{
              translateStatus(selectedOrder.orderStatus)
            }}</span>
          </p>
        </div>

        <h4 class="details-title">商品明細</h4>
        <el-table :data="selectedOrder.orderDetails" class="details-table">
          <el-table-column label="商品" min-width="250">
            <template #default="scope">
              <div class="product-cell">
                <el-image :src="scope.row.productInfo.imageUrl" fit="cover" class="product-image">
                  <template #error
                    ><div class="image-slot"><img src="@/assets/images/noimg.png" /></div
                  ></template>
                </el-image>
                <span>{{ scope.row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="單價" align="right">
            <template #default="scope">NT$ {{ scope.row.unitPrice.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="數量" align="center" />
          <el-table-column label="小計" align="right">
            <template #default="scope">NT$ {{ scope.row.subtotal.toLocaleString() }}</template>
          </el-table-column>
        </el-table>

        <div class="order-total">
          <p><strong>訂單總計:</strong> NT$ {{ selectedOrder.orderAmount.toLocaleString() }}</p>
          <p><strong>折扣金額:</strong> NT$ {{ selectedOrder.discountAmount.toLocaleString() }}</p>
          <p>
            <strong>最終付款金額:</strong>
            <span>NT$ {{ selectedOrder.finalPaymentAmount.toLocaleString() }}</span>
          </p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button
            v-if="isCancelable"
            class="cancel-btn"
            @click="handleCancelOrder"
            :disabled="isCancelling"
          >
            {{ isCancelling ? '取消中...' : '取消訂單' }}
          </button>
          <button class="close-btn" @click="isDialogVisible = false">關閉</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import httpClient from '@/services/api.js'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { ElDialog, ElTable, ElTableColumn, ElImage, ElMessageBox, ElMessage } from 'element-plus'

// --- Local Component State ---
const orders = ref([])
const isLoading = ref(true)
const error = ref(null)
const selectedStatus = ref('ALL')
const isDialogVisible = ref(false)
const selectedOrder = ref(null)
const isCancelling = ref(false)

// --- Data Fetching ---
async function fetchMemberOrders() {
  isLoading.value = true
  error.value = null
  try {
    const response = await httpClient.get('/membercenter/orders/products')
    orders.value = response
  } catch (err) {
    console.error('Failed to fetch member orders:', err)
    error.value = '無法載入您的訂單紀錄，請稍後再試。'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchMemberOrders()
})

// --- Computed Properties ---
const filteredOrders = computed(() => {
  if (!Array.isArray(orders.value)) return []
  if (selectedStatus.value === 'ALL') return orders.value
  if (selectedStatus.value === 'PROCESSING') {
    return orders.value.filter((o) => o.orderStatus === 'PENDING' || o.orderStatus === 'PROCESSING')
  }
  return orders.value.filter((o) => o.orderStatus === selectedStatus.value)
})

const noRecordsMessage = computed(() => {
  const statusMap = {
    ALL: '尚無任何購買紀錄',
    PROCESSING: '沒有處理中的訂單',
    COMPLETED: '沒有已完成的訂單',
    CANCELLED: '沒有已取消的訂單',
  }
  return statusMap[selectedStatus.value] || '尚無紀錄'
})

const isCancelable = computed(() => {
  if (!selectedOrder.value) return false
  const cancelableStatus = ['PENDING', 'PROCESSING', 'PREPARED']
  return cancelableStatus.includes(selectedOrder.value.orderStatus)
})

// --- UI Methods ---
const showOrderDetails = (order) => {
  selectedOrder.value = order
  isDialogVisible.value = true
}

const handleCancelOrder = async () => {
  if (!selectedOrder.value) return

  try {
    await ElMessageBox.confirm('您確定要取消這筆訂單嗎？此操作無法復原。', '確認取消訂單', {
      confirmButtonText: '確定取消',
      cancelButtonText: '考慮一下',
      type: 'warning',
    })

    isCancelling.value = true
    const orderId = selectedOrder.value.orderId
    const response = await httpClient.put(`/membercenter/orders/${orderId}/cancel`)

    // Update local data
    const index = orders.value.findIndex((o) => o.orderId === orderId)
    if (index !== -1) {
      orders.value[index] = response // Assuming backend returns the updated order
    }

    ElMessage.success('訂單已成功取消')
    isDialogVisible.value = false
  } catch (err) {
    if (err !== 'cancel') {
      // Axios interceptor will show the main error message
      console.error('Failed to cancel order:', err)
    }
  } finally {
    isCancelling.value = false
  }
}

// --- Helper Functions ---
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

const getOrderSummary = (details) => {
  if (!details || details.length === 0) return 'N/A'
  const firstItemName = details[0].productName
  return details.length > 1 ? `${firstItemName} 等 ${details.length} 項商品` : firstItemName
}

const statusMap = {
  PENDING: '處理中',
  PROCESSING: '處理中',
  PREPARED: '準備中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}
const translateStatus = (status) => statusMap[status] || status

const paymentMethodMap = { CREDIT_CARD: '信用卡', CASH: '現金', LINE_PAY: 'Line Pay' }
const translateMethod = (method) => paymentMethodMap[method] || method

const getStatusClass = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'status-badge status-completed'
    case 'PENDING':
    case 'PROCESSING':
    case 'PREPARED':
      return 'status-badge status-pending'
    case 'CANCELLED':
      return 'status-badge status-cancelled'
    default:
      return 'status-badge'
  }
}
</script>

<style scoped>
/* Base container and theme */
.orders-history-container {
  padding: 2rem;
  background-color: #1c1c1e;
  color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-height: 100vh;
}
.page-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #fff;
}

/* Tabs */
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

/* Loading/Info States */
.loading-container,
.info-container {
  text-align: center;
  padding: 4rem 0;
  color: #8d8d92;
}
.info-container h2 {
  font-size: 1.5rem;
  color: #fff;
  margin: 1rem 0 0.5rem;
}
.info-container .icon {
  font-size: 2.5rem;
}

/* Table Styles */
.table-wrapper {
  overflow-x: auto;
}
.orders-table {
  width: 100%;
  border-collapse: collapse;
  color: #f5f5f7;
}
.orders-table th,
.orders-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #3a3a3c;
}
.orders-table th {
  font-size: 0.875rem;
  color: #8d8d92;
  text-transform: uppercase;
}
.orders-table td {
  font-size: 0.95rem;
}

/* Details Button */
.details-btn {
  background-color: #444;
  color: #fff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}
.details-btn:hover {
  background-color: #555;
}

/* Status Badge */
.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-weight: 500;
  font-size: 0.85rem;
  color: #fff;
  min-width: 60px;
  text-align: center;
  display: inline-block;
}
.status-completed {
  background-color: #34c759;
}
.status-cancelled {
  background-color: #ff3b30;
}
.status-pending {
  background-color: #e6a23c;
}

/* Dialog (Modal) Styles */
:deep(.el-dialog) {
  background-color: #2c2c2e;
  color: #f5f5f7;
  border-radius: 12px;
}
:deep(.el-dialog__title) {
  color: #fff;
}
:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.dialog-content {
  color: #f5f5f7;
}
.order-summary {
  background-color: #3a3a3c;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}
.order-summary p {
  margin: 0.5rem 0;
}
.details-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #fff;
  border-bottom: 1px solid #4a4a4c;
  padding-bottom: 0.5rem;
}

.details-table {
  --el-table-border-color: #4a4a4c;
  --el-table-header-bg-color: #3a3a3c;
  --el-table-header-text-color: #f5f5f7;
  --el-table-bg-color: #2c2c2e;
  --el-table-tr-bg-color: #2c2c2e;
  --el-table-row-hover-bg-color: #3a3a3c;
  --el-table-text-color: #f5f5f7;
  --el-table-border: 1px solid #4a4a4c;
  border-radius: 8px;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.product-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  flex-shrink: 0;
  background-color: #3a3a3c;
}
.image-slot img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-total {
  margin-top: 1.5rem;
  text-align: right;
  padding-top: 1rem;
  border-top: 1px solid #4a4a4c;
}
.order-total p {
  margin: 0.5rem 0;
  font-size: 1rem;
}
.order-total span {
  font-weight: 600;
  font-size: 1.2rem;
  color: #e50914;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
.close-btn {
  background-color: #555;
  color: #fff;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}
.close-btn:hover {
  background-color: #666;
}
.cancel-btn {
  background-color: #f56c6c;
  color: #fff;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}
.cancel-btn:hover {
  background-color: #f78989;
}
.cancel-btn:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}
</style>
