<template>
  <div class="event-list">
    <div class="header">
      <div class="left">
        <el-tag class="count" v-if="!loading">目前共 {{ total }} 筆</el-tag>
      </div>
      <div class="right">
        <el-button :loading="loading" @click="reload" plain>重新整理</el-button>
        <el-button type="success" @click="goCreate">新增活動</el-button>
      </div>
    </div>

    <el-card class="filters" shadow="never">
      <div class="filters-row">
        <el-input
          v-model="query.keyword"
          placeholder="搜尋標題 / 內容…"
          clearable
          @keyup.enter="reload"
          style="max-width: 320px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="query.categoryId"
          placeholder="活動分類"
          clearable
          style="width: 180px"
          @change="reload"
        >
          <el-option
            v-for="c in eventCategory"
            :key="c.eventCategoryId"
            :label="c.eventCategoryName"
            :value="c.eventCategoryId"
          />
        </el-select>

        <el-select
          v-model="query.status"
          placeholder="活動狀態"
          clearable
          style="width: 160px"
          @change="reload"
        >
          <el-option label="即將開始" value="upcoming" />
          <el-option label="進行中" value="ongoing" />
          <el-option label="已結束" value="ended" />
          <el-option label="待定" value="pending" />
        </el-select>

        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          unlink-panels
          range-separator="至"
          start-placeholder="建立起日"
          end-placeholder="建立迄日"
          value-format="YYYY-MM-DD"
          style="width: 340px"
          @change="reload"
        />

        <div class="filters-actions">
          <el-button @click="resetFilters">重設</el-button>
          <el-button type="primary" :loading="loading" @click="reload">搜尋</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="rows"
        header-cell-class-name="table-header"
        size="small"
        @sort-change="onSortChange"
        :default-sort="{ prop: 'startDate', order: 'descending' }"
      >
        <el-table-column label="標題" sortable="custom" prop="title">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name">{{ row.title || '—' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="eventCategoryName" label="分類" sortable="custom">
          <template #default="{ row }">
            {{ row.eventCategoryName || '—' }}
          </template>
        </el-table-column>

        <el-table-column label="狀態">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" round>
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="開始日期" sortable="custom" prop="startDate">
          <template #default="{ row }">{{ formatDate(row.startDate) }}</template>
        </el-table-column>

        <el-table-column label="結束日期" sortable="custom" prop="endDate">
          <template #default="{ row }">{{ formatDate(row.endDate) }}</template>
        </el-table-column>

        <el-table-column label="圖片" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.hasImage && row.imageUrl"
              style="width: 150px; height: 100px; object-fit: cover; border-radius: 4px"
              :src="row.imageUrl"
              fit="cover"
              preview-teleported
              :preview-src-list="[row.imageUrl]"
            />
            <div v-else-if="row.hasImage && !row.imageUrl" style="color: #909399; font-size: 12px">
              載入中...
            </div>
            <span v-else>
              <img
                src="/src/assets/images/noimg.png"
                alt="無圖片"
                style="width: 72px; height: 90px"
              />
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goDetail(row.eventId)">詳情</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && rows.length === 0" description="查無資料" class="empty" />

      <div class="pager" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, sizes"
          :total="total"
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[5, 10, 15, 20, 25, 50, 100]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// --- 引入依賴 ---
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue' // 引入 Vue 核心功能
import { useRoute, useRouter } from 'vue-router' // 引入路由功能
import { storeToRefs } from 'pinia' // 引入 Pinia 功能，讓 store 的 state 具有響應性
import { ElMessage, ElMessageBox } from 'element-plus' // 引入 Element Plus 的提示框
import { Search } from '@element-plus/icons-vue' // 引入 Element Plus 的圖示
import { useEventStore } from '@/features/Event/store/useEventStore' // 引入活動資料的 store
import { useEventCategoryStore } from '@/features/Event/store/useEventCategoryStore' // 引入活動分類的 store
import eventService from '@/features/Event/services/eventService' // 引入 API 服務

// --- 初始化 ---
const route = useRoute() // 獲取當前路由資訊
const router = useRouter() // 獲取路由實例，用於頁面跳轉

