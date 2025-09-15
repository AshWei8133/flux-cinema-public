<template>
  <div v-if="sessionInfo" class="session-info-header">
    <div class="info-item">
      <span class="label">電影</span>
      <span class="value">{{ sessionInfo.movieTitle }}</span>
    </div>
    <div class="info-item">
      <span class="label">版本</span>
      <span class="value">{{ sessionInfo.version }}</span>
    </div>
    <div class="info-item">
      <span class="label">影廳</span>
      <span class="value">{{ sessionInfo.theaterName }}</span>
    </div>
    <div class="info-item">
      <span class="label">時間</span>
      <span class="value">{{ formattedShowtime }}</span>
    </div>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 可重用的場次資訊標頭組件 (SessionInfoHeader.vue)
 *
 * - 職責：
 * 純粹用於顯示傳入的場次基本資訊，是一個高度可重用的「展示型組件」。
 */
import { computed } from 'vue'

// ----------------------------------------------------------------
// 組件屬性定義 (Props Definition)
// ----------------------------------------------------------------
const props = defineProps({
  /**
   * 場次資訊物件，包含電影標題、版本、影廳、時間等。
   */
  sessionInfo: {
    type: Object,
    required: true,
  },
})

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------

/**
 * 格式化從後端獲取的 ISO 格式時間字串，使其更適合人類閱讀。
 * @returns {string} 格式化後的日期時間字串，例如 "2025/8/31 19:05"。
 */
const formattedShowtime = computed(() => {
  // 增加保護，確保 props 和 showtime 屬性都存在
  if (!props.sessionInfo || !props.sessionInfo.showtime) return ''
  const date = new Date(props.sessionInfo.showtime)
  return date.toLocaleString('zh-TW', {
    dateStyle: 'short',
    timeStyle: 'short',
    hour12: false,
  })
})
</script>

<style scoped>
/* 場次資訊標頭 (與 SeatSelectionPage 樣式一致) */
.session-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #2a2a2a;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #333;
  max-width: 97%;
}
.info-item {
  display: flex;
  flex-direction: column;
}
.info-item .label {
  font-size: 14px;
  color: #aaa;
  margin-bottom: 4px;
}
.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center; /* 讓文字在容器內垂直置中 */
  height: 24px; /* 給予一個固定的高度 (約 16px 的 1.5 倍行高) */
}
</style>
