<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="票種介紹"
    width="600px"
    custom-class="dark-dialog"
  >
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-table
      v-else
      :data="ticketTypes"
      v-loading="isLoading"
      class="ticket-info-table"
      :header-cell-style="{ background: '#333', color: '#E5E5E5' }"
      empty-text="暫無票種資訊"
      style="width: 100%"
    >
      <el-table-column prop="ticketTypeName" label="票種名稱" min-width="120" />
      <el-table-column prop="description" label="適用說明" min-width="250" />
      <el-table-column label="優惠方式" min-width="180" align="center">
        <template #default="scope">
          <el-tag :type="getTagType(scope.row.discountType)" effect="dark" size="small">
            {{ formatDiscountInfo(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button type="primary" @click="$emit('update:modelValue', false)">關閉</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { watch } from 'vue'
import { ElDialog, ElButton, ElTable, ElTableColumn, ElTag, ElAlert } from 'element-plus'
import { storeToRefs } from 'pinia'
import { usePublicTicketStore } from '../../store/usePublicTicketStore'

// ----------------------------------------------------------------
// Props & Emits
// ----------------------------------------------------------------
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true,
  },
})
const emit = defineEmits(['update:modelValue'])

// ----------------------------------------------------------------
// Store
// ----------------------------------------------------------------
const ticketStore = usePublicTicketStore()
const { ticketTypes, isLoading, error } = storeToRefs(ticketStore) // [修改] 從 Store 獲取狀態
const { fetchAllTicketTypes } = ticketStore

// ----------------------------------------------------------------
// Watcher
// ----------------------------------------------------------------
// [修改] 監聽 Modal 的開啟狀態，當第一次開啟時，觸發 API 請求
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      fetchAllTicketTypes()
    }
  },
  { immediate: true }, // 立即執行一次，確保在元件建立時就能判斷是否需要載入
)

// ----------------------------------------------------------------
// Methods
// ----------------------------------------------------------------
// 格式化優惠資訊顯示的函式 (維持不變)
function formatDiscountInfo(ticket) {
  const value = Number(ticket.discountValue)
  if (ticket.discountType === 'PERCENTAGE') {
    const discount = parseFloat((value * 10).toFixed(1))
    return `依全票票價 ${discount} 折計算`
  }
  if (ticket.discountType === 'FIXED') {
    return `依全票票價折抵 ${Math.abs(value)} 元`
  }
  return '依影城全票票價'
}

// 根據折扣類型決定 el-tag 顏色的函式 (維持不變)
function getTagType(discountType) {
  if (discountType === 'PERCENTAGE') return 'success'
  if (discountType === 'FIXED') return 'warning'
  return 'info'
}
</script>

<style scoped>
/* --- 深色主題 Dialog 和 Table 的樣式 (維持不變) --- */
:deep(.dark-dialog) {
  background-color: #2c2c2c;
}
:deep(.dark-dialog .el-dialog__title) {
  color: #e5e5e5;
}
:deep(.dark-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #e5e5e5;
}
.ticket-info-table {
  --el-table-border-color: #444;
  --el-table-tr-bg-color: #2c2c2c;
  --el-table-bg-color: #2c2c2c;
  --el-table-header-bg-color: #333;
  --el-table-header-text-color: #e5e5e5;
  --el-table-row-hover-bg-color: #4a4a4a;
  color: #ccc;
}
:deep(.ticket-info-table .el-table__cell) {
  color: #ccc;
}
:deep(.ticket-info-table .el-table__body tr:hover > td) {
  color: #fff;
}
:deep(.el-table__empty-text) {
  color: #888;
}
</style>
