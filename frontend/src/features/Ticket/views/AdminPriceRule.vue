<template>
  <div class="price-rule-management">
    <!-- <h2 class="page-title">票價規則設定</h2> -->

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>影廳類型基礎票價</span>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item
              v-for="theaterType in theaterTypes"
              :key="theaterType.theaterTypeId"
              :label="theaterType.theaterTypeName"
              label-align="center"
            >
              <div class="base-price-cell">
                <template v-if="editingBasePriceId === theaterType.theaterTypeId">
                  <el-input-number
                    v-model="tempBasePrice"
                    :min="0"
                    :step="10"
                    controls-position="right"
                    size="small"
                    class="edit-input"
                  />
                  <el-button
                    :icon="Check"
                    type="success"
                    circle
                    size="small"
                    @click="confirmEdit(theaterType.theaterTypeId)"
                  />
                  <el-button
                    :icon="Close"
                    type="danger"
                    circle
                    size="small"
                    @click="cancelEdit(theaterType.theaterTypeId)"
                  />
                </template>
                <template v-else>
                  <span
                    class="price-text"
                    :class="{ 'price-unset': !basePrices[theaterType.theaterTypeId] }"
                    >$ {{ basePrices[theaterType.theaterTypeId] }}</span
                  >
                  <el-button
                    :icon="Edit"
                    type="primary"
                    circle
                    size="small"
                    @click="startEditing(theaterType)"
                  />
                </template>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>票價規則預覽</span>
            </div>
          </template>

          <el-table :data="priceMatrixData" style="width: 100%" border :resizable="false">
            <el-table-column prop="ticketTypeName" label="票種 \ 影廳類型" minWidth="120" fixed />

            <el-table-column
              v-for="theaterType in theaterTypes"
              :key="theaterType.theaterTypeId"
              :label="theaterType.theaterTypeName"
              align="center"
              minWidth="120"
            >
              <template #default="scope">
                <el-tooltip effect="dark" placement="top">
                  <template #content>
                    <div v-html="scope.row.prices[theaterType.theaterTypeId].tooltip"></div>
                  </template>
                  <span
                    class="final-price"
                    :class="{
                      'price-changed': scope.row.prices[theaterType.theaterTypeId].isChanged,
                    }"
                  >
                    $ {{ scope.row.prices[theaterType.theaterTypeId].finalPrice }}
                  </span>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <transition name="el-fade-in-linear">
      <div v-if="hasChanges" class="action-footer">
        <el-button @click="resetChanges">重設變更</el-button>
        <el-button type="primary" @click="saveChanges">儲存票價規則</el-button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Check, Close } from '@element-plus/icons-vue'
import { useTheaterStore } from '@/features/Theaters/store/useTheaterStore'
import { useAdminTicketStore } from '../store/useAdminTicketStore'
import { storeToRefs } from 'pinia'

// =================================================================
//  元件狀態管理 (Component State)
// =================================================================

// 實例化 store
const theaterStore = useTheaterStore()
const ticketStore = useAdminTicketStore()

// 影廳類型列表
const { theaterTypes } = storeToRefs(theaterStore)
// 票種列表、基礎票價
const { ticketTypes, basePrices } = storeToRefs(ticketStore)

// 用於儲存「已儲存」的基礎票價，作為重設的基準
const initialBasePrices = reactive({})
// 追蹤當前正在編輯的是哪一個基礎票價的 ID
const editingBasePriceId = ref(null)
// 臨時儲存正在編輯的票價數值，方便取消時還原
const tempBasePrice = ref(0)
// isLoading 狀態，初始為 true
const isLoading = ref(true)

// =================================================================
//  計算屬性 (Computed Properties)
// =================================================================

// 判斷當前是否有未儲存的變更
const hasChanges = computed(() => {
  // 如果頁面還在初始載入中，永遠回傳 false
  if (isLoading.value) {
    return false
  }
  // 透過比較當前票價與初始票價的 JSON 字串來判斷是否有變更
  return JSON.stringify(initialBasePrices) !== JSON.stringify(basePrices.value)
})

