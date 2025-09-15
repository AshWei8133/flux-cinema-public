<template>
  <div class="schedule-container" v-loading="isMovieSessionUpdateLoading">
    <div class="timeline-area">
      <MovieTimeline
        v-if="groupedTheatersData.length > 0"
        :scheduleData="scheduleData"
        :isEditable="isEditable"
        :movies="nowPlayingMovies"
        :theatersData="groupedTheatersData"
        :has-changes="hasChanges"
        :selected-date="selectedDate"
        @update-schedule="handleScheduleUpdate"
        @drop-movie="handleMovieDrop"
        @delete-schedule="handleScheduleDelete"
        @toggle-category="handleToggleCategory"
        @save-changes="handleSaveChanges"
        @reset-changes="handleResetChanges"
      />
      <el-empty v-else-if="!isMovieSessionUpdateLoading" description="該日期尚無影廳資料或排程" />
    </div>

    <div class="movie-list-wrapper">
      <div
        v-if="upcomingMovies.length > 0"
        class="movie-list-sidebar"
        :class="{ 'is-open': isUpcomingOpen }"
      >
        <button @click="toggleUpcomingList" class="toggle-btn">待排片</button>

        <div class="movie-list-content-wrapper">
          <div class="header-controls">
            <div class="drag-handle" @mousedown="startDrag($event, 'upcoming')">
              <Icon icon="ic:round-drag-indicator" class="drag-icon" />
            </div>
            <button
              v-show="isUpcomingOpen"
              @click="toggleUpcomingList"
              class="close-btn upcoming-list"
            >
              <Icon icon="zondicons:minus-outline" />
            </button>
          </div>
          <div class="movie-list-content">
            <DraggableMovieList
              v-if="upcomingMovies.length > 0"
              :movies="upcomingMovies"
              title="待排片"
            />
          </div>
        </div>
      </div>

      <div class="movie-list-sidebar" :class="{ 'is-open': isNowPlayingOpen }">
        <button @click="toggleNowPlayingList" class="toggle-btn">現正熱映</button>
        <div class="movie-list-content-wrapper">
          <div class="header-controls">
            <div class="drag-handle" @mousedown="startDrag($event, 'nowPlaying')">
              <Icon icon="ic:round-drag-indicator" class="drag-icon" />
            </div>
            <button
              v-show="isNowPlayingOpen"
              @click="toggleNowPlayingList"
              class="close-btn now-playing-list"
            >
              <Icon icon="zondicons:minus-outline" />
            </button>
          </div>
          <div class="movie-list-content">
            <DraggableMovieList
              v-if="nowPlayingMovies.length > 0"
              :movies="nowPlayingMovies"
              title="現正熱映"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// ----------------------------------------------------------------
// 引入依賴 (Import Dependencies)
// 就像蓋房子前，先把所有需要的工具和材料 (Vue功能、外部元件、狀態管理庫、工具函式) 都準備好。
// ----------------------------------------------------------------
import { ref, onMounted, computed, watch } from 'vue' // 從 Vue 核心引入 Composition API，用於定義響應式狀態、計算屬性等
import { useRoute } from 'vue-router' // 引入 Vue Router 的功能，用來獲取當前路由的資訊 (如此處用來拿日期參數)
import MovieTimeline from '../components/MovieTimeline.vue' // 引入核心的「電影時間軸」子元件
import DraggableMovieList from '../components/DraggableMovieList.vue' // 引入「可拖曳的電影清單」子元件
import { Icon } from '@iconify/vue' // 引入圖示庫元件，方便使用各種 Icon
import { useTheaterStore } from '@/features/Theaters/store/useTheaterStore' // 引入影廳的 Pinia store
import { storeToRefs } from 'pinia' // 引入 Pinia 的輔助函式，可以將 store 中的 state 轉為 ref，同時保持響應性
import { useMovieStore } from '@/features/Movies/store/useMovieStore' // 引入電影的 Pinia store
import { useMovieSessionStore } from '../store/useMovieSessionStore' // 引入電影場次的 Pinia store
import { toLocalISOString } from '@/utils/dateUtils.js' // 引入日期處理的工具函式，用來格式化時間
import { ElMessage } from 'element-plus' // 引入 Element Plus 的訊息提示元件

