<template>
  <!-- 點選表頭年月時顯示下拉式選單區域提供選擇年、月 -->
  <div class="year-month-area">
    <!-- 年月選擇器 -->
    <div class="year-month-selector">
      <el-select
        :model-value="tempYear"
        @update:model-value="$emit('update:temp-year', $event)"
        placeholder="選擇年"
        style="width: 100px"
      >
        <el-option v-for="year in yearList" :key="year" :label="`${year} 年`" :value="year" />
      </el-select>
      <el-select
        :model-value="tempMonth"
        @update:model-value="$emit('update:temp-month', $event)"
        placeholder="選擇月"
        style="width: 80px; margin-left: 10px"
      >
        <el-option v-for="month in 12" :key="month" :label="`${month} 月`" :value="month" />
      </el-select>
    </div>

    <!-- 年月選擇器按鈕區操作 -->
    <div class="action-buttons">
      <el-button @click="$emit('cancel-change')">取消</el-button>
      <el-button type="primary" @click="$emit('confirm-change')">確認</el-button>
    </div>
  </div>
</template>

<script setup>
import { ElButton, ElSelect, ElOption } from 'element-plus'

// 1. 定義 props，注意這裡的 v-model 轉換
const props = defineProps({
  yearList: {
    type: Array,
    required: true,
  },
  tempYear: {
    type: Number,
    required: true,
  },
  tempMonth: {
    type: Number,
    required: true,
  },
})

// 2. 定義 emits，用於雙向綁定和按鈕事件
const emits = defineEmits([
  'update:temp-year',
  'update:temp-month',
  'confirm-change',
  'cancel-change',
])
</script>

<style scoped>
/* 日曆選擇器樣式 */
.year-month-area {
  padding: 15px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: var(--el-fill-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.year-month-selector {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>
