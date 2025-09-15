<template>
  <el-container class="film-detail-container">
    <el-main class="main-content">
      <!-- 返回按鈕 -->
      <el-button @click="goback" class="back-button" plain>返回</el-button>

      <!-- 載入中的骨架屏動畫 -->
      <div v-if="isLoading" key="loading" class="p-10">
        <el-skeleton animated>
          <template #template>
            <el-row :gutter="30">
              <el-col :md="8">
                <el-skeleton-item variant="image" style="width: 100%; height: 500px" />
              </el-col>
              <el-col :md="16">
                <el-skeleton-item variant="h1" style="width: 50%" />
                <el-skeleton-item variant="h3" style="width: 30%" />
                <el-skeleton :rows="8" />
              </el-col>
            </el-row>
          </template>
        </el-skeleton>
      </div>

      <!-- 錯誤訊息顯示 -->
      <el-alert
        v-else-if="error"
        key="error"
        :title="error"
        type="error"
        show-icon
        center
        class="error-alert"
      />

      <!-- 電影詳細內容 -->
      <div v-else-if="movieData" key="content">
        <!-- 第一區塊：海報與主要資訊 -->
        <el-row :gutter="40">
          <!-- 左側：海報 -->
          <el-col :xs="24" :sm="10" :md="8">
            <el-image :src="posterSrc" :alt="movieData.titleLocal" fit="cover" class="poster-image">
              <template #error>
                <div class="image-slot"><span>海報載入失敗</span></div>
              </template>
            </el-image>
          </el-col>

          <!-- 右側：詳細資訊 -->
          <el-col :xs="24" :sm="14" :md="16">
            <!-- 頂部資訊區塊 -->
            <el-row justify="space-between" align="top">
              <!-- 左側的主要資訊 -->
              <el-col :span="24">
                <el-tag
                  v-if="movieData.certification"
                  type="info"
                  effect="plain"
                  class="info-tag"
                  >{{ movieData.certification }}</el-tag
                >
                <h1 class="movie-title">{{ movieData.titleLocal }}</h1>
                <h2 class="movie-subtitle">{{ movieData.titleEnglish }}</h2>

                <!-- 【新增】收藏按鈕，只有在登入時才會顯示 -->
                <div v-if="authStore.isAuthenticated" class="favorite-area">
                  <el-button
                    class="heart-button"
                    :class="{ 'is-favorited': isFavorited }"
                    @click="toggleFavorite"
                  >
                  </el-button>
                  <span class="favorite-text">{{ isFavorited ? '已收藏' : '加入收藏' }}</span>
                </div>

                <div class="info-block">
                  <p>上映日期：{{ movieData.releaseDate }}</p>
                  <p>片長：{{ movieData.durationMinutes }} 分鐘</p>
                </div>

                <div class="info-block">
                  <el-tag
                    v-for="genre in genres"
                    :key="genre.genreId"
                    type="primary"
                    effect="dark"
                    round
                    class="genre-tag"
                  >
                    {{ translateGenre(genre.name) }}
                  </el-tag>
                </div>

                <div v-if="directors.length > 0" class="info-block">
                  <h3 class="info-header">導演</h3>
                  <p>{{ directors.map((d) => d.name).join('、') }}</p>
                </div>

                <div v-if="actors.length > 0" class="info-block">
                  <h3 class="info-header">主要演員</h3>
                  <p>{{ actors.map((a) => a.name).join('、') }}</p>
                </div>
              </el-col>
            </el-row>

            <!-- 劇情簡介 -->
            <div class="info-block">
              <h3 class="section-title">劇情簡介</h3>
              <p class="overview-text">{{ movieData.overview }}</p>
            </div>
          </el-col>
        </el-row>

        <MovieShowtimes
          v-if="movieData"
          :movieId="movieData.id"
          :key="movieData.id"
          class="showtimes-wrapper"
        />

        <!-- 第二區塊：預告片和劇照 -->
        <div class="media-section">
          <!-- 電影預告 -->
          <el-card v-if="embedTrailerUrl" shadow="never" class="media-card">
            <template #header>
              <h3 class="section-title">電影預告</h3>
            </template>
            <div class="trailer-wrapper">
              <iframe
                :src="embedTrailerUrl"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen
              ></iframe>
            </div>
          </el-card>

          <!-- 電影劇照 -->
          <el-card v-if="previewImageSrcs.length > 0" shadow="never" class="media-card">
            <template #header>
              <h3 class="section-title">電影劇照</h3>
            </template>
            <el-row :gutter="20" justify="center">
              <el-col v-for="(src, index) in previewImageSrcs" :key="index" :xs="12" :sm="6">
                <el-image
                  :src="src"
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="previewImageSrcs"
                  :initial-index="index"
                  preview-teleported
                />
              </el-col>
            </el-row>
          </el-card>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { onMounted, computed, watch } from 'vue'