// --- 狀態管理 ---
const eventStore = useEventStore() // 建立 event store 實例
const { events } = storeToRefs(eventStore) // 從 store 中解構出 events 狀態，並保持其響應性

const categoryStore = useEventCategoryStore() // 建立 category store 實例
const { eventCategory } = storeToRefs(categoryStore) // 解構出 eventCategory 狀態

/**
 * @description 儲存使用者在篩選器中的輸入，是整個頁面的「UI狀態」核心
 */
const query = reactive({
  page: Number(route.query.page ?? 1),
  size: Number(route.query.size ?? 10),
  keyword: route.query.keyword ?? '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  status: route.query.status ?? '',
  dateRange: null,
})

/**
 * @description 本地狀態變數
 */
const loading = ref(false) // 控制 v-loading 的顯示，表示是否正在從後端載入資料
const rows = ref([]) // 儲存最終要渲染到表格中的資料陣列 (包含圖片的 Blob URL)
const total = ref(5) // 儲存經過前端篩選後的總筆數，用於分頁器

/**
 * @description 排序相關的狀態
 */
const sortBy = ref('startDate') // 當前排序的欄位
const sortOrder = ref('desc') // 當前排序的順序 ('asc' 或 'desc')

// --- 導航函式 ---
const goDetail = (id) => {
  router.push({ name: 'AdminEventEdit', params: { id } })
}

const goCreate = () => {
  // 用「路由名稱」導頁（建議）
  router.push({ name: 'AdminEventAdd' })
  // 如果你沒有命名路由，可以用 path：
  // router.push('/admin/events/create')
}

// --- 工具函式 (用於模板渲染) ---
/**
 * @description 根據狀態碼回傳對應的中文文字
 * @param {string} s 狀態碼 (e.g., 'upcoming')
 * @returns {string} 中文狀態 (e.g., '即將開始')
 */
const statusText = (s) => {
  if (s === 'upcoming') return '即將開始'
  if (s === 'ongoing') return '進行中'
  if (s === 'ended') return '已結束'
  if (s === 'pending') return '待定'
  return s || '—'
}

/**
 * @description 根據狀態碼回傳 Element Plus 標籤的 type 類型，決定顏色
 * @param {string} s 狀態碼
 * @returns {string} el-tag 的 type (e.g., 'warning')
 */
const statusTag = (s) => {
  if (s === 'upcoming') return 'warning'
  if (s === 'ongoing') return 'success'
  if (s === 'ended') return 'danger'
  if (s === 'pending') return 'info'
  return 'info'
}

/**
 * @description 格式化日期字串
 * @param {string} v 日期字串
 * @returns {string} 'YYYY-MM-DD' 格式的字串
 */
