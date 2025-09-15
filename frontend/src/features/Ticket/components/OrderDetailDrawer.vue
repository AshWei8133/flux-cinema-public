<template>
  <el-drawer
    v-model="isDrawerVisible"
    :title="`訂單詳情 - #${order?.orderNumber}`"
    direction="rtl"
    size="50%"
    with-header
  >
    <div v-if="order" v-loading="loading" class="drawer-content">
      <el-descriptions title="訂單摘要" :column="2" border>
        <el-descriptions-item label="訂單編號">{{ order.orderNumber }}</el-descriptions-item>

        <el-descriptions-item label="訂單狀態">
          <el-tag :type="getOrderStatusInfo(order.status).tagType">
            {{ getOrderStatusInfo(order.status).text }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="會員帳號">{{ order.member.username }}</el-descriptions-item>

        <el-descriptions-item label="會員手機">{{ order.member.phone }}</el-descriptions-item>

        <el-descriptions-item label="訂購時間">
          {{ formatDateTime(order.createdTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="付款時間">{{
          formatDateTime(order.paymentTime)
        }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="金額資訊" :column="2" border class="descriptions-margin-top">
        <el-descriptions-item label="票券總額">
          $ {{ order.totalTicketAmount.toLocaleString() }}
        </el-descriptions-item>

        <el-descriptions-item label="使用優惠券">
          {{ order.coupon?.couponName || '無' }}
        </el-descriptions-item>

        <el-descriptions-item label="總折扣金額"
          >$ {{ order.totalDiscount.toLocaleString() }}</el-descriptions-item
        >

        <el-descriptions-item label="實付總額" label-class-name="total-amount-label">
          <span class="total-amount-value">$ {{ order.totalAmount.toLocaleString() }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="details-section">
        <h3>訂單明細</h3>
        <el-table :data="order.ticketOrderDetails" border>
          <el-table-column label="電影名稱" prop="movieTitle" />
          <el-table-column label="場次時間" width="160">
            <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="影廳" prop="theaterName" width="100" />
          <el-table-column label="座位" prop="seatPosition" width="80" />
          <el-table-column label="票種" prop="ticketTypeName" width="100" />
          <el-table-column label="單價" prop="unitPrice" width="80" align="right">
            <template #default="scope">$ {{ scope.row.unitPrice }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button
          v-if="order?.status === 'PENDING'"
          type="success"
          :icon="Money"
          @click="handlePayment"
        >
          手動付款
        </el-button>

        <div v-if="order?.status === 'PAID'">
          <el-popconfirm
            v-if="isRefundable"
            title="您確定要為此訂單辦理退款嗎？"
            confirm-button-text="確定退款"
            cancel-button-text="取消"
            width="250"
            @confirm="handleRefund"
          >
            <template #reference>
              <el-button type="danger" :icon="CircleClose"> 訂單退款 </el-button>
            </template>
          </el-popconfirm>

          <el-tooltip v-else content="已超過可退款時間 (場次開始前10分鐘)" placement="top">
            <div class="tooltip-wrapper">
              <el-button type="danger" :icon="CircleClose" disabled> 訂單退款 </el-button>
            </div>
          </el-tooltip>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
// 從 vue 引入 computed 計算屬性
import { computed } from 'vue'
// 從 Element Plus 引入 ElMessage 全域訊息提示
import { ElMessage } from 'element-plus'
// 引入圖示
import { Money, CircleClose } from '@element-plus/icons-vue'
// 引入我們自訂的工具函式
import { formatDateTime } from '@/utils/dateUtils.js'
import { getOrderStatusInfo } from '@/utils/statusMapping.js'
// 引入 Pinia store
import { useAdminTicketOrderStore } from '../store/useAdminTicketOrderStore'

// 定義此元件可以接收的 props
const props = defineProps({
  modelValue: { type: Boolean, default: false }, // 對應 v-model，控制抽屜顯示/隱藏
  order: { type: Object, default: null }, // 從父元件傳入的訂單詳細資料物件
  loading: { type: Boolean, default: false }, // 是否顯示載入中狀態
})
// 定義此元件可以發射的事件
const emit = defineEmits(['update:modelValue', 'update:order'])

// 實例化 store
const orderStore = useAdminTicketOrderStore()

// 建立一個計算屬性來代理 props.modelValue，這是實現元件 v-model 的標準做法
const isDrawerVisible = computed({
  get: () => props.modelValue, // 讀取時，回傳 prop 的值
  set: (value) => emit('update:modelValue', value), // 寫入時，發射 update:modelValue 事件通知父元件更新
})

// 計算屬性：判斷當前訂單是否可以退款
const isRefundable = computed(() => {
  // 防呆判斷：如果沒有訂單資料、訂單沒有明細、或訂單狀態不是「已付款」，直接回傳 false
  if (
    !props.order ||
    !props.order.ticketOrderDetails ||
    props.order.ticketOrderDetails.length === 0 ||
    props.order.status !== 'PAID'
  ) {
    return false
  }

  // 取得第一張票的開演時間 (假設同一訂單的場次時間都相同)
  const startTimeString = props.order.ticketOrderDetails[0].startTime
  if (!startTimeString) {
    return false
  }

  // 核心邏輯：判斷當前時間是否在「可退款的截止時間」之前
  const startTime = new Date(startTimeString) // 電影開演時間
  const now = new Date() // 當前時間
  // 計算退款截止時間（開演時間 - 10分鐘）
  const refundDeadline = new Date(startTime.getTime() - 10 * 60 * 1000)
  // 如果當前時間 < 截止時間，則表示可以退款
  return now < refundDeadline
})

// 處理手動付款的異步函式
const handlePayment = async () => {
  if (!props.order) return // 防呆
  try {
    // 呼叫 store 中的 action 來發送 API 請求
    const response = await orderStore.markOrderAsPaid(props.order.orderNumber, {
      paymentType: 'Cash', // 假設手動付款都是現金
    })
    // 如果 API 回應成功
    if (response.success) {
      ElMessage.success(response.message || '訂單已成功標記為已付款！')

      // 發射 'update:order' 事件，通知父元件：「嘿，資料更新了，你該重新整理列表了！」
      emit('update:order')
      // 關閉抽屜
      isDrawerVisible.value = false
    }
  } catch (error) {
    // 錯誤訊息通常由統一的 Axios 攔截器處理，這裡只在控制台印出錯誤以供開發除錯
    console.error('手動付款失敗:', error)
  }
}

// 處理退款的異步函式
const handleRefund = async () => {
  // 雙重保險：再次確認是否符合退款條件
  if (!isRefundable.value || !props.order) {
    ElMessage.warning('訂單不符合退款條件。')
    return
  }
  try {
    // 呼叫 store 中的退款 action
    const response = await orderStore.refundOrder(props.order.orderNumber)
    if (response.success) {
      ElMessage.success(response.message || '訂單已成功退款！')
      emit('update:order') // 同樣通知父元件更新列表
      isDrawerVisible.value = false // 關閉抽屜
    }
  } catch (error) {
    console.error('退款失敗:', error)
  }
}
</script>

<style scoped>
/* style 區塊的註解 */
.drawer-content .el-descriptions {
  margin-bottom: 20px;
}
.descriptions-margin-top {
  margin-top: 20px;
}
.details-section h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

/* 使用 :deep() 穿透 scoped 限制，修改子元件 el-descriptions-item 的 label 樣式 */
:deep(.total-amount-label) {
  font-weight: bold;
}
.total-amount-value {
  font-size: 1.1em;
  font-weight: bold;
  color: #f56c6c; /* 使用醒目的紅色 */
}
.drawer-footer {
  display: flex;
  justify-content: flex-end; /* 讓按鈕靠右對齊 */
}
.tooltip-wrapper {
  display: inline-block; /* 確保包裹的 div 大小與按鈕一致 */
}

/* --- 以下是為了統一介面風格，覆寫 Element Plus 元件的預設樣式 --- */

/* 覆寫 el-table 的邊框顏色 CSS 變數 */
:deep(.el-table) {
  --el-table-border-color: #dcdfe6;
}

/* 修改 el-descriptions 的儲存格邊框顏色 */
:deep(.el-descriptions__cell) {
  border-color: #dcdfe6 !important;
}

/* 修改 el-descriptions 的外層 table 邊框顏色 */
:deep(.el-descriptions__table) {
  border-color: #dcdfe6 !important;
}

/* 修改 el-descriptions 的 label 和 content 儲存格邊框顏色 */
:deep(.el-descriptions__label),
:deep(.el-descriptions__content) {
  border-color: #dcdfe6 !important;
}

/* 修改 el-table 的表頭(th)樣式 */
:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important; /* 淺灰色背景 */
  color: #606266 !important; /* 深灰色文字 */
  font-weight: 600; /* 字體加粗 */
}
/* 修改 el-table 的內容儲存格(td)樣式 */
:deep(.el-table td.el-table__cell) {
  color: #303133 !important; /* 主要文字顏色 */
}
</style>
