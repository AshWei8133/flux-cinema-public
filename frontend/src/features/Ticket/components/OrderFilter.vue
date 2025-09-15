<template>
  <el-card shadow="never" class="filter-card">
    <el-form :model="searchData" inline>
      <el-form-item label="訂單編號">
        <el-input v-model="searchData.orderNumber" placeholder="請輸入訂單編號" clearable />
      </el-form-item>

      <el-form-item label="會員帳號">
        <el-input v-model="searchData.username" placeholder="請輸入會員帳號" clearable />
      </el-form-item>

      <el-form-item label="電子郵件">
        <el-input
          v-model="searchData.email"
          placeholder="請輸入電子郵件"
          clearable
          style="min-width: 250px"
        />
      </el-form-item>

      <el-form-item label="手機號碼">
        <el-input v-model="searchData.phone" placeholder="請輸入手機號碼" clearable />
      </el-form-item>

      <el-form-item label="訂單狀態">
        <el-select
          v-model="searchData.status"
          placeholder="所有狀態"
          clearable
          style="min-width: 100px"
        >
          <el-option
            v-for="option in ORDER_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="支付方式">
        <el-select
          v-model="searchData.paymentType"
          placeholder="所有方式"
          clearable
          style="width: 150px"
        >
          <el-option label="現金支付" value="Cash" />
          <el-option label="信用卡" value="ECPay_Credit" />
        </el-select>
      </el-form-item>

      <el-form-item label="訂購日期">
        <el-date-picker
          v-model="searchData.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
          :clearable="true"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :icon="Search" @click="emit('search')">搜尋</el-button>
        <el-button :icon="Refresh" @click="emit('clear')">清除</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
// 從 vue 核心中引入 `computed` 計算屬性，這是實現 v-model 綁定 props 的關鍵。
import { computed } from 'vue'
// 從 Element Plus 的圖示庫中，引入「搜尋」和「重新整理」這兩個圖示元件。
import { Search, Refresh } from '@element-plus/icons-vue'
import { ORDER_STATUS_OPTIONS } from '@/utils/statusMapping.js'

// `defineProps` 是 Vue 3 <script setup> 中用來定義此元件可以接收哪些來自父元件的資料（稱為 props）。
const props = defineProps({
  // 我們定義了一個名為 `modelValue` 的 prop。
  // 在 Vue 中，`modelValue` 是 `v-model` 指令的預設 prop 名稱。
  // 當父元件使用 <OrderFilter v-model="searchParams" /> 時，`searchParams` 物件就會透過這個 prop 傳進來。
  modelValue: {
    type: Object, // 規定傳進來的資料型別必須是物件。
    required: true, // 規定這個 prop 是必須的，父元件一定要傳。
  },
})

// `defineEmits` 用來定義這個元件可以「發射」或「廣播」哪些事件給父元件。
// 就像遙控器上定義了「開機」、「轉台」這些按鈕，按下後電視（父元件）才知道要做什麼。
const emit = defineEmits(['update:modelValue', 'search', 'clear'])

// `searchData` 是這個元件實現 `v-model` 雙向綁定的核心。
// 我們使用 `computed` 計算屬性，並提供 `get` 和 `set` 兩個方法來代理 `props.modelValue`。
// 這樣做的目的是：Vue 規定子元件不能直接修改從父元件傳來的 prop。所以我們需要一個「中間人」。
const searchData = computed({
  // `get` 方法：當模板中需要讀取 `searchData` 的值時（例如顯示在輸入框中），它會執行這裡的程式碼。
  // 作用：直接回傳父元件傳進來的 `props.modelValue` 物件。
  get: () => props.modelValue,

  // `set` 方法：當使用者在輸入框中打字，`v-model` 試圖修改 `searchData` 的值時，它會執行這裡的程式碼。
  // 作用：它不會直接修改 prop，而是發射一個名為 'update:modelValue' 的事件，並把新的值(value)一起傳出去。
  // 父元件上的 `v-model` 會自動監聽這個事件，並更新它自己的資料，從而實現了「子元件通知父元件更新資料」的流程。
  set: (value) => emit('update:modelValue', value),
})
</script>

<style scoped>
/* `scoped` 確保這裡的 CSS 樣式只對這個元件有效，不會影響到其他元件。 */

.filter-card {
  margin-bottom: 20px; /* 卡片底部留出 20px 的外邊距，和下面的表格分隔開 */
  border-radius: 8px; /* 設定卡片圓角 */
  border: 1px solid #b7b9bb51; /* 設定一個淺色的邊框 */
}

/* `:deep()` 是一種特殊的 CSS 選擇器，可以用來穿透 `scoped` 的限制，去修改子元件內部的樣式。
  我們在這裡使用它，是因為 `el-card` 元件內部有一個叫 `.el-card__body` 的 class，我們直接寫 `.el-card__body` 是選不到的。
  透過 `:deep()`，我們就可以精準地選中它並修改其樣式。
*/
:deep(.el-card__body) {
  /* Element Plus 的卡片 body 預設 padding 是 20px，但這會讓表單和卡片底部間距太大。 */
  /* 我們只調整底部的 padding，讓佈局更緊湊。 */
  padding-bottom: 10px;
}
</style>
