<template>
  <div class="booking-progress-bar">
    <div
      v-for="(step, index) in steps"
      :key="step.name"
      class="step-item"
      :class="{
        active: currentStep === index + 1,
        completed: currentStep > index + 1,
      }"
    >
      <div class="step-circle">{{ index + 1 }}</div>
      <div class="step-name">{{ step.name }}</div>
      <div v-if="index < steps.length - 1" class="step-connector"></div>
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 訂票流程進度條組件 (BookingProgressBar.vue)
 *
 * - 職責：
 * 顯示一個多步驟的進度條，並根據傳入的 currentStep prop 來高亮當前步驟和已完成步驟。
 * 這是一個純粹的「展示型組件」，具有高度可重用性。
 */
import { ref } from 'vue'

// ----------------------------------------------------------------
// 組件屬性定義 (Props Definition)
// ----------------------------------------------------------------
defineProps({
  /**
   * 當前所在的步驟編號 (從 1 開始)。
   */
  currentStep: {
    type: Number,
    required: true,
  },
})

// ----------------------------------------------------------------
// 本地狀態 (Local State)
// ----------------------------------------------------------------

/**
 * 定義訂票流程的所有步驟。
 * @type {import('vue').Ref<Array<{name: string}>>}
 */
const steps = ref([
  { name: '付款方式' },
  { name: '票種選擇' },
  { name: '座位選擇' },
  { name: '確認訂單' },
])
</script>

<style scoped>
/* 進度條容器，使用 Flexbox 佈局 */
.booking-progress-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 讓圓圈和文字頂部對齊 */
  width: 100%;
  /* max-width: 800px; */
  margin: 0 auto;
  padding: 20px 0;
  position: relative;
}

/* 每一個步驟項目的容器 */
.step-item {
  display: flex;
  flex-direction: column; /* 讓圓圈和文字垂直排列 */
  align-items: center;
  text-align: center;
  color: #666; /* 預設未完成的顏色 */
  position: relative;
  z-index: 1; /* 確保步驟在連接線之上 */
  flex: 1; /* 讓每個步驟平均分配寬度 */
}

/* 步驟的圓圈 */
.step-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #333; /* 預設背景 */
  border: 2px solid #939292; /* 預設邊框 */
  color: #939292;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  transition: all 0.4s ease;
  margin-bottom: 8px; /* 圓圈和文字的間距 */
}

/* 步驟的名稱 */
.step-name {
  font-size: 16px;
  font-weight: 500;
  transition: color 0.4s ease;
}

/* 連接線 */
.step-connector {
  position: absolute;
  top: 16px; /* 垂直置中於圓圈的高度 */
  left: 50%;
  width: 100%;
  height: 2px;
  background-color: #939292; /* 預設連接線顏色 */
  z-index: 0; /* 在圓圈和文字之下 */
  /* transition: background-color 0.4s ease; */
}

/* 使用 ::before 偽元素製作動畫層 */
.step-connector::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #e50914; /* 動畫的目標顏色 */
  transform: scaleX(0); /* 預設寬度為 0 */
  transform-origin: left; /* 從左邊開始放大 */
  transition: transform 0.4s ease; /* 設定動畫效果 */
}

/* 當步驟完成時，觸發偽元素的動畫 */
.step-item.completed .step-connector::before {
  transform: scaleX(1); /* 將寬度擴展到 100% */
}

.step-item.completed .step-name {
  color: #fff; /* 白色 */
}

.step-item.completed .step-circle {
  border-color: #e50914; /* 紅色邊框 */
  background-color: #e50914; /* 紅色背景，使其填滿 */
  color: #fff; /* 白色數字 */
  transform: scale(1.3); /* 同樣放大以突顯 */
}

/* 當前活動步驟的樣式 */
.step-item.active .step-circle {
  border-color: #e50914; /* 紅色邊框 */
  background-color: #3d3d3d;
  color: #e50914;
  transform: scale(1.3); /* 稍微放大以突顯 */
}
.step-item.active .step-name {
  color: #e50914; /* 紅色 */
  font-weight: bold;
}
</style>
