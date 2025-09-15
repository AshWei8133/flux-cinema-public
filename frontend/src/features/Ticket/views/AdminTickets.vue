<template>
  <div class="ticket-type-management">
    <div class="header-content">
      <el-button type="primary" :icon="Plus" @click="openDialog('new')">新增</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="ticketTypes" style="width: 100%" v-loading="loading">
        <el-table-column label="狀態" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnabled"
              @change="handleStatusChange(scope.row)"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            />
          </template>
        </el-table-column>

        <el-table-column prop="ticketTypeName" label="票種名稱" min-width="50" />

        <el-table-column label="折扣規則" min-width="50" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.discountType === 'PERCENTAGE'" type="success">
              票價 {{ formatDiscount(scope.row.discountValue) }} 折
            </el-tag>
            <el-tag v-else-if="scope.row.discountType === 'FIXED'" type="warning">
              折抵 {{ Math.abs(scope.row.discountValue) }} 元
            </el-tag>
            <el-tag v-else type="info"> 基準票價 </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="說明" min-width="230" />

        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              :icon="Edit"
              circle
              @click="openDialog('edit', scope.row)"
              title="編輯"
            />
            <el-button
              type="danger"
              :icon="Delete"
              circle
              @click="handleDelete(scope.row.ticketTypeId)"
              title="刪除"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '編輯票種' : '新增票種'"
      width="550px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" status-icon>
        <el-form-item label="票種名稱" prop="ticketTypeName">
          <el-input v-model="formData.ticketTypeName" placeholder="請輸入票種名稱" />
        </el-form-item>

        <el-form-item label="折扣類型" prop="discountType" required>
          <el-select
            v-model="formData.discountType"
            placeholder="請選擇折扣類型"
            clearable
            style="width: 100%"
          >
            <el-option label="無折扣 (基準票價)" value="NONE" />
            <el-option label="百分比折扣" value="PERCENTAGE" />
            <el-option label="固定金額折抵" value="FIXED" />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="formData.discountType === 'PERCENTAGE' || formData.discountType === 'FIXED'"
          :label="discountValueLabel"
          prop="discountValue"
        >
          <el-input-number
            v-if="formData.discountType === 'PERCENTAGE'"
            v-model="formData.discountValue"
            :precision="2"
            :step="0.05"
            :min="0"
            :max="1"
            placeholder="例: 0.8 為 8 折"
            style="width: 100%"
          />
          <el-input-number
            v-if="formData.discountType === 'FIXED'"
            v-model="formData.discountValue"
            :step="10"
            placeholder="例: -30 為便宜 30 元"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="說明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="請輸入相關說明"
          />
        </el-form-item>

        <el-form-item label="狀態" prop="isEnabled">
          <el-switch v-model="formData.isEnabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="submitForm"> 儲存 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// =================================================================
// 1. Imports (引入)
// =================================================================
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useAdminTicketStore } from '../store/useAdminTicketStore'
import { storeToRefs } from 'pinia'

// =================================================================
// 2. Store (狀態管理)
// =================================================================
const ticketStore = useAdminTicketStore()
const { ticketTypes, isTicketTypeloading: loading } = storeToRefs(ticketStore)

// =================================================================
// 3. Component State (元件內部狀態)
// =================================================================
const dialogVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)
const formData = reactive({
  ticketTypeId: null,
  ticketTypeName: '',
  description: '',
  isEnabled: true,
  discountType: '', // 初始值設為空字串，代表「尚未選擇」
  discountValue: 0,
})

