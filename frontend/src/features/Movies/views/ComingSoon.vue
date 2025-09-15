<template>
    <!-- 使用 Element Plus 的容器元件來做基本排版 -->
    <el-container class="page-container">
      <el-main>
        <!-- 載入中的骨架屏動畫 -->
        <div v-if="isLoading">
          <el-row :gutter="20">
            <el-col v-for="i in 10" :key="i" :xs="12" :sm="8" :md="6" :lg="6" class="mb-5">
              <el-skeleton style="width: 100%" animated>
                <template #template>
                  <el-skeleton-item variant="image" style="width: 100%; height: 350px;" />
                  <div style="padding-top: 14px;">
                    <el-skeleton-item variant="p" style="width: 100%" />
                  </div>
                </template>
              </el-skeleton>
            </el-col>
          </el-row>
        </div>
  
        <!-- 錯誤訊息顯示 -->
        <el-alert v-else-if="error" :title="error" type="error" show-icon center class="error-alert" />
  
        <!-- 電影列表 -->
        <div v-else-if="processedMovies.length > 0">
          <el-row :gutter="20">
            <el-col v-for="movie in processedMovies" :key="movie.id" :xs="24" :sm="8" :md="6" :lg="6" class="mb-5">
              <!-- 
                每部電影都是一張 el-card。
                點擊卡片時，會呼叫 goToDetail 方法來跳轉頁面。
              -->
              <el-card :body-style="{ padding: '0px' }" shadow="hover" class="movie-card" @click="goToDetail(movie.id)">
                <el-image 
                  :src="movie.posterImageUrl" 
                  :alt="movie.titleLocal" 
                  fit="cover" 
                  class="poster-image"
                >
                  <template #error>
                    <div class="image-slot"><span>海報載入失敗</span></div>
                  </template>
                </el-image>
  
                <div class="info-wrapper">
                  <h3 class="title" :title="movie.titleLocal">{{ movie.titleLocal }}</h3>
                  <p class="subtitle" :title="movie.titleEnglish">{{ movie.titleEnglish }}</p>
                  <div class="details">
                    <span>{{ movie.durationMinutes }} 分鐘</span>
                    <el-tag v-if="movie.certification" type="info" size="small" effect="dark" round>
                      {{ movie.certification }}
                    </el-tag>
                  </div>
                  <el-button class="detail-button" type="dark"  @click.stop="goToDetail(movie.id)">詳細內容</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
  
        <!-- 沒有電影時的提示 -->
        <div v-else>
          <el-empty description="目前沒有即將上映的電影。" />
        </div>
      </el-main>
    </el-container>
  </template>
  
  <script setup>
  import { onMounted , computed } from 'vue';
  import { usePublicMovieStore } from '@/features/Movies/store/usePublicMovieStore'; // <-- 請確認您的 store 路徑
  import { storeToRefs } from 'pinia';
  import { useRouter } from 'vue-router';
  
  const movieStore = usePublicMovieStore();
  // 【注意】這裡解構的是 comingSoonMovies
  const { comingSoonMovies, isLoading, error } = storeToRefs(movieStore);
  const router = useRouter();
  
  // 【已修正】讓這個 computed 屬性更聰明，可以處理兩種不同的資料結構
  const processedMovies = computed(() => {
    // 1. 先確保從 Store 拿到的 nowPlayingMovies 是一個有效的陣列
    if (!Array.isArray(comingSoonMovies.value) || comingSoonMovies.value.length === 0) {
      return []; // 如果不是，或是一個空陣列，就直接回傳空陣列
    }

    // 2. 檢查第一筆資料的結構，看看它是不是「未拆封的包裹」 (有 .movie 屬性)
    if (comingSoonMovies.value[0].movie) {
      // 如果是，就執行「拆包裹」的動作
      return comingSoonMovies.value.map(dto => {
        const movie = dto.movie;
        const genres = dto.genres;
        return {
          ...movie,
          genres: genres
        };
      });
    } else {
      // 3. 如果不是 (代表 Store 已經幫我們整理好了)，就直接使用
      return comingSoonMovies.value;
    }
  });
  // 在元件掛載到畫面上後，執行 action 來獲取資料
  onMounted(() => {
    // 【注意】這裡呼叫的是 fetchComingSoonMovies
    movieStore.fetchComingSoonMovies();
  });
  // 處理跳轉到詳細頁面的函式
  const goToDetail = (movieId) => {
    router.push(`/movie/${movieId}`);
  };
  
//   // 獲取電影海報的來源
//   const getPosterSrc = (movie) => {
//     // 檢查電影物件中是否有 posterImage 欄位，並且裡面有資料
//     if (movie && movie.posterImage) {
//       // 如果有，就在 Base64 字串前加上 data URI scheme
//       return `data:image/jpeg;base64,${movie.posterImage}`;
//     }
//     // 如果沒有，就回傳一個預設的佔位圖片
//     return 'https://placehold.co/300x450/1f2937/ffffff?text=無海報';
//   };
  </script>
  
  <style scoped>
  /* 這裡的樣式與 NowPlaying.vue 完全共用 */
  .page-container {
    background-color: #121212;
    padding: 20px;
  }
  .error-alert {
    margin-top: 32px;
  }
  .movie-card {
    cursor: pointer;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    border: none;
    background-color: #1f2937;
    color: #e5e7eb;
  }
  .movie-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5);
  }
  .poster-image {
    width: 100%;
    height: 350px;
  }
  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 350px;
    background: #2d3748;
    color: #a0aec0;
  }
  .info-wrapper {
    padding: 16px;
  }
  .title {
    font-size: 1.125rem;
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .subtitle {
    font-size: 0.875rem;
    color: #9ca3af;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 12px;
  }
  .details {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 0.75rem;
    color: #6b7280;
    margin-bottom: 16px;
  }
  .detail-button {
    width: 100%;
  }
  </style>
  