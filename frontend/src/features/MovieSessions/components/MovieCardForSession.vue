<template>
  <div class="movie-card" :draggable="true" @dragstart="handleDragStart">
    <img :src="posterSrc" :alt="movie.titleLocal" class="movie-poster" />
    <div class="movie-info">
      <h4 class="movie-title">{{ movie.titleLocal }}</h4>
      <p class="movie-duration">{{ movie.durationMinutes }} 分鐘</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import noImg from '@/assets/images/noimg.png'

const props = defineProps({
  movie: {
    type: Object,
    required: true,
  },
})

// 計算屬性：檢查 movie.posterImageUrl 是否存在，如果不存在，則回傳預設圖片路徑
const posterSrc = computed(() => {
  // 檢查 movie.posterImageUrl 是否為有效值 (非 null、非空字串)
  if (!props.movie.posterImageUrl || props.movie.posterImageUrl.trim() === '') {
    // 回傳 public 目錄下的預設圖片路徑
    return noImg
  }
  return props.movie.posterImageUrl
})

// 處理拖曳開始
const handleDragStart = (e) => {
  // 將電影物件轉換為 JSON 字串，並儲存到 dataTransfer 中
  e.dataTransfer.setData('application/json', JSON.stringify(props.movie))
}
</script>

<style scoped>
/* 樣式保持不變 */
.movie-card {
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
  cursor: pointer;
}

.movie-card:hover {
  transform: translateY(-5px);
}

.movie-poster {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.movie-info {
  padding: 8px;
  text-align: center;
  background-color: #ffffff;
}

.movie-title {
  font-size: 14px;
  margin: 0;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.movie-duration {
  font-size: 12px;
  color: #666;
  margin: 4px 0 0;
}
</style>