// =================================================================
// 4. Form Validation (表單驗證)
// =================================================================
const validateDiscountValue = (rule, value, callback) => {
  if (formData.discountType === 'PERCENTAGE' || formData.discountType === 'FIXED') {
    if (value === null || value === undefined || value === 0) {
      callback(new Error('請輸入有效的折扣數值'))
    } else if (formData.discountType === 'PERCENTAGE' && (value <= 0 || value >= 1)) {
      callback(new Error('折扣比例必須介於 0.01 和 0.99 之間'))
    } else if (formData.discountType === 'FIXED' && value >= 0) {
      callback(new Error('折抵金額應為負數 (例如: -30)'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}
const formRules = reactive({
  ticketTypeName: [
    { required: true, message: '票種名稱為必填項目', trigger: 'blur' },
    { min: 2, max: 20, message: '長度應在 2 到 20 個字元之間', trigger: 'blur' },
  ],
  discountType: [{ required: true, message: '請選擇折扣類型', trigger: 'change' }],
  discountValue: [{ validator: validateDiscountValue, trigger: 'blur' }],
})

// =================================================================
// 5. Computed Properties (計算屬性)
// =================================================================
const discountValueLabel = computed(() => {
  if (formData.discountType === 'PERCENTAGE') return '折扣比例'
  if (formData.discountType === 'FIXED') return '折抵金額'
  return '折扣數值'
})

// =================================================================
// 6. Helper Functions (輔助函式)
// =================================================================
const formatDiscount = (value) => {
  if (typeof value !== 'number' || value <= 0 || value >= 1) {
    return ''
  }
  const discountNumber = value * 10
  const finalDiscount = Number(discountNumber.toFixed(1))
  return finalDiscount
}

// =================================================================
// 7. Lifecycle Hooks (生命週期鉤子)
// =================================================================
onMounted(() => {
  ticketStore.fetchAllTicketTypes()
})

// =================================================================
// 8. Methods (方法)
// =================================================================
const openDialog = (mode, data = null) => {
  isEditMode.value = mode === 'edit'
  if (isEditMode.value && data) {
    Object.assign(formData, data)
    // 如果從後端來的資料 discountType 是 null，我們在表單中將其對應為 'NONE' 字串
    if (formData.discountType === null) {
      formData.discountType = 'NONE'
    }
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

const resetForm = () => {
  Object.assign(formData, {
    ticketTypeId: null,
    ticketTypeName: '',
    description: '',
    isEnabled: true,
    discountType: '', // 重置值也改為空字串
    discountValue: 0,
  })
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 1. 建立一個準備提交到後端的資料副本
      const dataToSubmit = { ...formData }

      // 2. 在提交前，將 'NONE' 字串或空字串等轉換回後端需要的 null
      if (
        dataToSubmit.discountType === 'NONE' ||
        dataToSubmit.discountType === '' ||
        dataToSubmit.discountType === undefined
      ) {
        dataToSubmit.discountType = null
      }

      // 3. 如果轉換後 discountType 為 null，則將 discountValue 設為 0
      if (!dataToSubmit.discountType) {
        dataToSubmit.discountValue = 0
      }
      try {
        if (isEditMode.value) {
          // --- 更新邏輯 ---
          console.log('正在更新票種:', dataToSubmit)
          const response = await ticketStore.updateTicketType(
            dataToSubmit.ticketTypeId,
            dataToSubmit,
          )
          if (response && response.success) {
            ElMessage.success(response.message || '更新成功！')
          }
        } else {
          // --- 新增邏輯 ---
          console.log('正在新增票種:', dataToSubmit)
          const response = await ticketStore.createTicketType(dataToSubmit)
          if (response && response.success) {
            ElMessage.success(response.message || '新增成功！')
          }
        }
        closeDialog()
      } catch (error) {
        console.error('提交表單時發生錯誤:', error)
      }
    } else {
      ElMessage.error('請檢查表單輸入是否正確')
    }
  })
}

/**
 * 處理表格中狀態開關變更的函式
 * @param {object} row - 被變更的那一列的資料
 */
const handleStatusChange = async (row) => {
  // el-switch 的 v-model 已經先更新了 row.isEnabled 的值
  // 所以我們可以直接把整個 row 物件當作更新資料傳送到後端
  console.log(`正在更新 [${row.ticketTypeName}] 的狀態為: ${row.isEnabled}`)

  try {
    // 呼叫 Store 中的 update action，傳入 ID 和整筆 row 的資料
    const response = await ticketStore.updateTicketType(row.ticketTypeId, row)

    // 根據 API 回應顯示訊息
    if (response && response.success) {
      ElMessage.success(response.message || '狀態更新成功！')
    } else {
      // 如果 API 回應 success: false，也視為失敗，還原開關狀態
      throw new Error(response.message || '後端更新失敗')
    }
  } catch (error) {
    // 如果 API 請求失敗，Axios 攔截器會顯示錯誤訊息
    console.error('狀態更新失敗:', error)
    // 關鍵一步：將開關的狀態還原回操作前的樣子，以保持畫面與後端資料同步
    row.isEnabled = !row.isEnabled
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('您確定要刪除此票種嗎？此操作將無法復原。', '警告', {
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        // --- 刪除邏輯 ---
        console.log('正在刪除票種, ID:', id)
        const response = await ticketStore.deleteTicketType(id)
        if (response && response.success) {
          ElMessage.success(response.message || '刪除成功！')
        }
      } catch (error) {
        console.error(`刪除 ID 為 ${id} 的票種失敗:`, error)
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}
</script>

<style scoped>
/* 您的樣式完全保留，無需變動 */
.ticket-type-management {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}
.header-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}
.table-card {
  border-radius: 4px;
}
:deep(.el-card__body) {
  padding: 0;
}
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
}
.dialog-footer {
  text-align: right;
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848;
  color: #e9eaeb;
  font-weight: 600;
}
</style>
