<template>
    <!-- 頁面最外層容器 -->
    <div class="bg-gray-900 text-white min-h-screen">
      <div class="container mx-auto p-4 md:p-8">
        <!-- 頁面標題 -->
        <h1 class="text-4xl font-bold mb-8 text-center text-red-600 tracking-wider">現正熱映</h1>
  
        <!-- 載入中的骨架屏動畫 -->
        <div v-if="isLoading" class="flex justify-center items-center h-96">
          <el-skeleton style="width: 100%" animated>
            <template #template>
              <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
                <div v-for="i in 10" :key="i">
                  <el-skeleton-item variant="image" style="width: 100%; height: 300px; border-radius: 8px;" />
                  <div style="padding-top: 14px;">
                    <el-skeleton-item variant="p" style="width: 100%" />
                  </div>
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
  
        <!-- 錯誤訊息顯示 -->
        <el-alert v-else-if="error" :title="error" type="error" show-icon class="mt-8">
        </el-alert>
  
        <!-- 電影列表 -->
        <div v-else-if="nowShowingMovies.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <!-- v-for 迴圈，遍歷所有符合條件的電影 -->
          <div v-for="movie in nowShowingMovies" :key="movie.id" 
               class="movie-card bg-gray-800 rounded-lg overflow-hidden relative group">
            <!-- 海報圖片 -->
            <img :src="getPosterUrl(movie.id)" :alt="movie.titleLocal" 
                 class="w-full h-[450px] object-cover transition-transform duration-300 group-hover:scale-105"
                 onerror="this.onerror=null;this.src='https://placehold.co/300x450/1a202c/ffffff?text=海報載入失敗';">
            
            <!-- 海報下方的電影標題 -->
            <div class="p-3">
              <h3 class="text-lg font-semibold truncate" :title="movie.titleLocal">{{ movie.titleLocal }}</h3>
              <p class="text-sm text-gray-400">{{ movie.releaseDate }}</p>
            </div>
  
            <!-- 滑鼠移上去時顯示的遮罩層和按鈕 -->
            <div class="absolute inset-0 bg-black bg-opacity-70 flex flex-col justify-center items-center p-4
                        opacity-0 group-hover:opacity-100 transition-opacity duration-300">
              <div class="text-center space-y-4 w-full">
                <el-button class="w-full btn-gradient" type="primary" size="large" @click="showInfo(movie)">電影資訊</el-button>
                <el-button class="w-full" type="primary" size="large" plain @click="buyTickets(movie)">立即訂票</el-button>
              </div>
            </div>
          </div>
        </div>
  
        <!-- 沒有符合條件的電影時的提示 -->
        <div v-else class="text-center mt-16">
          <el-empty description="目前沒有符合條件的熱映電影。"></el-empty>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useMovieStore } from '@/features/Movies/store/useMovieStore'; // <-- 請確認您的 store 路徑
  import { storeToRefs } from 'pinia';
  import { ElMessage } from 'element-plus'; // 導入 ElMessage
  import httpClient from '@/services/api';
  
  const movieStore = useMovieStore();
  
  // 使用 storeToRefs 從 store 中解構出需要的狀態，這樣它們在 template 中才能保持響應性
  const { nowShowingMovies, isLoading, error } = storeToRefs(movieStore);
  
  // 在元件掛載到畫面上後，執行 action 來獲取並篩選電影資料
  onMounted(() => {
    movieStore.fetchNowShowingMovies();
  });
  
  // 獲取電影海報的 URL
  // 假設您的後端有提供一個 API 端點來獲取海報
const getPosterUrl = (movieId) => {
  return `${import.meta.env.VITE_APP_API_BASE_URL}/admin/movie/movies/${movieId}/poster`;
};
  
  // 按鈕的點擊事件處理函式
  const showInfo = (movie) => {
    ElMessage.info(`您點擊了「${movie.titleLocal}」的電影資訊`);
    // 在這裡您可以加入跳轉到電影詳細頁面的邏輯
    // 例如：router.push(`/movie/${movie.id}`);
  };
  
  const buyTickets = (movie) => {
    ElMessage.success(`準備為「${movie.titleLocal}」訂票...`);
    // 在這裡您可以加入跳轉到訂票網站的邏輯
  };
  </script>
  
  <style scoped>
  /* scoped 樣式只會作用在這個元件內 */
  .movie-card {
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    cursor: pointer;
  }
  .movie-card:hover {
    transform: translateY(-10px); /* 滑鼠移上去時稍微向上浮動 */
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5);
  }
  .btn-gradient {
    background: linear-gradient(45deg, #e50914, #db0000);
    border: none;
  }
  .btn-gradient:hover {
    background: linear-gradient(45deg, #f40612, #e50914);
  }
  </style>
  