const formatDate = (v) => {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/**
 * @description 核心函式，負責載入和重整頁面資料
 */
const reload = async () => {
  loading.value = true // 開始載入，顯示 loading 效果
  cleanupImageUrls() // 在獲取新資料前，先清理上一頁圖片佔用的瀏覽器記憶體

  try {
    // 將當前的篩選、分頁狀態更新到瀏覽器的 URL 地址上
    router.replace({
      query: {
        page: String(query.page),
        size: String(query.size),
        keyword: query.keyword || undefined,
        categoryId: query.categoryId || undefined,
        status: query.status || undefined,
      },
    })

    // **【流程第一步：請求後端資料】**
    // 組合要傳給後端的參數。根據您目前的後端，它只會使用 title 和 sort 相關的參數。
    await eventStore.fetchEvents({
      title: query.keyword,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value,
    })

    // **【流程第二步：異步載入所有圖片】**
    // 從 store 中獲取後端回傳的原始資料列表
    const listFromStore = events.value.list || []
    // 為列表中的每個項目建立一個載入圖片的非同步任務
    const imageLoadTasks = listFromStore.map(async (event) => {
      let imageUrl = null
      if (event.hasImage) {
        try {
          // 呼叫 service，透過 axios 安全地獲取圖片的 Blob URL
          imageUrl = await eventService.getEventImage(event.eventId)
        } catch (e) {
          console.error(`無法載入活動 ${event.eventId} 的圖片`, e)
        }
      }
      // 返回一個新物件，包含所有原始資料，並額外附加一個 imageUrl 屬性
      return { ...event, imageUrl }
    })
    // 使用 Promise.all 等待所有圖片載入任務並行完成
    const populatedList = await Promise.all(imageLoadTasks)

    // **【流程第三步：前端篩選】**
    // 因為後端目前只篩選了 title，我們需要在前端完成 categoryId 和 status 的篩選
    let arr = populatedList
    if (query.categoryId) {
      arr = arr.filter((x) => x.eventCategoryId == query.categoryId)
    }
    if (query.status) {
      arr = arr.filter((x) => x.status === query.status)
    }
    // 未來如果後端也支援這些篩選，就可以移除這段程式碼

    // **【流程第四步：前端分頁】**
    // 計算經過前端篩選後的總筆數
    total.value = arr.length
    // 根據當前頁碼和每頁數量，從篩選後的陣列中切出當前頁要顯示的資料
    const start = (query.page - 1) * query.size
    rows.value = arr.slice(start, start + query.size)
    // 未來如果後端支援分頁，這段程式碼將會被 `rows.value = populatedList` 和 `total.value = events.value.count` 取代
  } catch (e) {
    ElMessage.error('載入失敗，請稍後再試。')
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false // 結束載入，隱藏 loading 效果
  }
}

/**
 * @description 清理上一頁圖片產生的 Blob URL，防止瀏覽器記憶體洩漏
 */
const cleanupImageUrls = () => {
  rows.value.forEach((row) => {
    if (row.imageUrl && row.imageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(row.imageUrl)
    }
  })
}

// --- Vue 生命週期掛鉤 ---
/**
 * @description 在元件被卸載（例如跳轉到其他頁面）之前，執行最後一次記憶體清理
 */
onBeforeUnmount(() => {
  cleanupImageUrls()
})

/**
 * @description 元件掛載完成後執行的初始化操作
 */
onMounted(async () => {
  // 先請求分類資料，填充篩選器下拉選單
  await categoryStore.fetchCategories()
  // 然後執行第一次資料載入
  await reload()
})

// --- 事件處理函式 ---
/**
 * @description 處理刪除按鈕點擊事件
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`確定要刪除活動「${row.title}」嗎？`, '提示', { type: 'warning' })
    await eventStore.deleteEventById(row.eventId)
    ElMessage.success('刪除成功')
    reload() // 刪除後重載資料
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

/**
 * @description 處理分頁器頁碼改變事件
 */
const onPageChange = (p) => {
  query.page = p
  reload()
}

/**
 * @description 處理分頁器每頁數量改變事件
 */
const onSizeChange = (s) => {
  query.size = s
  query.page = 1 // 改變每頁數量時，回到第一頁
  reload()
}

/**
 * @description 處理表格排序改變事件
 */
const onSortChange = ({ prop, order }) => {
  sortBy.value = prop || 'startDate'
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  reload() // 排序改變，重新載入資料
}

/**
 * @description 處理重設篩選按鈕點擊事件
 */
const resetFilters = () => {
  query.keyword = ''
  query.categoryId = undefined
  query.status = ''
  query.dateRange = null
  query.page = 1
  reload()
}

// 監聽路由中的 refresh 參數，用於從其他頁面跳轉回來時強制刷新
watch(
  () => route.query.refresh,
  () => reload(),
)

// 移除 computed 和 watch storeLoading，因為本地 loading.value 已足夠
</script>

<style scoped>
/* 樣式保持不變 */
.event-list {
  padding: 20px;
  background-color: #f0f2f5; /* 灰底 */
  min-height: 100vh; /* 滿版高度 */
  width: 100%; /* 滿版寬度 */
  margin: 0; /* 移除水平置中 */
  box-sizing: border-box; /* 確保 padding 不會撐爆 */
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.count {
  border: 1px solid rgba(141, 177, 215, 0.901);
  border-radius: 999px;
}
.filters {
  border-radius: 12px;
}
.filters-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filters-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}
.table-card {
  border-radius: 12px;
}
.table-header {
  background: var(--el-fill-color-light) !important;
  font-weight: 600;
}
.name-cell .name {
  font-weight: 700;
}
.name-cell .sub {
  margin-left: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.empty {
  padding: 24px 0;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.detail-image {
  margin-top: 12px;
  text-align: center;
}
.detail-image img {
  max-width: 240px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}
</style>
