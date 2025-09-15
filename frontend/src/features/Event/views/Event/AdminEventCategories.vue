<template>
  <div class="event-category-management">
    <!-- 和上方一致：右上角操作列 -->
    <div class="header-content">
      <el-button type="primary" :icon="Plus" @click="openDialog('new')">新增</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="eventCategory" style="width: 100%" v-loading="isLoading">
        <el-table-column prop="eventCategoryName" label="類型名稱" min-width="200" />
        <el-table-column prop="description" label="說明" min-width="300" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
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
      :title="isEditMode ? '編輯活動類型' : '新增活動類型'"
      width="550px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" status-icon>
        <el-form-item label="類型名稱" prop="eventCategoryName">
          <el-input
            v-model="formData.eventCategoryName"
            placeholder="請輸入類型名稱（例如：會員專享）"
          />
        </el-form-item>
        <el-form-item label="說明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="請輸入此活動類型的相關說明"
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
/* =================================================================
   1) Imports
================================================================= */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useEventCategoryStore } from '@/features/Event/store/useEventCategoryStore'

/* =================================================================
   2) Store
================================================================= */
const categoryStore = useEventCategoryStore()
const { eventCategory, isLoading, error } = storeToRefs(categoryStore)

/* =================================================================
   3) Local State
================================================================= */
const dialogVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)
const formData = reactive({
  eventCategoryId: null,
  eventCategoryName: '',
  description: '',
})

/* =================================================================
   4) Validation
================================================================= */
const formRules = reactive({
  eventCategoryName: [
    { required: true, message: '類型名稱為必填項目', trigger: 'blur' },
    { min: 2, max: 20, message: '長度應在 2 到 20 個字元之間', trigger: 'blur' },
  ],
  description: [{ max: 100, message: '說明長度不能超過 100 個字元', trigger: 'blur' }],
})

/* =================================================================
   5) Methods
================================================================= */
const fetchCategories = async () => {
  try {
    await categoryStore.fetchCategories()
  } catch (_) {}
}

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
    eventCategoryId: null,
    eventCategoryName: '',
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
        await categoryStore.updateEventCategory(formData.eventCategoryId, formData)
        ElMessage.success('更新成功！')
      } else {
        await categoryStore.createEventCategory(formData)
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
    `您確定要刪除「${category.eventCategoryName}」這個類型嗎？此操作將無法復原。`,
    '警告',
    {
      confirmButtonText: '確定刪除',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
    .then(async () => {
      try {
        await categoryStore.deleteEventCategory(category.eventCategoryId)
        ElMessage.success('刪除成功！')
      } catch (err) {
        console.error('刪除失敗:', err)
        ElMessage.error(error.value || '刪除失敗，可能已有活動使用此類型。')
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}

/* =================================================================
   6) Lifecycle
================================================================= */
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
/* 與上方票種管理風格一致 */
.event-category-management {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 右上角工具列 */
.header-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

/* 卡片樣式對齊 */
.table-card {
  border-radius: 4px;
}
:deep(.el-card__body) {
  padding: 0;
}

/* Loading 遮罩一致的淡白背景 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
}

/* Dialog footer 對齊 */
.dialog-footer {
  text-align: right;
}

/* 表頭深色條、字色加粗 */
:deep(.el-table th.el-table__cell) {
  background-color: #484848;
  color: #e9eaeb;
  font-weight: 600;
}
/* ---- 表頭深色樣式（含右側補丁） ---- */
:deep(.el-table th.el-table__cell),
:deep(.el-table__fixed-right-patch) {
  background-color: #484848 !important;
  color: #e9eaeb !important;
  font-weight: 600;
}

/* ---- 右側 fixed 欄位背景與陰影（避免看起來浮一塊） ---- */
:deep(.el-table__fixed-right) {
  background-color: #fff; /* 與表格 body 一致 */
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.06); /* 右側微陰影區隔 */
}

/* ---- 讓固定欄與主表頭高度對齊（某些字型/縮放會差 1px） ---- */
:deep(.el-table__header-wrapper),
:deep(.el-table .el-table__fixed-right .el-table__header-wrapper) {
  line-height: normal;
}

/* ---- 避免卡片/容器 overflow 把 fixed 欄裁掉 ---- */
.table-card {
  overflow: visible; /* 保留 fixed 欄陰影與點擊 */
}
</style>