import { usePublicMovieStore } from '../store/usePublicMovieStore'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { usePublicMemberProfileStore } from '@/features/Members/store/usePublicMemberProfileStore'
import { useMemberAuthStore } from '@/features/Product/stores/memberAuthStore'
import { translateGenre } from '@/utils/genreTranslator'
import MovieShowtimes from '@/features/MovieSessions/components/front/MovieShowtimes.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true,
  },
})

const router = useRouter()
const goback = () => {
  router.back()
}

const movieStore = usePublicMovieStore()
const { selectedMovie, isLoading, error } = storeToRefs(movieStore)
const { clearSelectedMovie } = movieStore

const movieData = computed(() => {
  return selectedMovie.value ? selectedMovie.value.movie : null
})

const genres = computed(() => {
  return selectedMovie.value ? selectedMovie.value.genres : []
})

const directors = computed(() => {
  return selectedMovie.value ? selectedMovie.value.directors : []
})

const actors = computed(() => {
  return selectedMovie.value ? selectedMovie.value.actors : []
})

const posterSrc = computed(() => {
  if (movieData.value && movieData.value.posterImage) {
    return `data:image/jpeg;base64,${movieData.value.posterImage}`
  }
  return 'https://placehold.co/400x600/121212/ffffff?text=無海報'
})

const embedTrailerUrl = computed(() => {
  if (!movieData.value || !movieData.value.trailerUrl) {
    return null
  }
  const getYouTubeID = (url) => {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#?]*).*/
    const match = url.match(regExp)
    return match && match[2].length === 11 ? match[2] : null
  }
  const videoId = getYouTubeID(movieData.value.trailerUrl)
  if (videoId) {
    return `https://www.youtube.com/embed/${videoId}`
  }
  return null
})

const previewImageSrcs = computed(() => {
  if (!movieData.value) return []
  const srcs = []
  for (let i = 1; i <= 4; i++) {
    const fieldName = `previewImage${i}`
    if (movieData.value[fieldName]) {
      srcs.push(`data:image/jpeg;base64,${movieData.value[fieldName]}`)
    }
  }
  return srcs
})
// --- 會員相關 Stores ---
const authStore = useMemberAuthStore() // 「大門警衛」的實例
const profileStore = usePublicMemberProfileStore() // 「人事部經理」的實例

// 從「人事部經理」那裡拿出收藏 ID 列表
const { favoriteMovieIds } = storeToRefs(profileStore)

// --- 收藏相關邏輯 ---

const isFavorited = computed(() => {
  if (!favoriteMovieIds.value || typeof favoriteMovieIds.value.has !== 'function') {
    return false
  }
  const currentMovieId = Number(props.id)
  return favoriteMovieIds.value.has(currentMovieId)
})

const toggleFavorite = () => {
  const currentMovieId = Number(props.id)
  if (isFavorited.value) {
    // 呼叫「人事部經理」去移除收藏
    profileStore.removeFavorite(currentMovieId)
  } else {
    // 呼叫「人事部經理」去新增收藏
    profileStore.addFavorite(currentMovieId)
  }
}

onMounted(() => {
  movieStore.fetchMovieById(Number(props.id))
  // 【已修正】我們去問「大門警衛」現在是否已登入
  if (authStore.isAuthenticated) {
    // 如果已登入，才請「人事部經理」去拿取最新的收藏列表
    profileStore.fetchFavoriteIds()
  }
})
// 【修改】使用 watch 來確保父元件能響應路由變化
watch(
  () => props.id,
  (newId) => {
    if (newId) {
      // 1. 清理舊的電影資料，這會讓 v-if="movieData" 暫時為 false，從而銷毀舊的子元件
      clearSelectedMovie()

      // 2. 獲取新的電影資料
      movieStore.fetchMovieById(Number(newId))

      // 3. 如果使用者已登入，也重新獲取收藏狀態
      if (authStore.isAuthenticated) {
        profileStore.fetchFavoriteIds()
      }
    }
  },
  { immediate: true }, // immediate: true 確保元件第一次載入時也能執行
)
</script>