// ----------------------------------------------------------------
// 核心狀態定義 (Component State)
// ----------------------------------------------------------------

// 獲取當前路由實例
const route = useRoute()
// 定義 `selectedDate`，並從路由參數中初始化它的值。如果路由沒有提供日期，就使用今天的日期作為預設值。
const selectedDate = ref(route.params.date || new Date().toISOString().slice(0, 10))

// --- 連接 Pinia Store ---

// 實例化影廳 store
const theaterStore = useTheaterStore()
// 使用 storeToRefs 將 store 中的 `theaters` 狀態解構出來，使其成為一個響應式的 ref
const { theaters } = storeToRefs(theaterStore)

// 實例化電影 store
const movieStore = useMovieStore()
// 從電影 store 中解構出 `nowPlayingMoviesFromAPI`
const { nowPlayingMoviesFromAPI } = storeToRefs(movieStore)

// 實例化電影場次 store
const movieSessionStore = useMovieSessionStore()
// 從場次 store 中解構出場次資料 `scheduleData` 和載入狀態 `isMovieSessionUpdateLoading`
const { scheduleData, isMovieSessionUpdateLoading } = storeToRefs(movieSessionStore)

// --- 本地元件狀態 (Local Component State) ---

// `expandedCategories`：用一個 Set 來儲存當前哪些影廳類別是處於「展開」狀態的。
const expandedCategories = ref(new Set())
// `originalScheduleData`：用來備份從伺服器載入的「最原始」的排程資料。
// 實現「重置」和「偵測變更」功能的關鍵
const originalScheduleData = ref([])

// 「現正熱映」列表的電影資料。這裡直接使用從 store 來的資料。
const nowPlayingMovies = computed(() => nowPlayingMoviesFromAPI.value)

// 「待排片」列表的電影資料。它的內容是動態計算出來的。
const upcomingMovies = computed(() => {
  // 如果根本沒有任何電影，直接回傳空陣列
  if (nowPlayingMoviesFromAPI.value.length === 0) {
    return []
  }
  // 如果目前排程是空的，那表示所有「現正熱映」的電影都算是「待排片」
  if (scheduleData.value.length === 0) {
    return nowPlayingMoviesFromAPI.value
  }

  // 1. 建立一個 Set，包含所有「已經被排程」的電影 ID。
  const scheduledMovieIds = new Set(scheduleData.value.map((session) => session.movieId))

  // 2. 過濾「所有現正熱映的電影」，只留下那些 ID 不在 `scheduledMovieIds` 這個 Set 裡的電影。
  return nowPlayingMoviesFromAPI.value.filter((movie) => !scheduledMovieIds.has(movie.id))
})

// ----------------------------------------------------------------
// UI 互動狀態定義 (UI Interaction State)
// 這裡定義了控制 UI 動畫、開合、拖曳等視覺效果的狀態。
// ----------------------------------------------------------------
const isNowPlayingOpen = ref(false) // 「現正熱映」列表是否展開
const isUpcomingOpen = ref(false) // 「待排片」列表是否展開
const isDragging = ref(false) // 是否正在拖曳側邊欄
const dragStartX = ref(0) // 拖曳開始時的滑鼠 X 座標
const currentTranslateX = ref(0) // 側邊欄當前 X 軸的位移量
const targetListRef = ref(null) // 正在被拖曳的 DOM 元素
const animationFrameId = ref(null) // 用於 requestAnimationFrame 的 ID，優化拖曳效能

// ----------------------------------------------------------------
// 監聽器 (Watchers)
// ----------------------------------------------------------------
/**
 * 監聽 `selectedDate` 這個 ref 的變化。
 * 當使用者透過日期選擇器等方式改變了日期，這個函式就會被觸發。
 * @param {string} newDate - 新的日期字串
 * @param {string} oldDate - 舊的日期字串
 */
