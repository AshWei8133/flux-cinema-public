<template>
  <!-- 
    在最外層的 div 加上 @mouseenter 和 @mouseleave 事件，
    用來偵測滑鼠是否移到這張卡片上，並更新 isHovered 的狀態。
  -->
  <div class="movie-card" @mouseenter="isHovered = true" @mouseleave="isHovered = false">
    <div class="poster-wrapper">
      <!-- 
        海報圖片：
        我們用 :class 來動態綁定 'is-hidden' class。
        當 isHovered 為 true 且這部電影有預告片時，就加上 'is-hidden' 讓海報淡出。
      -->
      <img 
        :src="movie.poster" 
        :alt="movie.title" 
        :class="{ 'is-hidden': isHovered && videoUrl }"
        @error="$event.target.src = 'https://placehold.co/200x300/1c1c1c/ffffff?text=無海報'" 
      />
      <!-- 
        YouTube 預告片播放器 (iframe)：
        只有在滑鼠移上去、且電影有預告片網址時，這個 iframe 才會被建立並顯示出來。
        :src 綁定我們在 script 中處理好的 videoUrl。
      -->
      <iframe
        v-if="isHovered && videoUrl"
        :src="videoUrl"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
      ></iframe>
    </div>
    
    <!-- 
      【已修改】在下方的資訊區塊加上 @click 事件。
      .stop 修飾符可以防止這個點擊事件意外觸發到父層的其他點擊效果。
    -->
    <div class="movie-info" @click="viewDetails">
      <h4 class="movie-title">{{ movie.title }}</h4>
      <div class="actions">
        <!-- <button class="action-btn play" title="播放">▶</button>
        <button class="action-btn add" title="加入收藏清單">+</button> -->
        <!-- <button class="action-btn details" title="電影詳情">v</button> -->
      </div>
      <!-- <div class="details-row">
        <span v-if="movie.rating" class="movie-rating">⭐ {{ movie.rating }}</span>
      </div> -->
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  movie: {
    type: Object,
    required: true,
  },
});

// 【新增】定義這個元件會發送出去的事件
const emit = defineEmits(['view-details']);

// 【新增】當使用者點擊 .movie-info 時，觸發 'view-details' 事件，並把電影 ID 傳出去
const viewDetails = () => {
  emit('view-details', props.movie.id);
};

// 建立一個 ref 來追蹤滑鼠是否正懸停在這張卡片上
const isHovered = ref(false);

// 建立一個 computed 屬性，專門用來產生適合嵌入的 YouTube 預告片網址
const videoUrl = computed(() => {
  // --- 【偵錯日誌 1】---
  // 當這個 computed 屬性被觸發時，我們先印出從父元件傳來的 trailerUrl 是什麼
  console.log(`[MovieCard] 正在為 "${props.movie.title}" 計算 videoUrl...`);
  console.log('[MovieCard] 收到的 trailerUrl:', props.movie.trailerUrl);

  // 1. 先檢查 movie 物件中是否有 trailerUrl
  if (!props.movie.trailerUrl) {
    console.log('[MovieCard] 結果: 找不到 trailerUrl，回傳 null。');
    return null;
  }

  // 建立一個更強健的函式，用來從各種 YouTube 網址格式中抓取影片 ID
  const getYouTubeID = (url) => {
    // 這個正規表示式可以匹配 "watch?v="、"youtu.be/"、"embed/" 等多種常見格式
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = url.match(regExp);
    // 如果成功匹配，且抓取到的 ID 長度為 11 (YouTube ID 的標準長度)，就回傳 ID
    return (match && match[2].length === 11) ? match[2] : null;
  };

  // 2. 使用新函式來解析影片 ID
  const videoId = getYouTubeID(props.movie.trailerUrl);
  
  // --- 【偵錯日誌 2】---
  // 印出解析後的 videoId 是什麼
  console.log('[MovieCard] 解析出的 Video ID:', videoId);
    
  if (videoId) {
    // 3. 如果成功抓到 ID，就組合成一個帶有「自動播放」、「靜音」、「無控制項」等參數的嵌入網址
    const embedUrl = `https://www.youtube.com/embed/${videoId}?autoplay=1&mute=1&controls=0&loop=1&playlist=${videoId}&iv_load_policy=3&modestbranding=1&showinfo=0`;
    console.log('[MovieCard] 結果: 成功產生嵌入網址:', embedUrl);
    return embedUrl;
  } else {
    // 如果無法解析，就在 console 提示錯誤
    console.error('無效的預告片網址或無法解析Video ID:', props.movie.trailerUrl);
    console.log('[MovieCard] 結果: 無法解析出 Video ID，回傳 null。');
    return null;
  }
});
</script>

<style scoped>
.movie-card {
  position: relative;
  /* background-color: #1c1c1c; */
  border-radius: 4px;
  overflow: visible;
  transition: all 0.3s ease;
  cursor: pointer;
}

.movie-card:hover {
  /* box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7); */
}

.poster-wrapper {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  z-index: 2;
  /* 【已修改】在 transition 中加入了 transform，讓尺寸變化更平滑 */
  transition:
    aspect-ratio 0.35s cubic-bezier(0.165, 0.84, 0.44, 1),
    transform 0.35s cubic-bezier(0.165, 0.84, 0.44, 1);
  /* 預設是直式海報的長寬比 */
  aspect-ratio: 2 / 3;
  
}

/* 當滑鼠移到卡片上時，將海報容器的長寬比變為 16:9 (YouTube 影片比例) */
.movie-card:hover .poster-wrapper {
  aspect-ratio: 16 / 9;
/* 【已修改】在放大的同時，也將影片視窗往下移動 80px */
transform: scale(1.60) translateY(70px);
box-shadow: 0 10px 30px rgba(0, 0, 0.7, 0.7);

}

/* 海報圖片和影片播放器共用的樣式 */
.poster-wrapper img,
.poster-wrapper iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  /* 關鍵：讓淡入淡出的效果更平滑 */
  transition: opacity 0.3s ease;
}

/* 這個 class 用來讓海報圖片淡出 */
.poster-wrapper img.is-hidden {
  opacity: 0;
}

/* 下方資訊區塊的樣式 (維持您原本的設計) */
.movie-info {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 10px 10px 0px 10px;
  background-color: #1c1c1c;
  border-radius: 0 0 4px 4px;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(100%) scale(0.8);
  transform-origin: top center;
  transition: all 0.3s ease;
  z-index: 1;
}

/* 當滑鼠 hover 到 .movie-card 上時，展開資訊區塊 */
.movie-card:hover .movie-info {
  opacity: 1;
  /* 【已修改】
    - scale(1.25): 讓資訊區塊也跟著放大 25%，寬度才能跟放大的影片視窗對齊。
    - translateY(100%): 維持原本的邏輯，讓它移動到影片視窗的正下方。
  */
  transform: translateY(80%) scale(1.60);
}

.movie-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.7);
  background-color: rgba(42, 42, 42, 0.6);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}
.action-btn:hover {
  border-color: white;
  background-color: rgba(255, 255, 255, 0.2);
}
.action-btn.play {
  background-color: white;
  color: black;
  border-color: white;
}
.action-btn.details {
  margin-left: auto;
}

.details-row {
  display: flex;
  align-items: center;
}

.movie-rating {
  font-size: 14px;
  color: #46d369; /* Netflix 綠 */
  font-weight: bold;
}
</style>