// 這是頁面的核心：計算整個票價矩陣的資料
const priceMatrixData = computed(() => {
  // 遍歷所有票種，生成表格的每一列 (row)
  return ticketTypes.value.map((ticketType) => {
    const prices = {}
    // 對於每個票種，再遍歷所有影廳類型，計算對應的價格
    theaterTypes.value.forEach((theaterType) => {
      // 分別取得「當前」基礎票價和「初始」基礎票價
      const currentBasePrice = basePrices.value[theaterType.theaterTypeId] || 0
      const initialBasePrice = initialBasePrices[theaterType.theaterTypeId] || 0

      // 進行價格計算
      const result = calculateFinalPrice(currentBasePrice, ticketType)

      // 判斷當前計算出的價格是否與用「初始」票價算出的價格不同
      const initialResult = calculateFinalPrice(initialBasePrice, ticketType)
      result.isChanged = result.finalPrice !== initialResult.finalPrice

      result.ticketTypeId = ticketType.ticketTypeId

      prices[theaterType.theaterTypeId] = result
    })
    return {
      ticketTypeName: ticketType.ticketTypeName,
      prices: prices,
    }
  })
})

// =================================================================
//  方法 (Methods)
// =================================================================

/**
 * 根據基礎票價和票種折扣規則，計算最終票價和提示文字
 * @param {number} basePrice - 基礎票價
 * @param {object} ticketType - 票種物件
 * @returns {object} - { finalPrice, tooltip }
 */
const calculateFinalPrice = (basePrice, ticketType) => {
  // 宣告一個變數來儲存計算過程中的價格
  let calculatedPrice = basePrice

  if (ticketType.discountType === 'PERCENTAGE') {
    calculatedPrice = Math.round(basePrice * ticketType.discountValue)
    const discountDisplay = ticketType.discountValue * 10
    const tooltip = `基礎票價 $${basePrice} x ${discountDisplay.toFixed(1)}折<br/>= $${calculatedPrice}`

    // 確保最終價格不小於 0
    const finalPrice = Math.max(0, calculatedPrice)
    return { finalPrice, tooltip }
  } else if (ticketType.discountType === 'FIXED') {
    calculatedPrice = basePrice + ticketType.discountValue
    const discountDisplay = Math.abs(ticketType.discountValue)
    const tooltip = `基礎票價 $${basePrice} - ${discountDisplay}元<br/>= $${calculatedPrice}`

    // 確保最終價格不小於 0
    const finalPrice = Math.max(0, calculatedPrice)
    return { finalPrice, tooltip }
  } else {
    // 如果沒有折扣 (discountType 為 null)
    // 確保基礎票價也不小於 0
    const finalPrice = Math.max(0, calculatedPrice)
    return {
      finalPrice: finalPrice,
      tooltip: `基礎票價，無折扣`,
    }
  }
}

/**
 * 開始編輯某個基礎票價
 * @param {object} theaterType - 要編輯的影廳類型物件
 */
const startEditing = (theaterType) => {
  editingBasePriceId.value = theaterType.theaterTypeId
  // 將當前價格存入臨時變數，方便取消時還原
  tempBasePrice.value = basePrices.value[theaterType.theaterTypeId]
}

/**
 * 確認編輯
 * @param {number} theaterTypeId - 正在編輯的影廳類型 ID
 */
const confirmEdit = (theaterTypeId) => {
  // 將臨時變數中的新價格，更新到主要的 basePrices 物件中
  basePrices.value[theaterTypeId] = tempBasePrice.value
  // 結束編輯模式
  editingBasePriceId.value = null
}

/**
 * 取消編輯
 * @param {number} theaterTypeId - 正在編輯的影廳類型 ID
 */
const cancelEdit = (theaterTypeId) => {
  // 不做任何事，直接結束編輯模式
  editingBasePriceId.value = null
}

const saveChanges = async () => {
  // 1. 組合要發送到後端的資料 (Payload)
  const payload = []
  // 遍歷當前的票價矩陣資料
  priceMatrixData.value.forEach((row) => {
    // 遍歷該列中每一格的價格資訊
    for (const theaterTypeId in row.prices) {
      const priceInfo = row.prices[theaterTypeId]
      payload.push({
        theaterTypeId: Number(theaterTypeId), // 確保 ID 是數字型別
        ticketTypeId: priceInfo.ticketTypeId,
        price: priceInfo.finalPrice,
      })
    }
  })

  console.log('準備發送到後端的票價規則:', payload)

  try {
    // 2. 呼叫 Store 的 action 來儲存資料
    const response = await ticketStore.savePriceRules(payload)

    if (response && response.success) {
      ElMessage.success(response.message || '票價規則儲存成功！')
      // 3. 儲存成功後，用當前 basePrices 的值來建立一個新的「還原點」
      Object.assign(initialBasePrices, JSON.parse(JSON.stringify(basePrices.value)))
    }
    // 失敗的訊息已由 Axios 攔截器統一處理
  } catch (error) {
    console.error('儲存票價規則失敗:', error)
  }
}

