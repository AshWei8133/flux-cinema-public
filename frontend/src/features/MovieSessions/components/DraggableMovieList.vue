<template>
  <el-card class="movie-list-card" shadow="never">
    <el-scrollbar class="flex-fill-scrollbar">
      <div class="movie-list">
        <div v-for="movie in movies" :key="movie.id">
          <MovieCard :movie="movie" />
        </div>
      </div>
    </el-scrollbar>
  </el-card>
</template>

<script setup>
// import draggable from 'vuedraggable' // 不再需要 vuedraggable
import MovieCard from './MovieCardForSession.vue' // 用於顯示單個電影資訊的卡片元件
import { ElCard, ElScrollbar } from 'element-plus' // 引入 Element Plus 的相關元件

// ----------------------------------------------------------------
// 組件接口 (Props)
// ----------------------------------------------------------------
const props = defineProps({
  // 電影資料陣列，由父元件傳入
  movies: {
    type: Array,
    required: true,
  },
  // 列表的標題 (雖然模板中未使用，但定義了 props)
  title: {
    type: String,
    required: true,
  },
})
</script>

<style scoped>
/* ---------------------------------------------------------------- */
/* 主要容器與佈局 (Main Container & Layout)                         */
/* ---------------------------------------------------------------- */

/* 電影列表卡片容器的樣式 */
.movie-list-card {
  border: none; /* 移除邊框 */
  background-color: transparent; /* 設定透明背景 */
  width: 100%;
  height: 100%;
  display: flex; /* 使用 Flexbox 佈局 */
  flex-direction: column; /* 讓子元素垂直排列 */
}

/* * 使用 :deep() 選擇器穿透 scoped CSS 的限制，修改 Element Plus Card 元件的內部樣式。
 * 這是必要的，因為 el-card 預設的 body 有 padding，會導致滾動條佈局不正確。
 */
:deep(.el-card__body) {
  padding: 0; /* 必須為 0，恢復到滾動條可運作的狀態 */
  box-sizing: border-box;
  flex-grow: 1; /* 讓 card body 佔滿所有可用的垂直空間 */
  min-height: 0; /* 在 flex 佈局中，這是讓 flex-grow 生效的關鍵 */
  display: flex;
  flex-direction: column;
}

/* 自訂的滾動條樣式，使其填滿父容器 (.el-card__body) */
.flex-fill-scrollbar {
  flex-grow: 1;
  min-height: 0;
}

/* 同樣使用 :deep() 修改 el-scrollbar 的內部包裹層，確保其高度為 100% */
.flex-fill-scrollbar :deep(.el-scrollbar__wrap) {
  height: 100%;
}

/* ---------------------------------------------------------------- */
/* 拖曳列表樣式 (Draggable List Styles)                             */
/* ---------------------------------------------------------------- */

/* 可拖曳列表本身的樣式 */
.movie-list {
  display: flex;
  flex-direction: column;
  gap: 10px; /* 每個 MovieCard 之間的間距 */
  /* 只設定上、左、右的 padding，底部留給偽元素處理 */
  padding: 10px 10px 0;
  /* 為列表設定一個固定的高度。如果列表內容超過此高度，
     父層的 el-scrollbar 就會因為內容溢出而顯示滾動條。*/
  height: 400px;
}

/* * 使用 ::after 偽元素作為列表的「底部留白」。
 * 這是一個很巧妙的 CSS 技巧，用來解決滾動到底部時，最後一個元素
 * 可能會被遮擋或緊貼邊緣的問題。
 */
.movie-list::after {
  content: ''; /* 偽元素必需的屬性 */
  display: block; /* 讓偽元素像一個區塊元素 */
  /* 這個高度就是我們需要的底部安全距離 */
  height: 10px;
}

/* 列表為空時的狀態樣式 (雖然模板中沒有使用 ElEmpty，但樣式已定義) */
.empty-state {
  padding: 20px 0;
}
</style>
