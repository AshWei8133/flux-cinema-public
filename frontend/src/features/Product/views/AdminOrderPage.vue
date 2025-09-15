<template>
  <div class="admin-orders">
    
    <div class="status-filter-container">
      <label for="status-select">篩選狀態：</label>
      <select id="status-select" v-model="selectedStatus" class="status-select-dropdown">
        <option value="ALL">全部訂單</option>
        <option value="PENDING">待付款</option>
        <option value="PREPARED">準備中</option>
        <option value="PROCESSING">處理中</option>
        <option value="COMPLETED">已完成</option>
        <option value="CANCELLED">已取消</option>
      </select>
    </div>

    <div v-if="isLoading" class="state-message loading-message">
      <p>訂單列表載入中...</p>
    </div>
    <div v-else-if="error" class="state-message error-message">
      <p>載入失敗：{{ error }}</p>
    </div>
    <div v-else-if="filteredOrders.length === 0 && !isLoading && !error" class="state-message no-data-message">
      <p>目前沒有任何符合條件的訂單。</p>
    </div>

    <div v-else class="order-table-wrapper">
      <table class="order-table">
        <thead>
          <tr>
            <th>訂單編號</th>
            <th>訂單時間</th>
            <th>會員ID</th>
            <th>會員帳號</th>
            <th>訂單金額</th>
            <th>最終金額</th>
            <th>付款方式</th>
            <th>訂單狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="order in filteredOrders"
            :key="order.orderId"
            class="order-row"
          >
            <td @click="showOrderDetails(order)" class="clickable-cell">{{ order.orderNumber }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">{{ formatDate(order.orderTime) }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">{{ order?.member?.memberId || 'N/A' }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">{{ order?.member?.username || 'N/A' }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">NT$ {{ formatPrice(order.orderAmount) }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">NT$ {{ formatPrice(order.finalPaymentAmount) }}</td>
            <td @click="showOrderDetails(order)" class="clickable-cell">{{ translateMethod(order.paymentMethod, 'payment') }}</td>
            <td>
              <!-- 狀態下拉選單 -->
              <select 
                v-model="order.orderStatus"
                @change="updateOrderStatus(order)"
                class="inline-status-select"
                :data-status="order.orderStatus"
                :disabled="updatingOrderId === order.orderId"
              >
                <option value="PENDING">待付款</option>
                <option value="PREPARED">準備中</option>
                <option value="PROCESSING">處理中</option>
                <option value="COMPLETED">已完成</option>
                <option value="CANCELLED">已取消</option>
              </select>
            </td>
            <td>
              <el-button 
                type="text" 
                size="small"
                @click="showOrderDetails(order)"
              >
                查看詳情
              </el-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="訂單細節"
      width="60%"
      :before-close="handleCloseDialog"
      class="order-details-dialog"
    >
      <div v-if="selectedOrderDetails" class="dialog-content">
        <h3 class="text-xl font-bold mb-4">訂單編號: #{{ selectedOrderDetails.orderNumber }}</h3>
        <p><strong>訂單時間:</strong> {{ formatDate(selectedOrderDetails.orderTime) }}</p>
        <p><strong>會員 ID:</strong> {{ selectedOrderDetails?.member?.memberId || 'N/A' }}</p>
        <p><strong>會員帳號:</strong> {{ selectedOrderDetails?.member?.username || 'N/A' }}</p>
        <p><strong>訂單總金額:</strong> NT$ {{ formatPrice(selectedOrderDetails.orderAmount) }}</p>
        <p><strong>折扣金額:</strong> NT$ {{ formatPrice(selectedOrderDetails.discountAmount) }}</p>
        <p><strong>最終支付金額:</strong> NT$ {{ formatPrice(selectedOrderDetails.finalPaymentAmount) }}</p>
        <p><strong>付款方式:</strong> {{ translateMethod(selectedOrderDetails.paymentMethod, 'payment') }}</p>
        <p><strong>聯絡信箱:</strong> {{ selectedOrderDetails.customerEmail }}</p>
        <p><strong>訂單狀態:</strong>
          <span :class="getStatusClass(selectedOrderDetails.orderStatus)">
            {{ statusLabel(selectedOrderDetails.orderStatus) }}
          </span>
        </p>

        <h4 class="text-lg font-bold mt-6 mb-3">商品明細:</h4>
        <div v-if="selectedOrderDetails.orderDetails && selectedOrderDetails.orderDetails.length > 0">
          <ul class="order-details-list">
            <li
              v-for="item in selectedOrderDetails.orderDetails"
              :key="item.detailId"
              class="order-detail-item"
            >
              <span class="item-name">{{ item.productName  }}</span> -
              <span class="item-qty">數量: {{ item.quantity }}</span> x
              <span class="item-price">單價: NT$ {{ formatPrice(item.unitPrice) }}</span>
              <span v-if="item.extraPrice > 0" class="item-extra-price">
                (額外費用: NT$ {{ formatPrice(item.extraPrice) }})
              </span>
              <span class="item-subtotal"> = 小計: NT$ {{ formatPrice(item.subtotal) }}</span>
            </li>
          </ul>
        </div>
        <p v-else class="text-gray-600">此訂單沒有商品明細。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">關閉</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import httpClient from "@/services/api.js";
import { ElDialog, ElButton, ElMessage, ElMessageBox } from 'element-plus';

const orders = ref([]);
const selectedStatus = ref("ALL");
const isLoading = ref(false);
const error = ref(null);
const updatingOrderId = ref(null);
const originalStatuses = ref(new Map());

const dialogVisible = ref(false);
const selectedOrderDetails = ref(null);

const statusMap = {
  PENDING: '待付款',
  PREPARED: '準備中',
  PROCESSING: '處理中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

const paymentMethodMap = {
  CREDIT_CARD: '信用卡',
  CASH: '現金',
  LINE_PAY: 'Line Pay',
};

const statusLabel = (status) => statusMap[status] || status;

const translateMethod = (method, type) => {
  if (type === 'payment') return paymentMethodMap[method] || method;
  return method;
};

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'status-badge pending';
    case 'PREPARED': return 'status-badge prepared';
    case 'PROCESSING': return 'status-badge processing';
    case 'COMPLETED': return 'status-badge completed';
    case 'CANCELLED': return 'status-badge cancelled';
    default: return 'status-badge';
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
};

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0';
  return price.toLocaleString('zh-TW');
};

const filteredOrders = computed(() => {
  const currentOrders = orders.value || [];
  if (selectedStatus.value === "ALL") return currentOrders;
  return currentOrders.filter(order => order.orderStatus === selectedStatus.value);
});

async function fetchOrders() {
  isLoading.value = true;
  error.value = null;
  try {
    const res = await httpClient.get("/admin/order");
    // 確保每個訂單都有 orderStatus，如果是 null 就設為 PENDING
    orders.value = res.map(order => ({
      ...order,
      orderStatus: order.orderStatus || 'PENDING'
    }));
    
    // 記錄原始狀態
    orders.value.forEach(order => {
      originalStatuses.value.set(order.orderId, order.orderStatus);
    });
  } catch (err) {
    console.error("獲取訂單失敗:", err);
    error.value = '無法載入訂單資料，請檢查網路或伺服器。';
  } finally {
    isLoading.value = false;
  }
}

const updateOrderStatus = async (order) => {
  const newStatus = order.orderStatus;
  const originalStatus = originalStatuses.value.get(order.orderId);

  if (newStatus === originalStatus) return;

  // 顯示更新中狀態
  updatingOrderId.value = order.orderId;
  
  try {
    // 設置超時時間
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000); // 3秒超時
    
    const response = await httpClient.put(
      `/admin/order/${order.orderId}/status`, 
      { newStatus },
      { signal: controller.signal }
    );
    
    clearTimeout(timeoutId);
    
    // 成功更新
    originalStatuses.value.set(order.orderId, newStatus);
    ElMessage.success(`訂單 #${order.orderNumber} 狀態已更新為「${statusLabel(newStatus)}」`);
    
  } catch (err) {
    if (err.name === 'AbortError') {
      ElMessage.warning('更新請求超時，請稍後重試');
    } else {
      ElMessage.error('訂單狀態更新失敗');
    }
    
    // 回滾狀態
    order.orderStatus = originalStatus;
    console.error('更新失敗:', err);
  } finally {
    updatingOrderId.value = null;
  }
};
  

const showOrderDetails = (order) => {
  selectedOrderDetails.value = order;
  dialogVisible.value = true;
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
  selectedOrderDetails.value = null;
};

onMounted(fetchOrders);
</script>

<style scoped>
.admin-orders {
  padding: 30px;
  max-width: 1400px;
  margin: 40px auto;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  font-family: 'Inter', sans-serif;
}

h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #343a40;
  margin-bottom: 25px;
  text-align: center;
}