watch(selectedDate, (newDate, oldDate) => {
  // 只有當新日期存在且不等於舊日期時，才執行載入資料的動作，避免不必要的重複載入
  if (newDate && newDate !== oldDate) {
    // console.log(`日期變更為 ${newDate}，重新載入資料...`)
    loadData(newDate)
  }
})

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// 根據現有的狀態，計算衍生出新的狀態。它們會自動緩存，只有在依賴的資料改變時才會重新計算。
// ----------------------------------------------------------------

/**
 * 計算屬性：`hasChanges`
 * 用來判斷目前的排程資料 (`scheduleData`) 是否與最原始的資料 (`originalScheduleData`) 有所不同。
 * 判斷是否顯示「儲存變更」和「重製變更」按鈕
 * @returns {boolean} 如果有變更則返回 true，否則返回 false。
 */
const hasChanges = computed(() => {
  // 因為直接比較兩個物件或陣列 (`originalScheduleData.value !== scheduleData.value`) 只會比較它們的記憶體位置，而不是內容。
  // 將它們都轉換成 JSON 字串後再比較，就能有效地「深層比較 (deep compare)」兩個複雜資料結構的內容是否完全一致。
  return JSON.stringify(originalScheduleData.value) !== JSON.stringify(scheduleData.value)
})

/**
 * 計算屬性：`isEditable`
 * 根據當前的路由名稱，判斷頁面是否處於「可編輯」模式。不然即為歷史紀錄(不可編輯)
 * @returns {boolean}
 */
const isEditable = computed(() => {
  return route.name === 'movieScheduleEditor'
})

/**
 * 計算屬性：`groupedTheatersData`
 * 將從 API 獲取的扁平化影廳列表 (`theaters`)，轉換成按「影廳類型」分組的巢狀結構。
 * @returns {Array} 分組後的影廳資料陣列。
 */
const groupedTheatersData = computed(() => {
  // 如果沒有影廳資料，直接回傳空陣列
  if (!theaters.value || theaters.value.length === 0) {
    return []
  }
  // 1. 找出所有不重複的「影廳類型」
  const typeMap = new Map()
  theaters.value.forEach((theater) => {
    // 利用 Map 的 key 唯一性來去重
    if (!typeMap.has(theater.theaterTypeId)) {
      typeMap.set(theater.theaterTypeId, {
        theaterTypeId: theater.theaterTypeId,
        theaterTypeName: theater.theaterTypeName,
      })
    }
  })
  const localTheaterTypes = Array.from(typeMap.values()) // 將 Map 的值轉為陣列

  // 2. 根據找出的類型，建立分組結構
  return (
    localTheaterTypes
      .map((category) => {
        // 為每個類別，從原始列表中篩選出屬於該類別的所有影廳
        const theatersInGroup = theaters.value.filter(
          (theater) => theater.theaterTypeId === category.theaterTypeId,
        )
        // 組合成 MovieTimeline 需要的格式
        return {
          id: category.theaterTypeId,
          name: category.theaterTypeName,
          // 檢查這個類別是否在 `expandedCategories` 中，決定其初始是否為展開狀態
          isExpanded: expandedCategories.value.has(category.theaterTypeId),
          theaters: theatersInGroup.map((th) => ({
            ...th,
            id: th.theaterId, // 為了與元件期望的 prop 名稱一致，做個轉換
            name: th.theaterName,
          })),
        }
      })
      // 3. 過濾掉那些沒有任何影廳的分組 (雖然正常情況下不該發生，但這是一種防禦性程式設計)
      .filter((group) => group.theaters.length > 0)
  )
})

// ----------------------------------------------------------------
// 方法 (Methods)
// 這裡定義了所有會被事件觸發 (如點擊、拖曳) 或在邏輯中呼叫的函式。
// ----------------------------------------------------------------

// --- UI 互動方法 ---

/**
 * 處理影廳類別的展開/收合
 * @param {number|string} categoryId - 被點擊的影廳類別 ID
 */
const handleToggleCategory = (categoryId) => {
  if (expandedCategories.value.has(categoryId)) {
    // 如果已經是展開狀態，就從 Set 中移除它 (收合)
    expandedCategories.value.delete(categoryId)
  } else {
    // 如果是收合狀態，就將它加入 Set (展開)
    expandedCategories.value.add(categoryId)
  }
}
/**
 * 切換「現正熱映」列表的展開/收合狀態
 */