/**
 * 重設所有未儲存的變更
 */
const resetChanges = () => {
  // 將當前 basePrices 的值，還原成 initialBasePrices 的狀態
  Object.assign(basePrices.value, initialBasePrices)
  ElMessage.info('所有變更已重設')
}
// =================================================================
// 生命週期鉤子 (Lifecycle Hooks)
// =================================================================
onMounted(async () => {
  isLoading.value = true
  try {
    // 1. 先並行獲取所有需要的資料
    await Promise.all([
      theaterStore.fetchAllTheaterTypes(),
      ticketStore.fetchAllTicketTypes(),
      ticketStore.fetchBasePrices(),
    ])

    // 2. 在所有資料都從 Store 更新後，先建立一個本地的、可修改的 basePrices 副本
    const localBasePrices = JSON.parse(JSON.stringify(basePrices.value))

    // 3. 執行「補 0」的檢查邏輯 (操作本地副本，而不是 Store 的狀態)
    theaterTypes.value.forEach((theaterType) => {
      const typeId = theaterType.theaterTypeId
      if (!(typeId in localBasePrices)) {
        console.warn(`影廳類型 "${theaterType.theaterTypeName}" 沒有初始基礎票價，預設為 0。`)
        localBasePrices[typeId] = 0
      }
    })

    // 4. 最後，用這個整理好的、最完整的 localBasePrices 來同時設定「還原點」和「當前值」
    Object.assign(initialBasePrices, localBasePrices)
    Object.assign(basePrices.value, localBasePrices)
  } catch (error) {
    console.error('頁面資料初始化失敗:', error)
    ElMessage.error('頁面資料載入失敗，請稍後再試。')
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped>
.price-rule-management {
  padding: 20px;
  background-color: #f0f2f5; /* 新增：設定背景顏色 */
  min-height: 100vh; /* 新增：確保背景至少填滿整個視窗高度 */
}

:deep(.el-card__header) {
  background-color: #484848; /* 設定背景顏色，與表格表頭一致 */
  color: #e9eaeb; /* 設定字體顏色，與表格表頭一致 */
  padding: 12px 20px; /* 調整上下 padding 來控制高度 */
  font-size: 16px; /* 設定字體大小 */
  font-weight: bold; /* 設定字體為粗體 */
}

.base-price-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}
.price-text {
  font-weight: bold;
  margin-right: 10px;
  font-size: 16px;
}

.price-text.price-unset {
  color: #f56c6c; /* Element Plus 的 Danger a.k.a 紅色 */
}
.edit-input {
  width: 100px;
  margin-right: 10px;
}
.final-price {
  font-weight: 500;
  font-size: 16px;
}
.action-footer {
  display: flex;
  margin-top: 10px;
  text-align: right;
  padding: 10px;
  align-items: center;
  justify-content: center;
}

/* 調整右側「票價規則預覽矩陣」el-table 的表頭樣式 */
:deep(.el-table th.el-table__cell) {
  background-color: #f7f7f7 !important; /* 深灰色背景 */
  color: #2d2c2c !important; /* 淺灰色文字 */
  font-weight: 600;
}

:deep(.el-descriptions__label) {
  background-color: #f7f7f7; /* 淺灰底 */
  color: #2d2c2c; /* 深灰字 */
  font-weight: 500;
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

/* 定義一個 CSS 動畫，從高亮顏色漸變到透明 */
@keyframes price-flash {
  from {
    background-color: #fdf6ec; /* Element Plus warning-light-9 color */
  }
  to {
    background-color: transparent;
  }
}

/* 當價格變動時，套用這個動畫 */
.price-changed {
  /* 動畫名稱: price-flash, 持續時間: 1.5秒, 效果: 緩和淡出 */
  animation: price-flash 1.5s ease-out;
  color: #e6a23c; /* 同時讓字體顏色也高亮 */
  font-weight: 600; /* 字體加粗 */
}
</style>
