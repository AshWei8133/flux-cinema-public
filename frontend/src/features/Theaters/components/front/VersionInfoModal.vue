<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="影廳版本介紹"
    width="700px"
    custom-class="dark-dialog"
  >
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-table
      v-else
      :data="theaterTypes"
      v-loading="isLoading"
      class="ticket-info-table"
      :header-cell-style="{ background: '#333', color: '#E5E5E5' }"
      empty-text="暫無版本資訊"
      style="width: 100%"
    >
      <el-table-column prop="theaterTypeName" label="版本名稱" min-width="150" />
      <el-table-column prop="description" label="版本特色說明" min-width="450" />
    </el-table>
    <template #footer>
      <el-button type="primary" @click="$emit('update:modelValue', false)">關閉</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { watch } from 'vue'
import { ElDialog, ElButton, ElTable, ElTableColumn, ElAlert } from 'element-plus'
import { storeToRefs } from 'pinia'
import { usePublicTheaterStore } from '../../store/usePublicTheaterStore'

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
const theaterStore = usePublicTheaterStore()
const { theaterTypes, isLoading, error } = storeToRefs(theaterStore) // [修改] 從 Store 獲取狀態
const { fetchAllTheaterTypes } = theaterStore

// ----------------------------------------------------------------
// Watcher
// ----------------------------------------------------------------
// [修改] 監聽 Modal 的開啟狀態，當第一次開啟時，觸發 API 請求
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      fetchAllTheaterTypes()
    }
  },
  { immediate: true },
)
</script>

<style scoped>
/* --- 深色主題 Dialog 和 Table 的樣式 (與 TicketInfoModal 相同) --- */
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
