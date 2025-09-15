<template>
  <el-table
    class="elTable"
    :data="data"
    border
    style="width: 100%"
    :row-class-name="tableRowClassName"
    :header-cell-style="headerCellStyle"
    :cell-style="cellStyle"
  >
    <el-table-column
      v-for="column in columns"
      :key="column.key"
      :prop="column.key"
      :label="column.title"
      :width="column.width || undefined"
      :min-width="column.minWidth || undefined"
      :resizable="false"
    >
      <template #default="{ row }">
        <div class="column-content">
          <el-input
            v-if="isEditing(row, column.key)"
            :key="getRowId(row) + '-' + column.key"
            v-model="editValue"
            size="small"
            @keyup.enter="saveEdit(row, column.key)"
          ></el-input>

          <span
            v-if="!isEditing(row, column.key)"
            :class="{
              'new-data': row.isNew,
              'updated-data': row.isUpdated && row.changedFields && row.changedFields[column.key],
              'deleted-text': row.isDeleted,
            }"
          >
            {{ row[column.key] || '&nbsp;' }}
          </span>

          <div v-if="isEditing(row, column.key)" class="edit-actions">
            <AdminButton size="small" circle type="success" @click="saveEdit(row, column.key)">
              <Icon icon="mdi:check" />
            </AdminButton>
            <AdminButton size="small" circle type="danger" @click="cancelEdit">
              <Icon icon="mdi:close" />
            </AdminButton>
          </div>

          <AdminButton
            v-else-if="column.isEditable && !row.isDeleted"
            class="edit-icon-btn"
            type="primary"
            size="small"
            circle
            plain
            @click="handleEditClick(row, column.key)"
          >
            <Icon icon="mdi:pencil-outline" />
          </AdminButton>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="80" fixed="right" :resizable="false">
      <template #default="{ row }">
        <AdminButton
          v-if="row.isDeleted"
          type="info"
          size="small"
          circle
          @click="$emit('cancel-delete', row)"
        >
          <Icon icon="mdi:undo" />
        </AdminButton>
        <AdminButton
          v-else
          type="danger"
          size="small"
          circle
          :disabled="isEditing(row, null)"
          @click="$emit('delete', row)"
        >
          <Icon icon="mdi:trash-can-outline" />
        </AdminButton>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref } from 'vue'
import { ElTable, ElTableColumn, ElInput } from 'element-plus'
import AdminButton from './AdminButton.vue'
import { Icon } from '@iconify/vue'

// 數據
const props = defineProps(['data', 'columns', 'editingCell'])
const emit = defineEmits(['edit-field', 'delete', 'save-edit', 'cancel-edit', 'cancel-delete'])

// 暫存編輯的資料
const editValue = ref('')

// === 新增輔助函式，用來獲取正確的ID，可能是 id 或 theaterTypeId ===
const getRowId = (row) => row.id || row.theaterTypeId

// === 判斷當前儲存格是否為編輯狀態 ===
const isEditing = (row, field) => {
  const isEditing = props.editingCell.rowId === getRowId(row) && props.editingCell.field === field
  return isEditing
}

// === 處理點擊編輯的函式，只在點擊時賦值一次 ===
const handleEditClick = (row, field) => {
  editValue.value = row[field] // 只在這裡賦值一次
  emit('edit-field', { row, field })
}

// === 儲存編輯後內容 ===
const saveEdit = (row, field) => {
  emit('save-edit', { row, field, newValue: editValue.value })
  // 避免下次編輯帶入舊值
  editValue.value = ''
}

// === 取消編輯內容 ===
const cancelEdit = () => {
  emit('cancel-edit')
  // 避免下次編輯帶入舊值
  editValue.value = ''
}

// 表頭樣式
const headerCellStyle = {
  background: 'black',
  color: 'white',
  fontWeight: 800,
  fontSize: '14px',
  letterSpacing: '1px',
  borderColor: 'rgba(47, 46, 46, 0.553)',
}

// 儲存格樣式(這裡處理邊框顏色)
const cellStyle = {
  borderColor: 'rgba(47, 46, 46, 0.553)',
}

// 針對要刪除的資料加上刪除線樣式
const tableRowClassName = ({ row }) => {
  return row.isDeleted ? 'deleted-row' : ''
}
</script>

<style scoped>
/* 修正：將 hover 效果從整列 (tr) 改為單元格 (td)
這裡我們選擇父層 .el-table__cell 進行 hover 偵測 */
.column-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: 550;
}
.edit-actions {
  display: flex;
  gap: 5px;
}

:deep(.el-table__cell) {
  white-space: normal;
  position: relative; /* 為了讓 hover 效果能正確作用在單元格內 */
}

:deep(.el-table__cell .cell) {
  word-break: break-all;
}

.elTable {
  border: 1px solid rgba(47, 46, 46, 0.553);
}

.new-data {
  color: #05ed88c4;
}

.updated-data {
  color: #ffcc00ca;
  font-weight: 500;
}

.deleted-text {
  color: #ff4d4d;
  text-decoration: line-through;
}

:deep(.deleted-row) {
  background-color: rgba(174, 174, 174, 0.922);
}

:deep(.el-table__cell:hover .column-content) {
  transform: scale(1.02);
}

/* 編輯按鈕的樣式 */
.edit-icon-btn {
  visibility: hidden;
  border: none;
  background-color: transparent;
  color: #0059ff;
  padding: 0;
  height: auto;
  transition: color 0.3s ease;
  position: absolute;
  right: 5px; /* 調整位置，使其與文字不重疊 */
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

/* 編輯按鈕顯示的 hover 偵測，單位為單元格 (td) */
:deep(.el-table__cell:hover .edit-icon-btn) {
  visibility: visible;
}

:deep(.edit-icon-btn:hover) {
  color: #00ff9d;
}

/* 以下是其餘未更動的樣式，但為了完整性一併列出 */
/* .append-row-container {
  padding: 10px;
}
.add-button-col {
  display: flex;
  justify-content: flex-end;
} */
</style>
