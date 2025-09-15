<!-- 場次選擇日曆表頭元件 -->
<template>
  <!-- 日曆表頭區域 -->
  <div class="custom-calendar-header">
    <!-- 表頭左側-顯示當前年月 -->
    <span class="calendar-header-month clickable" @click="$emit('toggle-selector')">
      {{ getChineseHeader(selectedDate) }}
    </span>

    <!-- 表頭右側控制項button，包含【上個月】、【今天】、【下個月】 -->
    <div class="calendar-header-actions">
      <el-button-group>
        <el-button @click="$emit('prev-month')">上個月</el-button>
        <el-button :type="isTodayMonth ? 'primary' : ''" @click="$emit('today')">今天</el-button>
        <el-button @click="$emit('next-month')">下個月</el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script setup>
import { ElButton, ElButtonGroup } from 'element-plus'

// 1. 定義這個元件能接收的 props
const props = defineProps({
  selectedDate: {
    type: Date,
    required: true,
  },
  isTodayMonth: {
    type: Boolean,
    required: true,
  },
})

// 2. 定義這個元件能發出的事件 (emits)
const emits = defineEmits(['prev-month', 'next-month', 'today', 'toggle-selector'])

// 3. 將原來的 getChineseHeader 方法移到這裡
// 格式化日期，用於日曆表頭顯示中文年月
const getChineseHeader = (date) => {
  const dateObj = new Date(date)
  const year = dateObj.getFullYear()
  const month = (dateObj.getMonth() + 1).toString()
  return `${year}年${month}月`
}
</script>

<style scoped>
/* 日曆表頭樣式 */
.custom-calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid var(--el-calendar-border-color);
}
.calendar-header-month {
  font-size: 16px;
  cursor: pointer;
}
.calendar-header-month.clickable:hover {
  color: #409eff;
}
</style>
