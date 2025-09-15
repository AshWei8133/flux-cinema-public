<template>
  <div class="admin-announcements-container">
    <div class="table-controls">
      <!-- 搜尋表單區塊 -->
      <el-form :inline="true" :model="searchParams" class="search-form">
        <el-form-item label="公告標題">
          <el-input
            v-model="searchParams.title"
            placeholder="請輸入標題關鍵字"
            @keyup.enter="handleSearch"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜尋</el-button>
          <el-button @click="clearSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 新增公告按鈕 -->
      <el-button type="success" @click="goToAdd" :icon="Plus">新增公告</el-button>
    </div>

    <!-- 公告列表表格 -->
    <el-table
      :data="paginatedAnnouncements"
      style="width: 100%"
      @sort-change="handleSortChange"
      v-loading="storeLoading"
      :default-sort="{ prop: 'publishDate', order: 'descending' }"
    >
      <el-table-column label="圖片" width="120" align="center">
        <template #default="scope">
          <el-image
            v-if="scope.row.base64ImageString"
            style="width: 80px; height: 100px; object-fit: cover; border-radius: 4px"
            :src="'data:image/jpeg;base64,' + scope.row.base64ImageString"
            fit="cover"
            preview-teleported
            :preview-src-list="[`data:image/jpeg;base64,${scope.row.base64ImageString}`]"
          ></el-image>
          <!-- 無圖片時顯示預設圖片 noimg.png -->
          <span v-else>
            <img
              src="/src/assets/images/noimg.png"
              alt="無圖片"
              style="width: 80px; height: 100px; object-fit: contain"
            />
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="標題" align="center" min-width="180"></el-table-column>
      <el-table-column
        prop="publishDate"
        label="發布日期"
        width="150"
        sortable="custom"
        :sort-orders="['descending', 'ascending']"
        align="center"
      ></el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="scope">
          <!-- 點擊詳情按鈕直接跳轉到編輯頁面 -->
          <el-button size="small" @click="handleEdit(scope.row)">詳情</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁器 -->
    <div class="pagination-container">
      <el-pagination
        v-if="totalAnnouncements > 0"
        :total="totalAnnouncements"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue' // 引入 Element Plus 圖標

// 引入 Pinia Store
import { useAnnouncementStore } from '@/features/Event/store/useAnnouncementStore'

const router = useRouter()
const announcementStore = useAnnouncementStore()
// 從 announcementStore 獲取狀態：公告列表、載入狀態、排序欄位、排序順序
const { announcements, loading: storeLoading, sortBy, sortOrder } = storeToRefs(announcementStore)

// 篩選與分頁狀態
const searchParams = reactive({
  title: '', // 用於綁定搜尋框的輸入值
})

const currentPage = ref(1) // 當前頁碼
const pageSize = ref(5)

// 計算屬性：從 Store 中獲取公告列表和總數
// 由於 Store 現在會直接返回篩選和分頁後的數據，這裡直接使用
const paginatedAnnouncements = computed(() => announcements.value.list)
const totalAnnouncements = computed(() => announcements.value.count)

/**
 * 獲取公告列表，會帶上完整的查詢、排序與分頁參數給後端。
 */
const fetchAnnouncements = async () => {
  try {
    const params = {
      title: searchParams.title,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value,
      page: currentPage.value - 1,
      size: pageSize.value,
    }
    await announcementStore.fetchAnnouncements(params)
  } catch (error) {
    ElMessage.error('獲取公告列表失敗！')
    console.error('Fetch Announcements Error:', error)
  }
}

/**
 * 處理搜尋事件。
 * 當使用者點擊搜尋或按下 Enter 鍵時觸發。
 */
const handleSearch = () => {
  currentPage.value = 1 // 搜尋時重置到第一頁
  fetchAnnouncements() // 觸發數據獲取
}

/**
 * 處理重置事件。
 * 清空所有篩選條件並重置到第一頁。
 */
const clearSearch = () => {
  searchParams.title = '' // 清空標題搜尋
  currentPage.value = 1 // 重置到第一頁
  // 將 store 中的排序狀態重置為預設值
  announcementStore.sortBy = 'publishDate'
  announcementStore.sortOrder = 'desc'
  fetchAnnouncements() // 觸發數據獲取
}

/**
 * 處理表格排序變更的事件。
 * @param {object} sortInfo - Element Plus 提供的排序資訊物件，包含 { prop, order }
 */
const handleSortChange = ({ prop, order }) => {
  // 更新 store 中的排序狀態
  announcementStore.sortBy = prop
  announcementStore.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  fetchAnnouncements() // 根據新的排序狀態重新獲取資料
}

/**
 * 處理分頁頁碼變更。
 * @param {number} page - 新的頁碼
 */
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAnnouncements() // 重新獲取數據
}

/**
 * 處理分頁每頁大小變更。
 * @param {number} size - 新的每頁顯示數量
 */
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 每頁大小改變時回到第一頁
  fetchAnnouncements() // 重新獲取數據
}

/**
 * 導航到新增公告頁面。
 */
const goToAdd = () => {
  router.push({ name: 'AdminAnnouncementAdd' })
}

/**
 * 處理編輯/查看詳情事件。
 * 直接導航到編輯頁面。
 * @param {object} row - 當前選中的公告資料行
 */
const handleEdit = (row) => {
  router.push({ name: 'AdminAnnouncementEdit', params: { id: row.announcementId } })
}

/**
 * 處理刪除公告事件。
 * @param {object} row - 要刪除的公告資料行
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`確定要刪除公告 "${row.title}" 嗎？`, '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await announcementStore.deleteAnnouncement(row.announcementId)
    ElMessage.success('刪除成功！')
    fetchAnnouncements() // 刪除成功後重新獲取資料
  } catch (error) {
    if (error !== 'cancel') {
      // 避免用戶點擊取消時也報錯
      console.error('刪除操作失敗:', error)
      ElMessage.error('刪除失敗！')
    }
  }
}

// 元件掛載時，立即獲取一次資料
onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.admin-announcements-container {
  padding: 20px;
  background-color: #f0f2f5; /* 灰底 */
  min-height: 100vh; /* 滿版高度 */
  width: 100%; /* 滿版寬度 */
  margin: 0; /* 移除水平置中 */
  box-sizing: border-box; /* 確保 padding 不會撐爆 */
}
.table-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px; /* 調整 margin-bottom */
  gap: 10px; /* 項目之間的間距 */
}
.search-form {
  display: flex;
  flex-wrap: wrap; /* 允許換行 */
  gap: 10px; /* 表單項目之間的間距 */
}
.search-form .el-form-item {
  margin-bottom: 0; /* 移除預設的 margin-bottom */
  width: 250px; /* 調整輸入框寬度 */
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
/* 響應式調整 */
@media (max-width: 768px) {
  .table-controls {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-form {
    width: 100%;
  }
  .search-form .el-form-item {
    width: 100%; /* 小螢幕下佔滿寬度 */
  }
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}
</style>
