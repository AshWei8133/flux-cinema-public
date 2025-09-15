<template>
  <main class="homepage-content">
    <HeroCarousel />
    <BookingSection id="booking-section" />
    <MovieShowcase />
  </main>
</template>

<script setup>
// 匯入首頁需要用到的三個主要區塊組件
import HeroCarousel from '@/features/Event/components/HeroCarousel.vue'
import BookingSection from '@/components/fluxapp/BookingSection.vue'
import MovieShowcase from '@/features/Movies/components/MovieShowcase.vue'
import { nextTick, watch } from 'vue'
import { useUiStore } from '@/stores/uiStore'
import { storeToRefs } from 'pinia'

const uiStore = useUiStore()
const { scrollTarget } = storeToRefs(uiStore)

/**
 * 定義一個可重用的滾動函式
 * @param {string} selector - CSS 選擇器
 */
const performScroll = (selector) => {
  nextTick(() => {
    const element = document.querySelector(selector)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

// 監聽來自 Store 的滾動指令
watch(scrollTarget, (newTarget) => {
  if (newTarget) {
    performScroll(newTarget)
    // 指令執行完畢後，立刻將指令清空，避免重複觸發
    uiStore.setScrollTarget(null)
  }
})
</script>

<style scoped>
/* 我們將原本屬於外層容器的樣式移到這裡，
  讓這個 Homepage 組件自己管理自己的樣式，
  達到更好的封裝效果。
  我把 class 名稱從 .home-page 改為 .homepage-content 讓語意更清晰
*/
.homepage-content {
  width: 100%;
  background-color: #141414;
}
</style>
