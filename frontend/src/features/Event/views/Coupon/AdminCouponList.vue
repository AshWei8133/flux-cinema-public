<template>
  <div class="admin-announcements-container">
    <div class="table-controls">
      <!-- 搜尋表單區塊 -->
      <el-form :inline="true" :model="searchParams" class="search-form">
        <el-form-item label="優惠券名稱 / 代碼">
          <el-input
            v-model="searchParams.keyword"
            placeholder="請輸入關鍵字"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        <el-form-item label="狀態">
          <el-select v-model="searchParams.status" placeholder="全部狀態" clearable>
            <el-option label="全部" value="ALL" />
            <el-option label="啟用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜尋</el-button>
          <el-button @click="clearSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 優惠券列表表格 -->
    <el-table
      :data="coupons"
      style="width: 100%"
      v-loading="loading"
      @sort-change="handleSortChange"
      :default-sort="{ prop: sortBy, order: sortOrder === 'asc' ? 'ascending' : 'descending' }"
    >
      <el-table-column prop="serialNumber" label="序號" align="center" sortable="custom">
        <template #default="{ row }">{{ row.serialNumber || row.serial_number || '—' }}</template>
      </el-table-column>

      <el-table-column prop="couponName" label="名稱" align="center" sortable="custom">
        <template #default="{ row }">{{ row.couponName || row.coupon_name || '—' }}</template>
      </el-table-column>

      <el-table-column prop="couponDescription" label="描述" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.couponDescription || row.coupon_description || '—' }}
        </template>
      </el-table-column>

      <el-table-column label="折扣" align="center" width="140">
        <template #default="{ row }">
          <el-tag :type="discountTag(row).type">
            {{ discountTag(row).text }}
          </el-tag>
        </template> </el-table-column
      >>

      <el-table-column label="最低消費" align="center" sortable="custom" prop="minimumSpend">
        <template #default="{ row }">
          <span v-if="row.minimumSpend != null || row.minimum_spend != null">
            ${{ row.minimumSpend ?? row.minimum_spend }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="狀態" align="center" sortable="custom">
        <template #default="{ row }">
          <el-tag :type="(row.status || '').toUpperCase() === 'ACTIVE' ? 'success' : 'info'">
            {{ (row.status || 'INACTIVE') === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="redeemableTimes" label="每人領取" align="center" sortable="custom">
        <template #default="{ row }">
          {{ row.redeemableTimes ?? row.redeemable_times ?? 1 }}
        </template>
      </el-table-column>

      <el-table-column prop="quantity" label="數量" align="center" sortable="custom" />

      <el-table-column label="操作" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="detail(row)">詳情</el-button>
          <el-button size="small" type="danger" @click="remove(row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁器 -->
    <div class="pagination-container">
      <el-pagination
        v-if="total > 0"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import httpClient from '@/services/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const coupons = ref([])
const total = ref(0)

const currentPage = ref(1)
const pageSize = ref(5)

const searchParams = reactive({
  keyword: '',
  status: 'ALL',
})

// 排序（支援後端或前端 fallback）
const sortBy = ref('couponName') // 可改 'createdAt' 或後端支援欄位
const sortOrder = ref('desc') // 'asc' | 'desc'

// 泛用解析（支援 Page、data、array）
const parseList = (data) => {
  if (Array.isArray(data)) {
    total.value = data.length
    return data
  }
  if (data && Array.isArray(data.content)) {
    total.value = Number(data.totalElements ?? data.total ?? data.content.length)
    return data.content
  }
  if (data && Array.isArray(data.data)) {
    total.value = Number(data.total ?? data.data.length)
    return data.data
  }
  total.value = 0
  return []
}

// 將「PERCENTAGE / FIXED / 無」轉成統一顯示樣式
const discountTag = (row) => {
  const type = (row.discountType || row.coupon_category || '').toUpperCase()
  const amtRaw = row.discountAmount ?? row.discount_amount
  const amt = Number(amtRaw)

  // 1) 百分比（以「付款比例」定義）：80 => 8折、95 => 9.5折；同時容忍 0.8 => 8折
  if (type === 'PERCENTAGE' && Number.isFinite(amt)) {
    const percent = normalizePercent(amt) // 0.8 => 80, 80 => 80
    if (percent === null) return { type: 'info', text: '基準票價' }

    if (percent <= 0) return { type: 'success', text: '0 折（免費）' }
    if (percent >= 100) return { type: 'info', text: '原價' }

    const fold = Math.round((percent / 10) * 10) / 10 // 取到 1 位小數
    const foldText = Number.isInteger(fold) ? `${fold} 折` : `${fold.toFixed(1)} 折`
    return { type: 'success', text: foldText }
  }

  // 2) 固定金額：顯示「折抵 N 元」
  if (type === 'FIXED' && Number.isFinite(amt) && Math.abs(amt) > 0) {
    const dollars = Math.abs(amt)
    return { type: 'warning', text: `折抵 ${dollars.toLocaleString('zh-TW')} 元` }
  }

  // 3) 其他/未設定
  return { type: 'info', text: '基準票價' }
}

// 允許兩種後端表示法：0.8（比例）或 80（百分比）
function normalizePercent(n) {
  if (!Number.isFinite(n)) return null
  if (n <= 1 && n >= 0) return n * 100 // 0.8 -> 80
  if (n > 1 && n <= 100) return n // 80 -> 80
  return null // 其他異常值
}

const fetchCoupons = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status !== 'ALL' ? searchParams.status : undefined,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value,
    }
    const res = await httpClient.get('/admin/coupons', { params })
    const list = parseList(res)
    // 若後端未排序，前端補一手（穩妥派）
    if (!params.sortBy && !params.sortOrder && list?.length) {
      coupons.value = list
    } else {
      coupons.value = [...list].sort((a, b) => {
        const key = sortBy.value
        const va = a?.[key] ?? a?.[key + '_'] ?? ''
        const vb = b?.[key] ?? b?.[key + '_'] ?? ''
        if (typeof va === 'number' && typeof vb === 'number') {
          return sortOrder.value === 'asc' ? va - vb : vb - va
        }
        return sortOrder.value === 'asc'
          ? String(va).localeCompare(String(vb))
          : String(vb).localeCompare(String(va))
      })
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '載入列表失敗')
    coupons.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchCoupons()
}

const clearSearch = () => {
  searchParams.keyword = ''
  searchParams.status = 'ALL'
  sortBy.value = 'couponName'
  sortOrder.value = 'desc'
  currentPage.value = 1
  fetchCoupons()
}

const handleSortChange = ({ prop, order }) => {
  sortBy.value = prop || 'couponName'
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  fetchCoupons()
}

const handlePageChange = (p) => {
  currentPage.value = p
  fetchCoupons()
}

const handleSizeChange = (s) => {
  pageSize.value = s
  currentPage.value = 1
  fetchCoupons()
}

const goToAdd = () => {
  router.push({ name: 'AdminCouponCreate' }) // 你的路由名稱若不同，改這裡
}

const detail = (row) => {
  router.push({ name: 'AdminCouponDetail', params: { id: row.couponId ?? row.id } })
}

const remove = async (row) => {
  await ElMessageBox.confirm(
    `確定要刪除「${row.couponName || row.coupon_name || '這張券'}」？`,
    '刪除確認',
    { type: 'warning' },
  )
  try {
    await httpClient.delete(`/admin/coupons/${row.couponId ?? row.id}`)
    ElMessage.success('已刪除')
    fetchCoupons()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '刪除失敗')
  }
}

onMounted(fetchCoupons)
watch(
  () => route.query.t,
  () => fetchCoupons(),
)
</script>

<style scoped>
/* 與公告一致的容器與控件區 */
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
  margin-bottom: 20px;
  gap: 10px;
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.search-form .el-form-item {
  margin-bottom: 0;
  width: 250px;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 黑底白字表頭（與你前案一致） */
:deep(.el-table .el-table__header th) {
  background-color: #000 !important;
  color: #fff !important;
  font-weight: 600;
  text-align: center;
}

/* 響應式 */
@media (max-width: 768px) {
  .table-controls {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-form {
    width: 100%;
  }
  .search-form .el-form-item {
    width: 100%;
  }
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}
</style>
