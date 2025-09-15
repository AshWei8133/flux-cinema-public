<template>
  <div class="admin-members-container">
    <!-- 篩選區塊 -->
    <el-card class="filter-card" shadow="always">
      <el-form :model="filterParams" :inline="true" label-position="left">
        <el-form-item label="會員帳號">
          <el-input
            v-model="filterParams.username"
            placeholder="請輸入帳號關鍵字"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="Email">
          <el-input
            v-model="filterParams.email"
            placeholder="請輸入Email關鍵字"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="性別">
          <el-select
            v-model="filterParams.gender"
            placeholder="全部分性別"
            clearable
            style="width: 150px"
          >
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查詢</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作列 -->
    <!-- <div class="action-bar">
      <div>
        <el-button type="success" :icon="Plus" @click="navigateToAddMember">新增會員</el-button>
      </div>
    </div> -->

    <!-- 表格容器 -->
    <el-card class="table-container-card" shadow="always">
      <el-table
        :data="paginatedData"
        v-loading="isMembersLoading"
        @sort-change="handleSortChange"
        border
      >
        <!-- <el-table-column
          prop="memberId"
          label="ID"
          sortable="custom"
          width="80"
          align="center"
          :resizable="false"
        /> -->
        <el-table-column
          prop="username"
          label="帳號"
          sortable="custom"
          align="center"
          :resizable="false"
        />
        <el-table-column
          prop="email"
          label="Email"
          sortable="custom"
          align="center"
          :resizable="false"
        />
        <el-table-column
          prop="gender"
          label="性別"
          :formatter="genderFormatter"
          align="center"
          :resizable="false"
        />
        <el-table-column
          prop="phone"
          label="電話"
          :formatter="phoneFormatter"
          align="center"
          :resizable="false"
        />
        <el-table-column label="操作" width="200" align="center" :resizable="false">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewMember(scope.row.memberId)"
              >查看</el-button
            >
            <el-button size="small" @click="editMember(scope.row.memberId)">編輯</el-button>
            <el-button size="small" type="danger" @click="removeMember(scope.row.memberId)"
              >刪除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分頁 -->
      <div class="pagination-wrapper">
        <span class="pagination-total">共 {{ filteredAndSortedData.length }} 筆資料</span>
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredAndSortedData.length"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberStore } from '../store/useMemberStore'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'

const store = useMemberStore()
const { members, isMembersLoading, membersError } = storeToRefs(store)
const router = useRouter()

// --- 響應式狀態定義 ---
const filterParams = reactive({ username: '', email: '', gender: '' })
const activeFilterParams = reactive({ username: '', email: '', gender: '' })
const sortParams = reactive({ prop: 'memberId', order: 'ascending' })
const pagination = reactive({ currentPage: 1, pageSize: 10 })

// --- Computed Properties for Data Handling ---
const filteredAndSortedData = computed(() => {
  let data = [...members.value]

  // Filtering
  const { username, email, gender } = activeFilterParams
  if (username || email || gender) {
    data = data.filter((member) => {
      const usernameMatch = username
        ? member.username.toLowerCase().includes(username.toLowerCase())
        : true
      const emailMatch = email ? member.email.toLowerCase().includes(email.toLowerCase()) : true
      const genderMatch = gender ? member.gender === gender : true
      return usernameMatch && emailMatch && genderMatch
    })
  }

  // Sorting
  if (sortParams.prop && sortParams.order) {
    data.sort((a, b) => {
      const propA = a[sortParams.prop]
      const propB = b[sortParams.prop]
      let comparison = 0
      if (propA > propB) comparison = 1
      else if (propA < propB) comparison = -1
      return sortParams.order === 'ascending' ? comparison : -comparison
    })
  }

  return data
})

const paginatedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredAndSortedData.value.slice(start, end)
})

// --- Lifecycle Hooks ---
onMounted(() => {
  store.fetchAllMembers()
})

// --- 事件處理方法 ---
const handleSearch = () => {
  pagination.currentPage = 1
  Object.assign(activeFilterParams, JSON.parse(JSON.stringify(filterParams)))
  ElMessage.success(`已根據條件查詢到 ${filteredAndSortedData.value.length} 筆資料`)
}

const resetFilters = () => {
  Object.assign(filterParams, { username: '', email: '', gender: '' })
  Object.assign(activeFilterParams, { username: '', email: '', gender: '' })
  pagination.currentPage = 1
  ElMessage.info('篩選條件已重置')
}

const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop
  sortParams.order = order
  pagination.currentPage = 1
}

// --- 導航與操作方法 ---
const viewMember = (id) => router.push(`/admin/members/information/${id}`)
const editMember = (id) => router.push(`/admin/members/edit/${id}`)
const navigateToAddMember = () => router.push('/admin/members/add')

const removeMember = (id) => {
  ElMessageBox.confirm('您確定要永久刪除這位會員嗎？此操作無法復原。', '刪除確認', {
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await store.deleteMember(id)
        ElMessage.success('會員已成功刪除')
        store.fetchAllMembers() // 重新載入資料
      } catch (e) {
        ElMessage.error(e.message || '刪除失敗')
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除操作')
    })
}

// --- 表格格式化工具 ---
const genderFormatter = (row, column, cellValue) => {
  if (cellValue === 'M') return '男'
  if (cellValue === 'F') return '女'
  return '未知'
}

const phoneFormatter = (row, column, cellValue) => {
  if (!cellValue || typeof cellValue !== 'string' || cellValue.length !== 10) {
    return cellValue
  }
  return `${cellValue.slice(0, 4)}-${cellValue.slice(4, 7)}-${cellValue.slice(7)}`
}
</script>

<style scoped>
.admin-members-container {
  padding: 20px;
  background-color: #eaedf1;
}

.filter-card {
  margin-bottom: 20px;
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.pagination-total {
  margin-right: 16px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.el-form--inline .el-form-item {
  margin-right: 20px;
  margin-bottom: 18px;
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
