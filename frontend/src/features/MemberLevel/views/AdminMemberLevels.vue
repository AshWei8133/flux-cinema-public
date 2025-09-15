<template>
  <div class="admin-member-levels-container">
    <!-- 操作列 -->
    <div class="action-bar">
      <div>
        <el-button type="success" :icon="Plus" @click="handleAddNew">新增等級</el-button>
        <el-button type="info" :icon="Refresh" @click="store.fetchLevels()" :loading="isLoading"
          >重新整理</el-button
        >
      </div>
    </div>

    <!-- 表格容器 -->
    <el-card class="table-container-card" shadow="always">
      <el-table :data="levels" v-loading="isLoading" style="width: 100%" border>
        <el-table-column label="圖示" width="100" align="center" :resizable="false">
          <template #default="scope">
            <el-image
              v-if="scope.row.levelIcon"
              :src="`data:image/png;base64,${scope.row.levelIcon}`"
              fit="contain"
              style="width: 50px; height: 50px; border-radius: 4px"
            />
            <span v-else>無圖示</span>
          </template>
        </el-table-column>

        <el-table-column prop="levelName" label="等級名稱" align="center" :resizable="false" />
        <el-table-column
          prop="thresholdLowerBound"
          label="升級門檻 (累積消費)"
          align="center"
          :resizable="false"
        />
        <el-table-column
          prop="upgradeConditionDescription"
          label="條件描述"
          align="center"
          :resizable="false"
        />

        <el-table-column label="操作" width="150" align="center" fixed="right" :resizable="false">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/編輯彈窗 -->
    <MemberLevelModal
      :visible="dialogVisible"
      :is-edit-mode="isEditMode"
      :level-data="editingLevel"
      @close="dialogVisible = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useMemberLevelStore } from '../store/memberLevelStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import MemberLevelModal from '../components/MemberLevelModal.vue'

const store = useMemberLevelStore()
const { levels, isLoading } = storeToRefs(store)

const dialogVisible = ref(false)
const isEditMode = ref(false)
const editingLevel = ref(null)

onMounted(() => {
  store.fetchLevels()
})

const handleAddNew = () => {
  isEditMode.value = false
  editingLevel.value = null
  dialogVisible.value = true
}

const handleEdit = (level) => {
  isEditMode.value = true
  editingLevel.value = { ...level }
  dialogVisible.value = true
}

const handleDelete = (level) => {
  ElMessageBox.confirm(`確定要刪除等級 "${level.levelName}" 嗎？`, '警告', {
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await store.deleteLevel(level.memberLevelId)
        ElMessage.success('等級已成功刪除')
      } catch (error) {
        ElMessage.error(error.message || '刪除失敗')
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除操作')
    })
}

const handleSubmit = async (formData, file) => {
  try {
    if (isEditMode.value) {
      await store.updateLevel(formData.memberLevelId, formData, file)
      ElMessage.success('等級更新成功')
    } else {
      await store.createLevel(formData, file)
      ElMessage.success('等級新增成功')
    }
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '提交失敗')
  }
}
</script>

<style scoped>
.admin-member-levels-container {
  padding: 20px;
  background-color: #eaedf1;
  height: 100%;
}

.table-container-card {
  border-radius: 8px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 5px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important;
  color: #e9eaeb !important;
  font-weight: 600;
}

:deep(.el-table) {
  --el-table-border-color: #dcdfe6;
}
</style>
