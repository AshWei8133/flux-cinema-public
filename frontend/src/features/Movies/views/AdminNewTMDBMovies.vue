<template>
  <div class="tmdb-staging-container">
    <el-card class="filter-card" shadow="always">
      <el-form :model="filterParams" :inline="true" label-position="left">
        <el-form-item label="電影名稱">
          <el-input
            v-model="filterParams.name"
            placeholder="請輸入中英文關鍵字"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="上映日期">
          <el-date-picker
            v-model="filterParams.releaseDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="開始日期"
            end-placeholder="結束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="電影分級">
          <el-select
            v-model="filterParams.certification"
            placeholder="全部分級"
            clearable
            style="width: 150px"
          >
            <el-option label="普遍級" value="普遍級" />
            <el-option label="保護級" value="保護級" />
            <el-option label="輔導級" value="輔導級" />
            <el-option label="限制級" value="限制級" />
            <el-option label="待確定" value="待確定" />
          </el-select>
        </el-form-item>
        <el-form-item label="熱門度">
          <el-select
            v-model="filterParams.popularity"
            placeholder="全部熱度"
            clearable
            style="width: 180px"
          >
            <el-option label="指標性熱門 ( > 1000 )" :value="1000" />
            <el-option label="非常熱門 ( > 500 )" :value="500" />
            <el-option label="熱門 ( > 100 )" :value="100" />
            <el-option label="普通 ( 10 - 100 )" :value="10" />
            <el-option label="小眾 ( < 10 )" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查詢</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="action-bar">
      <div>
        <el-button type="success" :icon="Download" @click="openImportDialog"
          >從 TMDB API 匯入新資料</el-button
        >
      </div>
      <div>
        <el-badge
          :value="selectedMovies.length"
          class="badge-item"
          :hidden="selectedMovies.length === 0"
        >
          <el-button
            type="primary"
            :icon="Position"
            @click="handleBatchImportToLocal"
            :disabled="selectedMovies.length === 0"
          >
            匯入至本地電影庫
          </el-button>
        </el-badge>
      </div>
    </div>

    <el-card class="table-container-card" shadow="always">
      <el-table
        :data="paginatedData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        border
      >
        <el-table-column type="selection" width="55" align="center" :resizable="false" />
        <el-table-column label="海報" width="100" align="center" :resizable="false">
          <template #default="scope">
            <el-image
              class="poster-image"
              :src="scope.row.posterImageBase64"
              :preview-src-list="[scope.row.posterImageBase64]"
              fit="cover"
              hide-on-click-modal
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column
          prop="titleLocal"
          label="本地片名"
          sortable="custom"
          min-width="100"
          :resizable="false"
        />
        <el-table-column
          prop="titleEnglish"
          label="英文片名"
          sortable="custom"
          min-width="100"
          :resizable="false"
        />
        <el-table-column
          prop="releaseDate"
          label="上映日期"
          width="140"
          align="center"
          sortable="custom"
          :resizable="false"
        />

        <el-table-column
          prop="popularity"
          label="熱門度"
          width="120"
          align="center"
          sortable="custom"
          :resizable="false"
        />
        <el-table-column
          prop="voteAverage"
          label="平均評分 (5星制)"
          width="180"
          align="center"
          sortable="custom"
          :resizable="false"
        >
          <template #default="scope">
            <el-rate
              :model-value="scope.row.voteAverage / 2"
              disabled
              show-score
              text-color="#ff9900"
              :score-template="`${scope.row.voteAverage} 分`"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <span class="pagination-total">共 {{ filteredAndSortedData.length }} 筆資料</span>

        <el-pagination
          v-model:current-page="pagination.currentPage"
          :page-sizes="10"
          layout="prev, pager, next"
          :total="filteredAndSortedData.length"
          background
        />

        <el-dialog v-model="importDialogVisible" title="從 TMDB API 匯入電影" width="500px" center>
          <p class="dialog-tip">
            將會自動抓取指定日期範圍內，所有在台灣上映的電影資料，並存入 TMDB 暫存資料庫。
          </p>
          <el-form :model="importParams" label-position="top">
            <el-form-item label="請選擇要擷取的上映日期範圍">
              <el-date-picker
                v-model="importParams.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="開始日期"
                end-placeholder="結束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="resetImportParams" :icon="Refresh">重設</el-button>
              <div>
                <el-button @click="importDialogVisible = false">取消</el-button>
                <el-button
                  type="primary"
                  @click="handleApiImport"
                  :loading="importing"
                  :icon="Download"
                >
                  開始擷取
                </el-button>
              </div>
            </div>
          </template>
        </el-dialog>
      </div>
    </el-card>

    <el-dialog v-model="importDialogVisible" title="從 TMDB API 匯入電影" width="500px" center>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Position, Picture } from '@element-plus/icons-vue'
