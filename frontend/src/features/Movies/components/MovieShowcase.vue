<template>
  <div class="movie-showcase-section">
    <!-- 
      【已修改】
      這個元件現在只負責顯示電影列表。
      頁面切換的工作已交給 Vue Router 處理。
    -->
    <MovieList title="現正熱映" @select-movie="handleSelectMovie" />
    <MovieList title="即將上映" @select-movie="handleSelectMovie" />
  </div>
</template>

<script setup>
// 【已修改】導入 useRouter 來處理頁面跳轉
import { useRouter } from 'vue-router';
import MovieList from './MovieList.vue';

// 【已修改】建立 router 實例
const router = useRouter();

// 【已修改】處理函式現在會使用 router.push() 來導航到新的頁面
const handleSelectMovie = (movieId) => {
  // 當這個函式被呼叫時 (接收到來自 MovieList 的信號)
  console.log('MovieShowcase (父層) 已收到信號，準備跳轉到電影 ID:', movieId);
  
  // 使用 router.push() 來跳轉到一個新的 URL
  // 這裡假設您的路由設定中，有一個路徑是 '/movie/:id'
  router.push(`/movie/${movieId}`);
};
</script>

<style scoped>
.movie-showcase-section {
  background-color: #141414;
}
</style>