const toggleNowPlayingList = () => {
  isNowPlayingOpen.value = !isNowPlayingOpen.value
}
/**
 * 切換「待排片」列表的展開/收合狀態
 */
const toggleUpcomingList = () => {
  isUpcomingOpen.value = !isUpcomingOpen.value
}
/**
 * 開始拖曳電影列表
 * @param {MouseEvent} e - 滑鼠事件對象
 * @param {string} listType - 識別是哪個列表 ('upcoming' or 'nowPlaying')
 */
const startDrag = (e, listType) => {
  e.preventDefault() // 防止預設行為 (如選取文字)
  isDragging.value = true
  dragStartX.value = e.clientX // 記錄滑鼠起始位置
  // 找到被拖曳的 `.movie-list-sidebar` DOM 元素
  targetListRef.value = e.target.closest('.movie-list-sidebar')
  // 在整個 document 上註冊監聽器，這樣滑鼠移出元素外也能繼續拖曳
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', dragEnd)
}
/**
 * 處理滑鼠移動事件 (throttled by requestAnimationFrame)
 * @param {MouseEvent} e - 滑鼠事件對象
 */
const handleMouseMove = (e) => {
  // 使用 requestAnimationFrame 來優化拖曳效能
  // 它可以確保我們的拖曳計算只在瀏覽器下一次重繪之前執行，避免不必要的計算和畫面抖動
  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrameId.value)
  }
  animationFrameId.value = requestAnimationFrame(() => {
    dragMove(e)
  })
}
/**
 * 實際計算並應用拖曳位移
 * @param {MouseEvent} e - 滑鼠事件對象
 */
const dragMove = (e) => {
  if (!isDragging.value || !targetListRef.value) return
  const dx = e.clientX - dragStartX.value // 計算滑鼠 X 軸的移動量
  currentTranslateX.value += dx // 累加到總位移量上
  // 直接操作 DOM 的 style 來移動元素，效能比改變 Vue 的 data 好
  targetListRef.value.style.transform = `translateX(${currentTranslateX.value}px)`
  dragStartX.value = e.clientX // 更新起始位置為當前位置，為下一次移動計算做準備
}
/**
 * 結束拖曳
 */
const dragEnd = () => {
  isDragging.value = false
  targetListRef.value = null // 清除目標元素
  // 移除 document 上的監聽器，釋放資源
  document.removeEventListener('mousemove', handleMouseMove)
  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrameId.value)
    animationFrameId.value = null
  }
}

/**
 * 載入頁面所需的所有初始資料。
 * 這是一個核心的非同步函式，會一次性地發出多個 API 請求。
 * @param {string} dateString - 要載入資料的日期
 */
const loadData = async (dateString) => {
  // 使用 Promise.all 可以並行發出所有 API 請求，而不是一個接一個地等待。
  // 這能顯著縮短頁面的總體載入時間。
  await Promise.all([
    theaterStore.fetchAllTheaters(), // 獲取所有影廳資料
    movieStore.getNowPlayingMoviesByDate(dateString), // 獲取指定日期的電影資料
    movieSessionStore.getSessionsByDate(dateString), // 獲取指定日期的排程資料
  ])

  // 當所有資料都成功回來後，進行一次「深拷貝」，將初始的排程資料備份起來。
  // 這是為了後續能比較使用者是否做了修改。
  originalScheduleData.value = JSON.parse(JSON.stringify(scheduleData.value))
  // console.log('初始排程資料已備份', originalScheduleData.value)

  // 預設將所有影廳類別都設為展開狀態，提升使用者初次載入的體驗
  if (theaters.value.length > 0) {
    const typeMap = new Map()
    theaters.value.forEach((theater) => {
      if (!typeMap.has(theater.theaterTypeId)) {
        typeMap.set(theater.theaterTypeId, { theaterTypeId: theater.theaterTypeId })
      }
    })
    const localTheaterTypes = Array.from(typeMap.values())
    expandedCategories.value = new Set(localTheaterTypes.map((c) => c.theaterTypeId))
  }
}