// 1. 引入 Pinia Store 和 storeToRefs
import { storeToRefs } from 'pinia'
import { useTmdbStore } from '../store/useTmdbStore'

// --- Pinia Store 整合 ---
// 2. 實例化 Store
const tmdbStore = useTmdbStore()

// 3. 從 Store 中解構出 State 和 Action，並使用 storeToRefs 保持響應性
//    - tmdbMovies: 將取代原本的 allMockData，作為所有資料的來源
//    - isLoading: 將取代本地的 loading ref，用於 v-loading 指令
const { tmdbMovies, isLoading: loading,existingMovieTitles } = storeToRefs(tmdbStore) // 將 isLoading 別名為 loading
const { fetchAllTmdbMovies, importFromApi, importToLocal } = tmdbStore

// --- 本地元件狀態定義 (UI相關，繼續保留) ---
const selectedMovies = ref([])

const filterParams = reactive({
  name: '',
  releaseDateRange: [],
  certification: null,
  popularity: null,
})

const activeFilterParams = reactive({
  name: '',
  releaseDateRange: [],
  certification: null,
  popularity: null,
})

const sortParams = reactive({
  prop: 'releaseDate',
  order: 'descending',
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
})

const importDialogVisible = ref(false)
const importing = ref(false) // 這個 importing 用於對話框按鈕的 loading，可以保留
const importParams = reactive({
  dateRange: [],
})

const resetImportParams = () => {
  importParams.dateRange = []
  ElMessage.info('擷取日期已清除')
}

// --- 核心響應式邏輯 (computed properties) ---
// 4. 修改 computed properties 的資料來源為 Store 中的 tmdbMovies
// 【新增】一個 computed 屬性，它會先過濾掉已經存在於本地的電影
const availableTmdbMovies = computed(() => {
  // 如果「已存在ID列表」還沒載入好，就先顯示全部，避免畫面空白
  if (!existingMovieTitles.value || existingMovieTitles.value.size === 0) {
    return tmdbMovies.value;
  }
  // 進行過濾：只保留那些 tmdbMovieId 不在 existingMovieTmdbIds 集合中的電影
  return tmdbMovies.value.filter(movie => !existingMovieTitles.value.has(movie.titleLocal));
});

// 【已修改】filteredData 現在的資料來源是上面那個過濾好的 availableTmdbMovies
const filteredData = computed(() => {
  // <<-- 資料來源從 tmdbMovies.value 改為 availableTmdbMovies.value
  let data = availableTmdbMovies.value 
  if (activeFilterParams.name) {
    const keyword = activeFilterParams.name.toLowerCase()
    data = data.filter(
      (movie) =>
        movie.titleLocal.toLowerCase().includes(keyword) ||
        movie.titleEnglish.toLowerCase().includes(keyword),
    )
  }
  if (activeFilterParams.certification) {
    data = data.filter((movie) => movie.certification === activeFilterParams.certification)
  }
  if (activeFilterParams.releaseDateRange && activeFilterParams.releaseDateRange.length === 2) {
    const [start, end] = activeFilterParams.releaseDateRange
    data = data.filter((movie) => movie.releaseDate >= start && movie.releaseDate <= end)
  }
  if (activeFilterParams.popularity !== null) {
    switch (activeFilterParams.popularity) {
      case 1000:
        data = data.filter((movie) => movie.popularity > 1000)
        break
      case 500:
        data = data.filter((movie) => movie.popularity > 500)
        break
      case 100:
        data = data.filter((movie) => movie.popularity > 100)
        break
      case 10:
        data = data.filter((movie) => movie.popularity >= 10 && movie.popularity <= 100)
        break
      case 0:
        data = data.filter((movie) => movie.popularity < 10)
        break
    }
  }
  return data
})

