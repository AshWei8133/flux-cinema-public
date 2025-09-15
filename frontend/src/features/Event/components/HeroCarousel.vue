<template>
  <div
    class="hero-carousel"
    role="region"
    aria-roledescription="carousel"
    aria-label="宣傳輪播"
    @mouseenter="pauseOnHover ? stopAutoplay() : null"
    @mouseleave="pauseOnHover ? startAutoplay() : null"
    @keydown.left.prevent="prevSlide"
    @keydown.right.prevent="nextSlide"
    @keydown.enter.prevent="onSlideClick(currentIndex)"
    @keydown.space.prevent="onSlideClick(currentIndex)"
    @touchstart.passive="onTouchStart"
    @touchmove.passive="onTouchMove"
    @touchend.passive="onTouchEnd"
    tabindex="0"
  >
    <div
      class="slides-container"
      :style="{ '--transition-ms': transitionMs + 'ms' }"
      aria-live="polite"
    >
      <div
        v-for="(slide, index) in slidesSafe"
        :key="slide.id ?? index"
        class="slide"
        :class="{ active: index === currentIndex }"
        :aria-hidden="index !== currentIndex"
        @click="onSlideClick(index)"
      >
        <img
          class="slide-img"
          :src="slide.imageSrc"
          :alt="slide.alt || ''"
          :loading="index === currentIndex ? 'eager' : 'lazy'"
          :fetchpriority="index === currentIndex ? 'high' : 'auto'"
          decoding="async"
          draggable="false"
        />
        <!--
        <div class="slide-caption">
          <h1>{{ slide.title }}</h1>
          <p>{{ slide.description }}</p>
          <button class="cta-button">{{ slide.buttonText }}</button>
        </div>
        -->
      </div>
    </div>

    <button class="carousel-control prev" @click="prevSlide" aria-label="上一張">&lt;</button>
    <button class="carousel-control next" @click="nextSlide" aria-label="下一張">&gt;</button>

    <div class="carousel-dots" role="tablist" :aria-label="`共 ${slidesSafe.length} 張`">
      <button
        v-for="(slide, index) in slidesSafe"
        :key="slide.id ?? index"
        class="dot"
        :class="{ active: index === currentIndex }"
        role="tab"
        :aria-selected="index === currentIndex"
        :aria-controls="`slide-${index}`"
        @click="goToSlide(index)"
      />
    </div>

    <!-- 觸控滑動層（行動裝置） -->
    <!-- <div
      class="touch-layer"
      @touchstart.passive="onTouchStart"
      @touchmove.passive="onTouchMove"
      @touchend.passive="onTouchEnd"
    /> -->
  </div>
</template>

<script setup>
/**
 * HeroCarousel (首頁活動版)
 * - fetchLatestEvents=true：自動抓「最新 N 筆活動」→ 轉為 slides 結構
 * - slides（可選）：外部傳入也會正規化；remoteWins=true 時以遠端覆蓋
 * - 影像策略：base64 > 欄位URL > /events/{id}/image
 * - ARIA/鍵盤/滑動/自動播放：皆已完善
 */
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import eventPublicService from '@/features/Event/services/eventPublicService'
import httpClient from '@/services/api'
const router = useRouter()

// -------------------- Props --------------------
const props = defineProps({
  // 外部可傳自訂 slides；預設空，避免擋到遠端
  slides: { type: Array, default: () => [] },

  // 遠端抓活動
  fetchLatestEvents: { type: Boolean, default: true },
  limit: { type: Number, default: 5 },
  sort: { type: String, default: 'startDate,desc' },
  remoteWins: { type: Boolean, default: true }, // 遠端資料優先

  // 播放設定
  autoplay: { type: Boolean, default: true },
  interval: { type: Number, default: 5000 }, // ms
  pauseOnHover: { type: Boolean, default: true },
  loop: { type: Boolean, default: true },
  transitionMs: { type: Number, default: 800 }, // css 過場時間

  /**
   * 可自訂詳情頁路徑生成（例如改用 /event/:id 或 /events/detail/:id）
   * 預設: id => `/events/${id}`
   */
  detailRouteBuilder: {
    type: Function,
    default: (id) => `/events/${id}`,
  },
})

const emit = defineEmits(['slide-click', 'change'])

