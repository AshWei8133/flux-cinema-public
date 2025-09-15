<template>
  <div class="dashboard-container">
    <div class="calendar-wrapper">
      <CalendarHeader
        :selected-date="selectedDate"
        :is-today-month="isTodayMonth"
        @prev-month="prevMonth"
        @today="today"
        @next-month="nextMonth"
        @toggle-selector="toggleYearMonthSelector"
      />

      <YearMonthSelector
        v-if="showYearMonthSelector"
        :year-list="yearList"
        v-model:temp-year="tempYear"
        v-model:temp-month="tempMonth"
        @confirm-change="confirmChange"
        @cancel-change="cancelChange"
      />

      <el-calendar v-model="selectedDate">
        <template #header="{ date }"></template>

        <template #date-cell="{ data }">
          <CalendarDateCell
            :data="data"
            :date-status="dateStatus"
            :get-sessions="getSessions"
            :get-chinese-date="getChineseDate"
            :get-status-text="getStatusText"
            :date-has-schedule="dateHasSchedule"
            :cell-classes="cellClasses"
            :is-selected="data.isSelected"
            @date-click="handleDateClick"
          />
        </template>
      </el-calendar>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import CalendarHeader from '../components/CalendarHeader.vue'
import YearMonthSelector from '../components/YearMonthSelector.vue'
import CalendarDateCell from '../components/CalendarDateCell.vue'

import { formatDateString } from '@/utils/dateUtils'
import { useCalendar } from '../hooks/useCalendar'
import { useMovieSessions } from '../hooks/useMovieSessions'

const router = useRouter()

const {
  selectedDate,
  showYearMonthSelector,
  tempYear,
  tempMonth,
  yearList,
  isTodayMonth,
  prevMonth,
  nextMonth,
  today,
  toggleYearMonthSelector,
  confirmChange,
  cancelChange,
  cellClasses,
  getChineseDate,
} = useCalendar()

const { dateStatus, getSessions, getStatusText, dateHasSchedule } = useMovieSessions()

const handleDateClick = (date, isPreview) => {
  const formattedDate = formatDateString(date)

  if (isPreview) {
    // 修正：使用你新設定的路由名稱 'movieScheduleHistory'
    router.push({ name: 'movieScheduleHistory', params: { date: formattedDate } })
    console.log(`點擊歷史日期：${formattedDate}，跳轉至預覽頁面。`)
  } else {
    // 修正：使用你新設定的路由名稱 'movieScheduleEditor'
    router.push({ name: 'movieScheduleEditor', params: { date: formattedDate } })
    console.log(`點擊可編輯日期：${formattedDate}，跳轉至編輯頁面。`)
  }
}
</script>

<style scoped>
@import '@/styles/moviesessions/_calendar-overrides.css';
@import '@/styles/moviesessions/_calendar-statuses.css';

.dashboard-container {
  padding: 10px;
  max-width: 95%;
  margin: 0 auto;
}
.calendar-wrapper {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
</style>
