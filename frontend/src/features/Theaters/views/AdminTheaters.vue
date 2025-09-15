<template>
  <!-- 篩選框及新增影廳區域 -->
  <div class="action-bar">
    <el-row :gutter="20" class="actions-row">
      <el-col :span="8">
        <TheaterTypeSelect v-model="selectedTheaterTypeId" />
        <div v-if="theaterTypesError" class="error-inline-message">
          <p>載入影廳類別失敗: {{ theaterTypesError }}</p>
        </div>
      </el-col>
      <el-col :span="12" class="add-button-col">
        <AdminButton type="primary" @click="handleAddNewTheater">新增影廳</AdminButton>
      </el-col>
    </el-row>
  </div>

  <!-- 數據載入區域 -->
  <div v-if="isTheatersLoading" class="loading-state">
    <p>數據載入中...</p>
    <el-skeleton animated :rows="5" />
  </div>

  <!-- 載入失敗區域 -->
  <div v-else-if="theatersError" class="error-state">
    <p>載入影廳列表失敗：{{ theatersError }}</p>
    <AdminButton @click="theaterStore.fetchAllTheaters()">重試</AdminButton>
  </div>

  <!-- 當沒有影廳數據的處理區域 -->
  <div v-else-if="filteredTheaters.length === 0" class="empty-state">
    <el-empty description="沒有影廳資料"></el-empty>
  </div>

  <!-- 呈現影廳列表的區域 -->
  <!-- <div v-else></div> -->
  <div v-else class="theater-cards-container">
    <el-row :gutter="20">
      <el-col
        v-for="theater in filteredTheaters"
        :key="theater.theaterId"
        :span="6"
        class="card-col"
      >
        <TheaterCard :theater="theater" />
      </el-col>
    </el-row>
  </div>

  <!-- 暫時使用的table -->
  <!-- <div v-else class="theater-table-container">
    <el-table :data="filteredTheaters" border style="width: 100%">
      <el-table-column prop="theaterId" label="ID" width="80"></el-table-column>
      <el-table-column prop="theaterName" label="影廳名稱"></el-table-column>
      <el-table-column prop="theaterTypeName" label="影廳類型"></el-table-column>
      <el-table-column prop="totalSeats" label="總座位數" width="120"></el-table-column>
    </el-table>
  </div> -->
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useTheaterStore } from '../store/useTheaterStore'
import { storeToRefs } from 'pinia'
import AdminButton from '@/components/admin/AdminButton.vue'
import { useRouter } from 'vue-router'
import TheaterTypeSelect from '../components/TheaterTypeSelect.vue'
import TheaterCard from '../components/TheaterCard.vue'

// 數據
// 取得路由物件
const router = useRouter()

// 取用stores中資料
const theaterStore = useTheaterStore()
const { theaters, theatersError, theaterTypesError, isTheatersLoading } = storeToRefs(theaterStore)
// 儲存被選取的影廳類別 ID
const selectedTheaterTypeId = ref(null)

// 計算屬性
// 使用 computed 屬性來動態過濾影廳列表
const filteredTheaters = computed(() => {
  // 如果沒有選擇任何類別，或者清除了選項，就顯示所有影廳
  if (!selectedTheaterTypeId.value) {
    return theaters.value
  }
  // 否則，過濾出符合類別 ID 的影廳
  return theaters.value.filter((theater) => theater.theaterTypeId === selectedTheaterTypeId.value)
})

// 方法區
// 處理點擊「新增影廳」按鈕的函式
const handleAddNewTheater = () => {
  console.log('新增影廳按鈕被點擊！')
  // 跳轉至新增影廳頁面
  router.push({ name: 'AdminTheaterAdd' })
}

// 掛載時，先執行查詢全部影廳操作
onMounted(() => {
  // 取得所有影廳資料
  theaterStore.fetchAllTheaters()
})
</script>

<style scoped>
.theater-table-container {
  padding: 20px;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 50px;
  color: #909399;
}

.action-bar {
  margin: 2%;
  margin-bottom: 1.5%;
}
.actions-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.add-button-col {
  text-align: right;
}

.theater-cards-container {
  padding-left: 20px;
  padding-right: 20px;
}

.card-col {
  margin-bottom: 20px;
}
</style>
