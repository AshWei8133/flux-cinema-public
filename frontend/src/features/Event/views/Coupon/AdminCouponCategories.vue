<template>
  <div class="event-category-management">
    <!-- 與票種管理一致：右上角新增按鈕 -->
    <div class="header-content">
      <el-button type="primary" :icon="Plus" @click="openDialog('new')">新增類型</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="categories" style="width: 100%" v-loading="isLoading">
        <el-table-column prop="couponCategoryName" label="類型名稱" min-width="200" />
        <el-table-column prop="description" label="說明" min-width="300" />
        <el-table-column label="操作" width="128" align="center" fixed="right">
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
              @click="handleDelete(scope.row)"
              title="刪除"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '編輯優惠券類型' : '新增優惠券類型'"
      width="550px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" status-icon>
        <el-form-item label="類型名稱" prop="couponCategoryName">
          <el-input
            v-model="formData.couponCategoryName"
            placeholder="請輸入類型名稱 (例如：會員專享)"
          />
        </el-form-item>
        <el-form-item label="說明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="請輸入此優惠券類型的相關說明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="submitForm">儲存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useCouponCategoryStore } from '@/features/Event/store/useCouponCategoryStore'

// Store
const categoryStore = useCouponCategoryStore()
const { categories, isLoading, error } = storeToRefs(categoryStore)

// Local state
const dialogVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)
const formData = reactive({
  couponCategoryId: null,
  couponCategoryName: '',
  description: '',
})

// Validation
const formRules = reactive({
  couponCategoryName: [
    { required: true, message: '類型名稱為必填項目', trigger: 'blur' },
    { min: 2, max: 20, message: '長度應在 2 到 20 個字元之間', trigger: 'blur' },
  ],
  description: [{ max: 100, message: '說明長度不能超過 100 個字元', trigger: 'blur' }],
})

// Methods
const openDialog = (mode, data = null) => {
  isEditMode.value = mode === 'edit'
  if (isEditMode.value && data) {
    Object.assign(formData, data)
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
    couponCategoryId: null,
    couponCategoryName: '',
    description: '',
  })
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return ElMessage.error('請檢查表單輸入是否正確')
    try {
      if (isEditMode.value) {
        await categoryStore.updateCouponCategory(formData.couponCategoryId, formData)
        ElMessage.success('更新成功！')
      } else {
        await categoryStore.createCouponCategory(formData)
        ElMessage.success('新增成功！')
      }
      closeDialog()
    } catch (err) {
      console.error('儲存失敗:', err)
      ElMessage.error(error.value || '操作失敗，請稍後再試。')
    }
  })
}

const handleDelete = (category) => {
  ElMessageBox.confirm(
    `您確定要刪除「${category.couponCategoryName}」這個類型嗎？此操作將無法復原。`,
    '警告',
    {
      confirmButtonText: '確定刪除',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
    .then(async () => {
      try {
        await categoryStore.deleteCouponCategory(category.couponCategoryId)
        ElMessage.success('刪除成功')
      } catch (err) {
        console.error('刪除失敗:', err)
        ElMessage.error(error.value || '刪除失敗，可能已有優惠券使用此類型。')
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}

// Lifecycle
onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<style scoped>
/* 與票種管理相同的外觀規格 */
.event-category-management {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 右上角操作列 */
.header-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

/* 卡片與表格：去掉預設 padding，對齊票種頁 */
.table-card {
  border-radius: 4px;
  overflow: visible; /* 讓 fixed 右欄陰影不被裁切 */
}
:deep(.el-card__body) {
  padding: 0;
}

/* Loading 遮罩一致 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
}

/* Dialog footer 對齊 */
.dialog-footer {
  text-align: right;
}

/* 表頭深色條（含右側 fixed 補丁） */
:deep(.el-table th.el-table__cell),
:deep(.el-table__fixed-right-patch) {
  background-color: #484848 !important;
  color: #e9eaeb !important;
  font-weight: 600;
}

/* 固定右欄背景與陰影，避免“浮一塊”感 */
:deep(.el-table__fixed-right) {
  background-color: #fff;
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.06);
  z-index: 3;
}
:deep(.el-table__fixed-right-patch) {
  pointer-events: none; /* 不要擋到按鈕點擊 */
}

/* 圓形按鈕間距，避免擠在一起 */
:deep(.el-table .el-button.is-circle) {
  margin: 0 4px;
}
</style>
