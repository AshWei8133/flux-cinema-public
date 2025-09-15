<template>
  <el-card shadow="never" class="table-card">
    <el-table :data="orders" v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNumber" label="訂單編號" width="150" />
      <el-table-column label="會員帳號" prop="member.username" min-width="120" />
      <el-table-column label="電子郵件" prop="member.email" min-width="180" />
      <el-table-column label="手機號碼" prop="member.phone" min-width="120" />

      <el-table-column label="訂購時間" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdTime) }}
        </template>
      </el-table-column>

      <el-table-column label="總金額" width="100" align="right">
        <template #default="scope"> $ {{ scope.row.totalAmount.toLocaleString() }} </template>
      </el-table-column>

      <el-table-column label="支付方式" min-width="120" align="center">
        <template #default="scope">
          {{ formatPaymentType(scope.row.paymentType) || '-' }}
        </template>
      </el-table-column>

      <el-table-column label="狀態" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getOrderStatusInfo(scope.row.status).tagType">
            {{ getOrderStatusInfo(scope.row.status).text }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button type="primary" link @click="emit('view-details', scope.row)">
            查看詳情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <span class="pagination-total">共 {{ total }} 筆</span>
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @current-change="(page) => emit('page-change', page)"
        class="pagination-container"
      />
    </div>
  </el-card>
</template>

<script setup>
// 引入外部的工具函式，保持元件內部邏輯的簡潔
import { formatDateTime } from '@/utils/dateUtils.js'
import { getOrderStatusInfo } from '@/utils/statusMapping.js'

// 定義此元件可以接收的所有 props
defineProps({
  orders: { type: Array, required: true }, // 訂單資料陣列，必須傳入
  loading: { type: Boolean, default: false }, // 是否顯示載入中狀態，預設為 false
  total: { type: Number, default: 0 }, // 總資料筆數，用於分頁，預設為 0
  currentPage: { type: Number, default: 1 }, // 當前頁碼，預設為 1
  pageSize: { type: Number, default: 10 }, // 每頁筆數，預設為 10
})

// 定義此元件可以發射的所有事件，讓父元件可以監聽
const emit = defineEmits(['view-details', 'page-change'])

/**
 * 將後端的支付方式代碼轉換為顯示用的中文名稱。
 * 這是一個本地的輔助函式，只在這個元件內部使用。
 * @param {string | null} paymentType - 從後端來的支付方式代碼 (例如 'ECPay_Credit', 'Cash')
 * @returns {string} - 對應的中文名稱 (例如 '綠界_信用卡', '現金支付')
 */
const formatPaymentType = (paymentType) => {
  switch (paymentType) {
    case 'ECPay_Credit':
      return '信用卡'
    case 'Cash':
      return '現金支付'
    // 如果未來有更多支付方式，可以繼續在這裡擴充...
    default:
      return '' // 如果是 null 或其他未知的類型，返回空字串
  }
}
</script>

<style scoped>
/* `scoped` 屬性確保這裡的 CSS 樣式只會作用於當前這個元件，不會污染到其他元件。 */

/* 分頁器的外層包裹容器，我們將它設定為 flex 佈局 */
.pagination-wrapper {
  margin-top: 20px; /* 與上方表格的間距 */
  display: flex; /* 啟用 Flexbox 佈局 */
  justify-content: flex-end; /* 讓內部的所有項目（總筆數文字、分頁器）都靠右對齊 */
  align-items: center; /* 讓內部的所有項目在垂直方向上居中對齊 */
}

/* 移除分頁元件本身可能帶有的 margin，因為外層 wrapper 已經處理了間距 */
.pagination-container {
  margin-top: 0;
}

/* 使用 :deep() 穿透 scoped 限制，修改子元件 el-table 的表頭(th)樣式 */
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}

/* 自訂「共 X 筆」這段文字的樣式 */
.pagination-total {
  margin-right: 16px; /* 與右側的分頁器保持 16px 的間距 */
  font-weight: 500; /* 字體粗細適中 */
  color: var(
    --el-text-color-regular
  ); /* 使用 Element Plus 提供的 CSS 變數來設定文字顏色，以保持風格統一 */
  font-size: 14px; /* 字體大小 */
}
</style>
