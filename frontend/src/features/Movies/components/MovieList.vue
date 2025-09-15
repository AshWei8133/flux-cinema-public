<template>
  <section class="movie-list-section">
    <div class="container">
      <h2 class="section-title">{{ title }}</h2>

      <!-- 載入中的提示 -->
      <div v-if="isLoading" class="loading-container">
        <p>電影載入中...</p>
      </div>

      <!-- 錯誤訊息提示 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
      </div>

      <!-- 電影滑動列 -->
      <div v-else class="movie-row">
        <button
          v-if="showLeftArrow"
          class="scroll-arrow left"
          @click="scrollLeft"
          aria-label="向左滾動"
        >
          &lt;
        </button>

        <div class="movie-list" ref="movieListRef" @scroll="handleScroll">
          <!-- v-for 現在會遍歷從 computed 屬性 'movies' 拿到的電影 -->
          <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" class="movie-item" 
          @view-details="handleSelectMovie"/>
        </div>

        <button
          v-if="showRightArrow"
          class="scroll-arrow right"
          @click="scrollRight"
          aria-label="向右滾動"
        >
          &gt;
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue';
import MovieCard from './MovieCard.vue';
import { usePublicMovieStore } from '../store/usePublicMovieStore'; // <-- 請確認您的 store 路徑
import { storeToRefs } from 'pinia';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
});
const emit = defineEmits(['select-movie']); // 定義這個元件會發送出去的事件
const movieStore = usePublicMovieStore();
const { nowPlayingMovies, comingSoonMovies, isLoading, error } = storeToRefs(movieStore);

const movieListRef = ref(null);
const showLeftArrow = ref(false);
const showRightArrow = ref(false);

const movies = computed(() => {
  let sourceMovies = [];

  if (props.title === '現正熱映') {
    sourceMovies = Array.isArray(nowPlayingMovies.value) ? nowPlayingMovies.value : [];
  } 
  else if (props.title === '即將上映') {
    sourceMovies = Array.isArray(comingSoonMovies.value) ? comingSoonMovies.value : [];
  }

  // 【已修改】在轉換資料時，一併把 trailerUrl 加進去
  return sourceMovies.map((movieFromApi) => ({
    id: movieFromApi.id,
    title: movieFromApi.titleLocal,
    poster: movieFromApi.posterImageUrl,
    rating: movieFromApi.voteAverage,
    trailerUrl: movieFromApi.trailerUrl, // <-- 新增這一行
  }));
});
// 【新增】建立處理函式，當接收到來自 MovieCard 的信號時，再向上發送 'select-movie' 信號
const handleSelectMovie = (movieId) => {
  emit('select-movie', movieId);
  console.log(movieId);
};
// 處理滾動和箭頭顯示的邏輯 (維持原樣)
const handleScroll = () => {
  const el = movieListRef.value;
  if (!el) return;
  const isScrolledToStart = el.scrollLeft <= 1;
  const isScrolledToEnd = el.scrollLeft + el.clientWidth >= el.scrollWidth - 1;
  showLeftArrow.value = !isScrolledToStart;
  showRightArrow.value = !isScrolledToEnd;
};

// 新增一個 watch (監視器) 來監聽 movies 陣列的變化
watch(movies, (newMovies) => {
  // 當 movies 陣列從「空的」變成「有資料」時，這個 watch 就會被觸發
  if (newMovies.length > 0) {
    // nextTick 是一個保證，確保 Vue 已經把所有新的 MovieCard 都畫到畫面上了
    nextTick(() => {
      // 在畫面更新後，我們才手動呼叫 handleScroll 來重新計算一次箭頭的顯示狀態
      handleScroll();
    });
  }
});


const scrollRight = () => {
  const el = movieListRef.value;
  if (!el) return;
  const scrollAmount = el.clientWidth * 0.8;
  el.scrollBy({ left: scrollAmount, behavior: 'smooth' });
};

const scrollLeft = () => {
  const el = movieListRef.value;
  if (!el) return;
  const scrollAmount = el.clientWidth * 0.8;
  el.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
};

let observer;
onMounted(() => {
  if (props.title === '現正熱映') {
    movieStore.fetchNowPlayingMovies();
  }
  else if (props.title === '即將上映') {
    movieStore.fetchComingSoonMovies();
  }

  const el = movieListRef.value;
  if (el) {
    observer = new ResizeObserver(handleScroll);
    observer.observe(el);
  }
});

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
/* --- 整體區塊樣式 --- */
.movie-list-section {
  margin-bottom: 50px;
}
.movie-list-section:last-of-type {
  margin-bottom: 0;
}
.container {
  max-width: 100%;
  margin: 0 auto;
  padding: 0;
}
.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #e5e5e5;
  padding: 0 50px;
  font-weight: bold;
}
/* 載入和錯誤提示的樣式 */
.loading-container, .error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 150px; /* 與 MovieCard 高度差不多 */
  color: #a0aec0;
  padding: 0 50px;
  font-size: 18px;
}
.error-container {
  color: #e53e3e;
}
/* --- 滑動列容器樣式 (維持原樣) --- */
.movie-row {
  position: relative;
}
.scroll-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(calc(-50% - 40px));
  z-index: 20;
  width: 50px;
  height: 100px;
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  color: white;
  font-size: 2.5rem;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}
.movie-row:hover .scroll-arrow {
  opacity: 1;
  pointer-events: auto;
}
.scroll-arrow:hover {
  background-color: rgba(0, 0, 0, 0.8);
}
.scroll-arrow.left {
  left: 0;
  border-radius: 0 10px 10px 0;
}
.scroll-arrow.right {
  right: 0;
  border-radius: 10px 0 0 10px;
}
.movie-list {
  display: flex;
  gap: 10px;
  padding: 0 50px;
  overflow-x: auto;
  padding-bottom: 120px;
  margin-bottom: -120px;
  padding-top: 80px;
  margin-top: -60px;
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.movie-list::-webkit-scrollbar {
  display: none;
}
:deep(.movie-item) {
  width: 200px;
  transform-origin: top center;
  transition:
    transform 0.35s cubic-bezier(0.165, 0.84, 0.44, 1),
    margin 0.35s cubic-bezier(0.165, 0.44, 1),
    opacity 0.35s ease;
  flex-shrink: 0;
}
.movie-list:hover :deep(.movie-item) {
  opacity: 0.4;
}
.movie-list:hover :deep(.movie-item:hover) {
  opacity: 1;
  z-index: 10;
  transform: scale(1.4) translateY(-70px);
  margin: 0 55px;
}
.movie-list:hover :deep(.movie-item:hover + .movie-item) {
  transform: translateX(70px);
}
.movie-list:hover :deep(.movie-item:has(+ .movie-item:hover)) {
  transform: translateX(-70px);
}
</style>






