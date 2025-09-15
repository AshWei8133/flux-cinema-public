<template>
  <el-container class="page-container">
    <el-main>
      <!-- <h1 class="page-title">我的收藏</h1> -->

      <!-- 載入中 -->
      <div v-if="isLoadingFavorites" class="loading-placeholder">
        <p>正在載入您的收藏...</p>
      </div>
      <!-- 錯誤 -->
      <el-alert v-else-if="error" :title="error" type="error" show-icon center />

      <!-- 收藏列表 -->
      <div v-else-if="favorites && favorites.length > 0">
        <el-row :gutter="20">
          <!-- 【已修改】調整了 el-col 的斷點，讓橫向卡片有更多空間 -->
          <el-col v-for="movie in favorites" :key="movie.id" :xs="24" :sm="12" :md="12" :lg="24">
            <el-card
              :body-style="{ padding: '0px' }"
              shadow="hover"
              class="movie-card"
              @click="goToDetail(movie.id)"
            >
              <!-- 【已修改】新增一個 div 來做 flex 佈局 -->
              <div class="card-content">
                <el-image
                  :src="movie.posterImageUrl"
                  :alt="movie.titleLocal"
                  fit="cover"
                  class="poster-image"
                >
                  <template #error>
                    <div class="image-slot-horizontal"><span>海報載入失敗</span></div>
                  </template>
                </el-image>

                <div class="info-wrapper">
                  <div>
                    <h3 class="title" :title="movie.titleLocal">{{ movie.titleLocal }}</h3>
                    <p class="subtitle" :title="movie.titleEnglish">{{ movie.titleEnglish }}</p>
                  </div>
                  <div class="details">
                    <span>{{ movie.durationMinutes }} 分鐘</span>
                    <el-tag
                      v-if="movie.certification"
                      type="info"
                      size="medium"
                      effect="dark"
                      round
                    >
                      {{ movie.certification }}
                    </el-tag>
                  </div>
                  <el-button
                    class="detail-button"
                    type="dark"
                    plain
                    @click.stop="goToDetail(movie.id)"
                    >詳細內容</el-button
                  >
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 沒有收藏時的提示 -->
      <div v-else>
        <el-empty description="您尚未收藏任何電影。" />
        <div style="text-align: center">
          <el-button type="primary" @click="router.push('/movie/nowPlaying')">前往找電影</el-button>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { usePublicMemberProfileStore } from '@/features/Members/store/usePublicMemberProfileStore'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'

const router = useRouter()
const profileStore = usePublicMemberProfileStore()
const { favorites, isLoadingFavorites, error } = storeToRefs(profileStore)

onMounted(() => {
  profileStore.fetchFullFavorites()
})

const goToDetail = (movieId) => {
  if (typeof movieId === 'number' && !isNaN(movieId)) {
    router.push(`/movie/${movieId}`)
  } else {
    console.warn('試圖跳轉到一個無效的電影 ID:', movieId)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #121212;
}
.page-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 2rem;
  color: #e5e7eb;
}
.loading-placeholder {
  color: #9ca3af;
  text-align: center;
  padding: 40px;
}

/* --- 【已修改】卡片樣式 --- */
.movie-card {
  cursor: pointer;
  border: 1px solid #374151; /* gray-700 */
  background-color: #1f2937; /* gray-800 */
  color: #e5e7eb;
  margin-bottom: 20px;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  overflow: hidden; /* 確保內容不會超出圓角 */
}
.movie-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
}

/* 【新增】橫向佈局的容器 */
.card-content {
  display: flex;
}

/* 【已修改】海報樣式 */
.poster-image {
  width: 160px; /* 根據 2:3 的比例計算 */
  height: 240px;
  flex-shrink: 0; /* 防止圖片被壓縮 */
}
.image-slot-horizontal {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 160px;
  height: 240px;
  background: #2d3748;
  color: #a0aec0;
}

/* 【已修改】資訊區塊樣式 */
.info-wrapper {
  display: flex;
  flex-direction: column; /* 讓內容垂直排列 */
  flex-grow: 1; /* 佔滿剩餘空間 */
  padding: 16px;
  min-width: 0; /* 關鍵：防止 flex item 內容溢出 */
}

.title {
  font-size: 1.5rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 0;
  margin-bottom: 4px;
}
.subtitle {
  font-size: 1rem;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 12px;
}
.certificaton {
  font-size: 0.8rem;
  color: #9ca3af;
}
.details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.875rem;
  color: #9ca3af;
  margin-top: auto; /* 關鍵：將此區塊推到底部 */
  padding-top: 10px; /* 增加與上方內容的間距 */
}
.detail-button {
  width: 50%;
  margin-top: 12px;
  position: relative;
  left: 50%;
}
</style>