<style scoped>
/* 【新增】收藏按鈕區塊的樣式 */
.reserved-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.film-detail-container {
  background-color: #121212;
  min-height: 100vh;
  color: #e5e7eb;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
}
.back-button {
  margin-bottom: 32px;
}
.error-alert {
  margin-top: 32px;
}
.poster-image {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 500px;
  background: #2d3748;
  color: #a0aec0;
  font-size: 16px;
  border-radius: 8px;
}
.movie-title {
  font-size: 2.25rem; /* 36px */
  font-weight: bold;
  color: #ffffff;
}
.movie-subtitle {
  font-size: 1.25rem; /* 20px */
  color: #9ca3af; /* gray-400 */
  margin-bottom: 16px;
}
.info-tag {
  margin-bottom: 8px;
}
.info-block {
  margin-bottom: 24px;
  color: #d1d5db; /* gray-300 */
}
.info-block p {
  margin: 0;
}
.info-header {
  font-size: 1.125rem; /* 18px */
  font-weight: 600;
  margin-bottom: 8px;
  color: #9ca3af; /* gray-400 */
}
.genre-tag {
  margin-right: 8px;
}
.overview-text {
  color: #9ca3af; /* gray-400 */
  line-height: 1.75; /* leading-relaxed */
}
.reserved-area {
  width: 100％;
  height: 150px;
  border: 2px dashed #4b5563; /* gray-600 */
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #6b7280; /* gray-500 */
}
.media-section {
  margin-top: px;
  padding-top: px;
}
.media-card {
  margin-bottom: 32px;
  background-color: transparent;
  border: none;
}
/* 讓 el-card 的標題置中 */
:deep(.el-card__header) {
  text-align: center;
  border-bottom: none;
  padding-bottom: 0;
}
.section-title {
  font-size: 1.5rem; /* 24px */
  font-weight: 600;
  color: #ffffff;
}
.trailer-wrapper {
  max-width: 1160px;
  margin: 0 auto;
  aspect-ratio: 16 / 9;
}
.trailer-wrapper iframe {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
.preview-image {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}
.heart-button {
  position: relative;
  width: 50px;
  height: 45px;
  border: none;
  background-color: transparent; /* 清除 el-button 預設樣式 */
  padding: 0;
  cursor: pointer;
  transition: transform 0.2s ease-in-out;
  margin: 0px 0 10px 0;
}
.heart-button:hover {
  transform: scale(1.1);
}

/* 用 ::before 和 ::after 來畫出愛心上方的兩個圓弧 */
.heart-button::before,
.heart-button::after {
  content: '';
  position: absolute;
  top: 0;
  width: 25px;
  height: 40px;
  border-radius: 25px 25px 0 0;
  /* 預設的「未收藏」顏色 (灰色) */
  background-color: #909399;
  transition: background-color 0.3s ease;
}

/* 當按鈕有 .is-favorited class 時，改變顏色 */
.heart-button.is-favorited::before,
.heart-button.is-favorited::after {
  /* 「已收藏」的顏色 (紅色) */
  background-color: #f56c6c;
}

/* 定位左邊的圓弧 */
.heart-button::before {
  left: 25px;
  transform: rotate(-45deg);
  transform-origin: 0 100%;
}

/* 定位右邊的圓弧 */
.heart-button::after {
  left: 0;
  transform: rotate(45deg);
  transform-origin: 100% 100%;
}
.favorite-text {
  margin-left: 10px;
}
/* 【場次卡片的樣式 */
.showtimes-card {
  margin-top: 40px; /* 與上方的電影資訊區塊產生間距 */
  background-color: transparent; /* 讓背景色由子組件控制 */
  border: none; /* 移除 el-card 的預設邊框 */
  padding: 0;
}

/* 移除 el-card 預設的 padding，讓我們的子組件能填滿整個卡片 */
:deep(.showtimes-card .el-card__body) {
  padding: 0;
}
</style>