// -------------------- Helpers（與活動專區一致） --------------------
const absUrl = (u) => {
  if (!u) return ''
  if (/^https?:\/\//i.test(u)) return u
  if (u.startsWith('//')) return `${location.protocol}${u}`
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  const rel = u.startsWith('/') ? u : `/${u}`
  return `${base}${rel}`
}

const finalImageUrl = (id, base64Str, fieldUrl) => {
  if (base64Str) return `data:image/*;base64,${base64Str}`
  if (fieldUrl) return `${absUrl(String(fieldUrl))}?v=${Date.now()}`
  const base = (httpClient.defaults?.baseURL || '').replace(/\/+$/, '')
  return `${base}/events/${id}/image?v=${Date.now()}`
}

const textOnly = (html) =>
  String(html || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

const toSlide = (e) => {
  const id = String(e.id ?? e.eventId ?? e.event_id)
  const b64 = e.base64ImageString ?? e.base64_image_string ?? null
  const imgField =
    e.imageUrl ||
    e.coverUrl ||
    e.bannerUrl ||
    e.pictureUrl ||
    e.thumbnailUrl ||
    e.image ||
    e.cover ||
    e.banner ||
    e.pic ||
    e.image_path ||
    e.imagePath ||
    ''
  return {
    id,
    imageSrc: finalImageUrl(id, b64, imgField),
    alt: e.title ?? e.name ?? '',
    title: e.title ?? e.name ?? '',
    description: textOnly(e.content ?? e.description ?? ''),
    buttonText: '立即查看',
  }
}

// 外部傳入 slides 的正規化（保證有 id / imageSrc）
const normalizeSlide = (s, i) => {
  if (!s) return null
  const id =
    s.id ??
    s.eventId ??
    s.event_id ??
    // 如果外部真的沒給 id，最後才用索引或從 link 嘗試萃取
    (s.link?.replace?.(/[^\d]+/g, '') || String(i))
  return {
    id: String(id),
    imageSrc:
      s.imageSrc ||
      s.image ||
      s.cover ||
      s.banner ||
      s.imageUrl ||
      // 若最後仍沒有，給個透明 1x1 佔位，避免破圖
      'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==',
    alt: s.alt || s.title || '',
    title: s.title || '',
    description: s.description || '',
    buttonText: s.buttonText || '立即查看',
  }
}

// -------------------- Data --------------------
const internalSlides = ref([]) // 遠端載入後放這裡
const slidesSource = computed(() => {
  // 遠端優先：有抓到資料且 remoteWins=true
  if (props.remoteWins && internalSlides.value.length) return internalSlides.value
  // 否則用外部傳入（也做 normalize）；再不然就用遠端（可能尚未回來）
  const ext = Array.isArray(props.slides) ? props.slides.map(normalizeSlide).filter(Boolean) : []
  return ext.length ? ext : internalSlides.value
})

const slidesSafe = computed(() =>
  Array.isArray(slidesSource.value) ? slidesSource.value.filter(Boolean) : [],
)
const count = computed(() => slidesSafe.value.length)

const currentIndex = ref(0)
let autoplayTimer = null

// -------------------- Fetch latest events --------------------
async function loadLatestEvents() {
  if (!props.fetchLatestEvents) return
  try {
    const res = await eventPublicService.list({
      page: 0,
      size: props.limit,
      sort: props.sort,
    })
    const content = Array.isArray(res?.content) ? res.content : Array.isArray(res) ? res : []
    internalSlides.value = content.map(toSlide)
  } catch (err) {
    console.error('[HeroCarousel] fetch latest events failed:', err)
  }
}

// -------------------- Carousel core --------------------
const normalizeIndex = (idx) => {
  if (count.value === 0) return 0
  if (props.loop) return (idx + count.value) % count.value
  return Math.min(Math.max(idx, 0), count.value - 1)
}

const goToSlide = (index) => {
  const target = normalizeIndex(index)
  if (target !== currentIndex.value) {
    currentIndex.value = target
    emit('change', target)
  }
}
const nextSlide = () => goToSlide(currentIndex.value + 1)
const prevSlide = () => goToSlide(currentIndex.value - 1)

const startAutoplay = () => {
  stopAutoplay()
  if (!props.autoplay || count.value <= 1) return
  autoplayTimer = setInterval(nextSlide, props.interval)
}
const stopAutoplay = () => {
  if (autoplayTimer) {
    clearInterval(autoplayTimer)
    autoplayTimer = null
  }
}

const onVisibilityChange = () => {
  if (document.hidden) stopAutoplay()
  else startAutoplay()
}

// 導頁：支援 prefetch 狀態，以便詳情頁秒顯示
const onSlideClick = (index) => {
  const s = slidesSafe.value[index]
  if (!s) return
  if (s.id) {
    router.push({
      path: props.detailRouteBuilder(s.id),
      state: { prefetch: s },
    })
  }
  emit('slide-click', s)
}

// 觸控滑動
let touchStartX = 0
let touchDeltaX = 0
const SWIPE_THRESHOLD = 40 // px
const onTouchStart = (e) => {
  if (!e.touches || e.touches.length === 0) return
  touchStartX = e.touches[0].clientX
  touchDeltaX = 0
}
const onTouchMove = (e) => {
  if (!e.touches || e.touches.length === 0) return
  touchDeltaX = e.touches[0].clientX - touchStartX
}
const onTouchEnd = () => {
  if (Math.abs(touchDeltaX) > SWIPE_THRESHOLD) {
    touchDeltaX < 0 ? nextSlide() : prevSlide()
  }
  touchStartX = 0
  touchDeltaX = 0
}

// slides 數量變化時維持自動播放狀態
watch(count, (n) => {
  if (n === 0) {
    stopAutoplay()
    currentIndex.value = 0
    return
  }
  currentIndex.value = normalizeIndex(currentIndex.value)
  if (n <= 1) stopAutoplay()
  else if (props.autoplay) startAutoplay()
})

// -------------------- Lifecycle --------------------
onMounted(async () => {
  // 開啟 fetchLatestEvents 時一定會抓，不再受 props.slides 是否為空影響
  await loadLatestEvents()
  if (count.value > 1 && props.autoplay) startAutoplay()
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onBeforeUnmount(() => {
  stopAutoplay()
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<style scoped>
.hero-carousel {
  width: 100%;
  /* 可換成固定高度或利用 aspect-ratio： */
  aspect-ratio: 16 / 9;
  height: auto;
  position: relative;
  overflow: hidden;
  outline: none;
}

/* 幻燈片容器 */
.slides-container {
  width: 100%;
  height: 100%;
  display: block;
  position: relative;
}

/* 單張 slide 疊放+淡入淡出 */
.slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity var(--transition-ms, 800ms) ease-in-out;
  pointer-events: none;
  display: block;
}
.slide.active {
  opacity: 1;
  pointer-events: auto;
}

.slide img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 不裁切，保留完整圖片 */
  object-position: center; /* 置中顯示 */
  display: block;
  background: #000; /* 產生友善的 letterbox 黑邊 */
  cursor: pointer;
  user-select: none;
  -webkit-user-drag: none; /* 避免拖曳 ghost 影像 */
}
.slide {
  cursor: pointer;
}

/* Caption（預設關閉） */
.slide-caption {
  position: absolute;
  inset: auto auto 12% 8%;
  color: #fff;
  max-width: min(560px, 60%);
  z-index: 2;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.55);
}
.slide-caption h1 {
  font-size: clamp(28px, 4vw, 48px);
  margin-bottom: 12px;
}
.slide-caption p {
  font-size: clamp(14px, 2vw, 18px);
  margin-bottom: 20px;
}
.cta-button {
  background-color: #e50914;
  color: white;
  border: none;
  padding: 12px 22px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  border-radius: 8px;
  transition:
    transform 0.2s,
    background-color 0.3s;
}
.cta-button:hover {
  background-color: #f40612;
  transform: translateY(-1px) scale(1.03);
}

/* 控制鈕 */
.carousel-control {
  position: absolute;
  top: 50%;
  translate: 0 -50%;
  z-index: 3;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  font-size: 2rem;
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 999px;
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  transition: background-color 0.3s;
}
.carousel-control:hover {
  background-color: rgba(0, 0, 0, 0.8);
}
.carousel-control.prev {
  left: 20px;
}
.carousel-control.next {
  right: 20px;
}

/* 指示點 */
.carousel-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  translate: -50% 0;
  z-index: 3;
  display: flex;
  gap: 10px;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.55);
  border: none;
  cursor: pointer;
  transition:
    background-color 0.3s,
    transform 0.1s;
}
.dot:hover {
  background-color: rgba(255, 255, 255, 0.85);
  transform: scale(1.08);
}
.dot.active {
  background-color: #fff;
}

/* 觸控層（不影響既有元素點擊） */
.touch-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: transparent;
}
</style>