// --- 子元件事件處理 (Event Handlers from Child Components) ---

/**
 * 處理來自 MovieTimeline 的排程更新事件。
 * 當使用者在時間軸上拖曳或調整了某個排程的時間/影廳時觸發。
 * @param {Array} updatedSchedule - 更新後的完整排程資料陣列
 */
const handleScheduleUpdate = (updatedSchedule) => {
  // 只有在可編輯模式下才允許更新
  if (isEditable.value) {
    // console.log('排片更新：', updatedSchedule)
    // 直接用子元件傳來的新資料覆蓋本地的 scheduleData
    scheduleData.value = updatedSchedule
  }
}

/**
 * 處理從電影列表拖曳到時間軸的事件。
 * @param {object} droppedData - 包含電影ID、影廳ID和放置時間的物件
 */
const handleMovieDrop = (droppedData) => {
  if (isEditable.value) {
    const { movieId, theaterId, dropTime } = droppedData
    // 根據 movieId 找到完整的電影資料
    const droppedMovie = nowPlayingMovies.value.find((m) => m.id === movieId)

    if (droppedMovie) {
      // 計算開始和結束時間
      const startTime = new Date(dropTime)
      const endTime = new Date(startTime.getTime() + droppedMovie.durationMinutes * 60 * 1000)

      // 建立一個新的排程物件
      const newSchedule = {
        sessionId: Date.now(), // 暫時用時間戳作為臨時 ID，後端儲存時會生成真正的 ID
        theaterId: theaterId,
        movieId: movieId,
        // 使用 toLocalISOString 確保時間格式是 'YYYY-MM-DDTHH:mm:ss'，與後端期望的格式一致，避免時區問題
        startTime: toLocalISOString(startTime),
        endTime: toLocalISOString(endTime),
      }
      // 將新排程加入到 scheduleData 陣列中，畫面會自動響應更新
      scheduleData.value.push(newSchedule)
    }
  }
}

/**
 * 處理刪除排程的事件。
 * @param {object} scheduleToDelete - 要被刪除的排程物件
 */
const handleScheduleDelete = (scheduleToDelete) => {
  if (!isEditable.value) return
  // 使用 filter 方法，回傳一個不包含被刪除項目的新陣列，並賦值給 scheduleData
  scheduleData.value = scheduleData.value.filter((s) => s.sessionId !== scheduleToDelete.sessionId)
}

/**
 * 處理「儲存變更」的事件。
 * 會呼叫 store 中的 action 將目前的排程資料送到後端。
 */
const handleSaveChanges = async () => {
  try {
    // 呼叫 store 的 action 來儲存資料
    // 因為排片後，「待排片」列表可能會改變，所以需要重新獲取電影資料來刷新 UI
    await movieStore.getNowPlayingMoviesByDate(selectedDate.value)
    await movieSessionStore.saveSchedules(selectedDate.value, scheduleData.value)

    // 儲存成功後，store 內部會自動重新獲取最新的排程資料並更新 `scheduleData`。
    // 此時，我們需要手動將本地的備份資料 `originalScheduleData` 也更新成最新狀態，
    // 這樣 `hasChanges` 計算屬性才會變回 false，UI 上的「儲存」按鈕才會變為禁用狀態。
    originalScheduleData.value = JSON.parse(JSON.stringify(scheduleData.value))

    // 顯示成功訊息
    ElMessage.success('排程儲存成功！')
  } catch (error) {
    // 錯誤訊息通常由 axios 的攔截器統一處理並彈出提示，
    // 所以這裡通常不需要再次用 ElMessage 提示使用者。
    // 但在 console 中印出錯誤有助於開發時除錯。
    console.error('儲存操作失敗:', error)
  }
}

/**
 * 處理「重置變更」的事件。
 * 放棄所有未儲存的修改，將排程恢復到剛載入時的狀態。
 */
const handleResetChanges = () => {
  // 將之前備份的原始資料，透過深拷貝還原給 `scheduleData`。
  scheduleData.value = JSON.parse(JSON.stringify(originalScheduleData.value))
  ElMessage.info('變更已重置')
}

