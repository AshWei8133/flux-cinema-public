<template>
  <div class="home-view-container">
    <AppHeader />

    <RouterView></RouterView>
  </div>
</template>

<script setup>
// 移除不再直接使用的組件
// import HeroCarousel from '@/features/Event/components/HeroCarousel.vue'
// import BookingSection from '@/components/fluxapp/BookingSection.vue'
// import MovieShowcase from '@/features/Movies/components/MovieShowcase.vue'

// 只需要匯入 AppHeader 和我們新建立的 Homepage
import AppHeader from '@/components/fluxapp/AppHeader.vue'
</script>

<style scoped>
/* 原本的 .home-page 樣式已經搬到 Homepage.vue 裡面了，
  所以這裡可以移除或保留這個檔案自己需要的樣式。
  我新增一個 .home-view-container 來示意。
*/
.home-view-container {
  /* 你可以在這裡放整個視圖容器的樣式，如果需要的話 */
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #141414;
}
</style>