.status-filter-container {
  margin-bottom: 25px;
  text-align: center;
}

.status-filter-container label {
  font-size: 1.1rem;
  color: #555;
  margin-right: 10px;
}

.status-select-dropdown {
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  background-color: white;
  font-size: 1rem;
  color: #495057;
  cursor: pointer;
  appearance: none;
  background-image: url('data:image/svg+xml;utf8,<svg fill="black" height="24" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M7 10l5 5 5-5z"/><path d="M0 0h24v24H0z" fill="none"/></svg>');
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 18px;
  padding-right: 35px;
}

.status-select-dropdown:focus {
  outline: none;
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.state-message {
  padding: 40px;
  text-align: center;
  font-size: 1.2rem;
  color: #6c757d;
  background-color: #e9ecef;
  border-radius: 8px;
  margin-top: 20px;
}

.loading-message {
  color: #007bff;
}

.error-message {
  color: #dc3545;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
}

.no-data-message {
  color: #6c757d;
  background-color: #f8f9fa;
  border: 1px solid #e2e6ea;
}

.order-table-wrapper {
  overflow-x: auto;
  margin-top: 20px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.order-table {
  width: 100%;
  border-collapse: collapse;
}

.order-table th,
.order-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
}

.order-table th {
  background-color: #e9ecef;
  font-weight: 600;
  color: #495057;
  text-transform: uppercase;
  font-size: 0.9rem;
}

.order-table tbody tr:last-child td {
  border-bottom: none;
}

.order-table tbody tr.order-row:hover {
  background-color: #f8f9fa;
}

.clickable-cell {
  cursor: pointer;
}

.clickable-cell:hover {
  background-color: #e0f2f7;
}

.order-table td {
  font-size: 0.95rem;
  color: #343a40;
}

/* 行內狀態選單樣式 */
.inline-status-select {
  padding: 6px 10px;
  border: 2px solid;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  min-width: 120px;
  transition: all 0.3s;
}

/* 根據不同狀態顯示不同顏色 */
.inline-status-select[data-status="PENDING"] {
  background-color: #fff3e0;
  border-color: #ff9800;
  color: #e65100;
}

.inline-status-select[data-status="PREPARED"] {
  background-color: #e3f2fd;
  border-color: #2196f3;
  color: #1565c0;
}

.inline-status-select[data-status="PROCESSING"] {
  background-color: #e8eaf6;
  border-color: #3f51b5;
  color: #283593;
}

.inline-status-select[data-status="COMPLETED"] {
  background-color: #e8f5e9;
  border-color: #4caf50;
  color: #2e7d32;
}

.inline-status-select[data-status="CANCELLED"] {
  background-color: #ffebee;
  border-color: #f44336;
  color: #c62828;
}

.inline-status-select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.inline-status-select:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 下拉選項的顏色 */
.inline-status-select option[value="PENDING"] { background-color: #ffe0b2; color: #e65100; }
.inline-status-select option[value="PREPARED"] { background-color: #e1f5fe; color: #01579b; }
.inline-status-select option[value="PROCESSING"] { background-color: #bbdefb; color: #1976d2; }
.inline-status-select option[value="COMPLETED"] { background-color: #a5d6a7; color: #2e7d32; }
.inline-status-select option[value="CANCELLED"] { background-color: #ef9a9a; color: #c62828; }

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  display: inline-block;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.status-badge.pending { background-color: #ffe0b2; color: #e65100; }
.status-badge.prepared { background-color: #e1f5fe; color: #01579b; }
.status-badge.processing { background-color: #bbdefb; color: #1976d2; }
.status-badge.completed { background-color: #a5d6a7; color: #2e7d32; }
.status-badge.cancelled { background-color: #ef9a9a; color: #c62828; }

.dialog-content p {
  margin-bottom: 8px;
  font-size: 1rem;
  color: #343a40;
}

.dialog-content p strong {
  color: #0056b3;
}

.order-details-list {
  list-style-type: none;
  padding-left: 0;
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.order-detail-item {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 5px;
  padding: 10px 15px;
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  font-size: 0.95rem;
}

.order-detail-item .item-name { font-weight: bold; color: #343a40; }
.order-detail-item .item-qty,
.order-detail-item .item-price,
.order-detail-item .item-extra-price,
.order-detail-item .item-subtotal { color: #6c757d; }
.order-detail-item .item-subtotal { font-weight: bold; color: #007bff; }

.dialog-footer { text-align: right; padding-top: 20px; }
</style>