// --- 生命週期掛鉤 (Lifecycle Hooks) ---
/**
 * `onMounted` 會在元件被掛載到 DOM 上之後執行。
 * 這是發起初始資料請求的最佳時機。
 */
onMounted(() => {
  // 載入目前 `selectedDate` 的所有相關資料
  loadData(selectedDate.value)
})
</script>

<style scoped>
/* 整個排片編輯的最外層容器 */
.schedule-container {
  position: relative; /* 作為內部絕對定位元素的基準 */
  width: 100%;
  min-height: 100%; /* 確保容器至少和視窗一樣高 */
  padding: 20px;
  box-sizing: border-box; /* 讓 padding 不會撐大容器的寬度 */
}

/* 排片時間軸元件外層容器 */
.timeline-area {
  width: 100%;
  height: 100%;
}

/* 電影列表的外部容器 */
.movie-list-wrapper {
  position: fixed; /* 固定在畫面上，不隨頁面滾動 */
  bottom: 70px; /* 距離底部 70px */
  right: 10px; /* 距離右側 10px */
  z-index: 100; /* 確保在其他內容之上 */
  display: flex;
  gap: 10px; /* 兩個列表之間的間距 */
  align-items: flex-end; /* 讓列表從底部對齊 */
  pointer-events: none; /* 關鍵！讓這個大的透明容器本身不接收滑鼠事件，這樣才能點擊到它下方的內容。事件會穿透過去。 */
}

/* 單個電影列表側邊欄的樣式 */
.movie-list-sidebar {
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 添加陰影使其有立體感 */
  width: 120px; /* 收合時的寬度 */
  max-height: 40px; /* 收合時的高度，剛好等於按鈕高度 */
  overflow: hidden; /* 隱藏超出範圍的內容 */
  transition: all 0.3s ease-in-out; /* 為所有屬性變化添加平滑的過渡動畫 */
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 101;
  pointer-events: auto; /* 覆寫父層的 none，讓側邊欄本身可以接收滑鼠事件 */
}

/* 當側邊欄有 'is-open' class 時的樣式 (展開狀態) */
.movie-list-sidebar.is-open {
  width: 230px; /* 展開時的寬度 */
  max-height: 500px; /* 展開時的最大高度 */
}

.movie-list-content-wrapper {
  height: 100%; /* 內容區撐滿側邊欄 */
}

/* 側邊欄頂部的控制列 */
.header-controls {
  display: flex;
  justify-content: space-between; /* 讓拖曳手柄和關閉按鈕分居兩側 */
  align-items: center;
  width: 100%;
  height: 40px;
  padding: 0 5px;
  box-sizing: border-box;
  background-color: #fff;
  border-bottom: 1px solid #eee; /* 分隔線 */
}

/* 真正放置電影列表的內容區 */
.movie-list-content {
  /* 高度是父容器的100%減去頭部控制列的40px */
  height: calc(100% - 40px);
}

/* 當側邊欄不是展開狀態時，隱藏內容區 */
.movie-list-sidebar:not(.is-open) .movie-list-content-wrapper {
  display: none;
}

/* 收合/展開的切換按鈕 */
.toggle-btn {
  flex-shrink: 0; /* 防止按鈕在 flex 佈局中被壓縮 */
  width: 100%;
  height: 40px;
  padding: 10px;
  text-align: center;
  border: none;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  z-index: 102; /* 確保在內容之上 */
  background-color: #fff;
  border-top: 1px solid #ccc; /* 在收合狀態時，這條線看起來像列表的頂部邊框 */
}

/* 關閉按鈕 */
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #121030;
  z-index: 11;
  opacity: 0.8;
  transition: opacity 0.3s ease;
  padding: 5px;
}

/* 拖曳手柄 */
.drag-handle {
  width: 24px;
  height: 24px;
  cursor: grab; /* 顯示可抓取的手形游標 */
  flex-shrink: 0;
  z-index: 12;
}

.drag-handle:hover {
  opacity: 1; /* 滑鼠懸停時更明顯 */
}
</style>