const filteredAndSortedData = computed(() => {
  let data = [...filteredData.value]
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

const getHeaderCellStyle = ({ column }) => {
  if (column.type === 'selection') {
    return {}
  }
  return { 'pointer-events': 'none' }
}

// --- 方法定義 ---

// 5. 頁面掛載時，呼叫 Store 的 Action 來獲取資料
onMounted(() => {
  fetchAllTmdbMovies()
})

const handleSearch = () => {
  pagination.currentPage = 1
  Object.assign(activeFilterParams, JSON.parse(JSON.stringify(filterParams)))
  ElMessage.success('已根據條件查詢到 ' + filteredAndSortedData.value.length + ' 筆資料')
}

const resetFilters = () => {
  filterParams.name = ''
  filterParams.releaseDateRange = []
  filterParams.certification = null
  filterParams.popularity = null
  handleSearch()
  ElMessage.info('篩選條件已重置')
}

const handleSelectionChange = (val) => {
  selectedMovies.value = val
}

const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop
  sortParams.order = order
  pagination.currentPage = 1
}

const openImportDialog = () => {
  importParams.dateRange = []
  importDialogVisible.value = true
}

// 6. 將 handleApiImport 方法對接到 Store 的 Action
const handleApiImport = async () => {
  if (!importParams.dateRange || importParams.dateRange.length !== 2) {
    ElMessage.warning('請選擇完整的日期範圍')
    return
  }
  const [startDate, endDate] = importParams.dateRange
  importing.value = true
  ElMessage.info(`已發送匯入請求，日期範圍: ${startDate} 至 ${endDate}`)

  try {
    // 呼叫 Store Action
    await importFromApi(startDate, endDate)
    importDialogVisible.value = false
    ElMessageBox.confirm(
      'TMDB 電影資料匯入任務已在後端執行！請問是否要立即重新整理列表以查看新資料？',
      '匯入已啟動',
      { confirmButtonText: '立即重新整理', cancelButtonText: '稍後手動整理', type: 'success' },
    ).then(() => {
      // 匯入成功後，重新獲取列表
      fetchAllTmdbMovies()
    })
  } catch (error) {
    console.error('從 TMDB API 匯入失敗:', error)
    ElMessage.error('從 TMDB API 匯入失敗，請檢查後端日誌。')
  } finally {
    importing.value = false
  }
}

// 7. 將 handleBatchImportToLocal 方法對接到 Store 的 Action
const handleBatchImportToLocal = () => {
  if (selectedMovies.value.length === 0) return
  ElMessageBox.confirm(
    `您已勾選 ${selectedMovies.value.length} 部電影。確定要將它們匯入至本地電影資料庫嗎？`,
    '批次匯入確認',
    { confirmButtonText: '確定匯入', cancelButtonText: '取消', type: 'info' },
  )
    .then(async () => {
      const idsToImport = selectedMovies.value.map((m) => m.tmdbMovieId)
      ElMessage.info(`正在將 ${idsToImport.length} 部電影匯入至本地資料庫...`)

      try {
        // 呼叫 Store Action
        await importToLocal(idsToImport)
        ElMessage.success('選定的電影已成功匯入本地資料庫！')
        // 匯入成功後，重新獲取列表，以刷新（移除已匯入的電影）
        fetchAllTmdbMovies()
        selectedMovies.value = [] // 清空勾選
      } catch (error) {
        console.error('批次匯入至本地資料庫失敗:', error)
        ElMessage.error('批次匯入失敗，請檢查後端日誌。')
      }
    })
    .catch(() => {
      ElMessage.info('已取消操作')
    })
}
</script>

<style scoped>
.tmdb-staging-container {
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

/* .badge-item {
  margin-right: 15px;
} */

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.poster-image {
  width: 70px;
  height: 100px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f0f2f5;
  color: #a8abb2;
  font-size: 24px;
  border-radius: 4px;
}

.dialog-tip {
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.6;
  text-align: left;
  background-color: #ecf5ff;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #d9ecff;
}

.el-form--inline .el-form-item {
  margin-right: 20px;
  margin-bottom: 18px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}

/* 覆寫 el-table 的邊框顏色 CSS 變數 */
:deep(.el-table) {
  --el-table-border-color: #dcdfe6;
}

/* 修改 el-descriptions 的邊框顏色 */
:deep(.el-descriptions__cell) {
  border-color: #dcdfe6 !important;
}

/* 如果需要改整個 table 的邊框 */
:deep(.el-descriptions__table) {
  border-color: #dcdfe6 !important;
}

/* 如果想要 label 跟內容分隔線也跟著變 */
:deep(.el-descriptions__label),
:deep(.el-descriptions__content) {
  border-color: #dcdfe6 !important;
}

/* 分頁器的外層包裹容器，我們將它設定為 flex 佈局 */
.pagination-wrapper {
  margin-top: 20px; /* 與上方表格的間距 */
  display: flex; /* 啟用 Flexbox 佈局 */
  justify-content: flex-end; /* 讓內部的所有項目（總筆數文字、分頁器）都靠右對齊 */
  align-items: center; /* 讓內部的所有項目在垂直方向上居中對齊 */
}

/* 自訂「共 X 筆」這段文字的樣式 */
.pagination-total {
  margin-right: 16px; /* 與右側的分頁器保持 16px 的間距 */
  font-weight: 500; /* 字體粗細適中 */
  color: var(--el-text-color-regular); /* 使用 Element Plus 的 CSS 變數來設定顏色 */
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
</style>
