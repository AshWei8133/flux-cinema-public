<template>
  <div class="booking-form-card">
    <h3>快速訂票</h3>
    <form @submit.prevent>
      <div class="form-group">
        <Icon icon="mdi:ticket-confirmation-outline" class="input-icon" />
        <div class="select-wrapper">
          <select
            id="movie"
            v-model="booking.movieId"
            required
            :class="{ 'placeholder-style': !booking.movieId }"
          >
            <option disabled value="">請選擇想看的電影</option>
            <option v-for="movie in nowPlayingMovies" :key="movie.id" :value="movie.id">
              {{ movie.titleLocal }}
            </option>
          </select>
          <Icon
            v-if="booking.movieId"
            icon="mdi:close-circle"
            class="clear-button-overlay"
            @click="clearSelection('movieId')"
          />
        </div>
      </div>

      <div class="form-group">
        <Icon icon="ph:projector-screen-chart-bold" class="input-icon" />
        <div class="select-wrapper">
          <select
            id="version"
            v-model="booking.version"
            required
            :class="{ 'placeholder-style': !booking.version }"
          >
            <option disabled value="">
              {{ booking.movieId ? '請選擇電影版本' : '↑ 請先選擇電影' }}
            </option>
            <option v-for="version in availableVersions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
          <Icon
            v-if="booking.version"
            icon="mdi:close-circle"
            class="clear-button-overlay"
            @click="clearSelection('version')"
          />
        </div>
      </div>

      <div class="form-group">
        <Icon icon="mdi:calendar-month-outline" class="input-icon" />
        <div class="select-wrapper">
          <select
            id="date"
            v-model="booking.date"
            required
            :class="{ 'placeholder-style': !booking.date }"
          >
            <option disabled value="">
              {{ booking.version ? '請選擇觀影日期' : '↑ 請先選擇版本' }}
            </option>
            <option v-for="date in availableDates" :key="date.value" :value="date.value">
              {{ date.text }}
            </option>
          </select>
          <Icon
            v-if="booking.date"
            icon="mdi:close-circle"
            class="clear-button-overlay"
            @click="clearSelection('date')"
          />
        </div>
      </div>

      <div class="form-group">
        <Icon icon="mdi:clock-time-four-outline" class="input-icon" />
        <div class="select-wrapper">
          <select
            id="time"
            v-model="booking.time"
            required
            :class="{ 'placeholder-style': !booking.time }"
          >
            <option disabled value="">
              {{ booking.date ? '請選擇觀影時間' : '↑ 請先選擇日期' }}
            </option>
            <option v-for="time in availableTimes" :key="time" :value="time">
              {{ time }}
            </option>
          </select>
          <Icon
            v-if="booking.time"
            icon="mdi:close-circle"
            class="clear-button-overlay"
            @click="clearSelection('time')"
          />
        </div>
      </div>

      <div class="button-group">
        <button type="button" class="btn btn-secondary" @click="viewSeats">查看座位</button>
        <button type="button" class="btn btn-primary" @click="goToBooking">前往訂票</button>
      </div>
    </form>
  </div>
</template>

<script setup>
// ----------------------------------------------------------------
// 引入依賴 (Dependencies)
// ----------------------------------------------------------------
import { Icon } from '@iconify/vue'
import { reactive, computed, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { usePublicMovieStore } from '@/features/Movies/store/usePublicMovieStore'
import { usePublicMovieSessionStore } from '@/features/MovieSessions/store/usePublicMovieSessionStore'
import { formatDateForBooking, formatTimeForBooking } from '@/utils/dateUtils'
import { useRouter } from 'vue-router'

const router = useRouter()
// ----------------------------------------------------------------
// Store 狀態管理 (Pinia State Management)
// ----------------------------------------------------------------

// 電影列表 Store：用於獲取所有「現正熱映」的電影列表
const movieStore = usePublicMovieStore()
const { nowPlayingMovies } = storeToRefs(movieStore) // 使用 storeToRefs 維持響應性
const { fetchNowPlayingMovies } = movieStore // 獲取 action

// 電影場次 Store：用於獲取「單一電影」的所有場次資料
const sessionStore = usePublicMovieSessionStore()
const { currentMovieShowtimes } = storeToRefs(sessionStore) // 獲取場次資料狀態
const { fetchShowtimesByMovieId, clearShowtimes, getSessionByCriteria } = sessionStore // 獲取 action

// ----------------------------------------------------------------
// 組件本地狀態 (Local Component State)
// ----------------------------------------------------------------

// 使用 reactive 創建一個物件，來存放使用者在表單中選擇的所有資料
const booking = reactive({
  movieId: '', // 已選電影 ID
  version: '', // 已選版本
  date: '', // 已選日期 (YYYY-MM-DD)
  time: '', // 已選時間 (HH:mm)
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 清除指定欄位的選取值。
 * @param {string} fieldName - 要清除的 booking 物件中的屬性名稱 (例如 'movieId', 'version')
 */
function clearSelection(fieldName) {
  booking[fieldName] = ''
  // 後續的連鎖重置將由 watch 自動處理
}

// ----------------------------------------------------------------
// 監聽器 (Watchers) - 這是實現連鎖反應的核心
// ----------------------------------------------------------------

/**
 * 監聽 `booking.movieId` 的變化。
 * 這是整個連鎖反應的起點。
 */
watch(
  () => booking.movieId,
  async (newMovieId) => {
    // 無論是選擇新電影，還是清除電影，都先重置所有下游選項
    booking.version = ''
    booking.date = ''
    booking.time = ''

    if (newMovieId) {
      // 如果選擇了一個新的電影 ID，就呼叫 Store 的 action 來獲取該電影的場次
      await fetchShowtimesByMovieId(newMovieId)
    } else {
      // 如果是清除了電影選項 (newMovieId 為空)，就呼叫 Store 的 action 來清空場次資料
      clearShowtimes()
    }
  },
)

/**
 * 監聽 `booking.version` 的變化。
 * 當版本改變或被清空時，重置後續的日期和時間選項。
 */
watch(
  () => booking.version,
  () => {
    booking.date = ''
    booking.time = ''
  },
)

/**
 * 監聽 `booking.date` 的變化。
 * 當日期改變或被清空時，重置後續的時間選項。
 */
watch(
  () => booking.date,
  () => {
    booking.time = ''
  },
)

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------

/**
 * 當組件掛載到畫面上時執行。
 * 主要用於獲取初始資料。
 */
onMounted(() => {
  // 為了避免重複請求，先檢查 Store 中是否已經有電影列表資料
  if (nowPlayingMovies.value.length === 0) {
    // 如果沒有，才呼叫 action 去後端獲取
    fetchNowPlayingMovies()
  }
})

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties) - 用於動態生成下拉選單的選項
// ----------------------------------------------------------------

/**
 * 計算可用的電影版本。
 * 依賴 `currentMovieShowtimes` 狀態。
 */
const availableVersions = computed(() => {
  if (!currentMovieShowtimes.value || currentMovieShowtimes.value.length === 0) return []
  // 1. 從所有場次中，提取出所有的版本名稱
  const versions = currentMovieShowtimes.value.map((s) => s.theater.theaterType.theaterTypeName)
  // 2. 使用 Set 來去除重複的版本，然後轉回陣列
  return [...new Set(versions)]
})

/**
 * 計算可用的日期。
 * 依賴 `booking.version` 和 `currentMovieShowtimes` 狀態。
 */
const availableDates = computed(() => {
  if (!booking.version || !currentMovieShowtimes.value) return []

  // 1. 先根據使用者選擇的版本，篩選出場次
  const filteredByVersion = currentMovieShowtimes.value.filter(
    (s) => s.theater.theaterType.theaterTypeName === booking.version,
  )

  // 2. 遍歷篩選後的場次，提取出不重複的日期
  const dateMap = new Map() // 使用 Map 來自動處理重複的日期
  filteredByVersion.forEach((s) => {
    // 3. 呼叫 dateUtils 中的工具函式來進行格式化
    const formattedDate = formatDateForBooking(s.startTime)
    // 4. 如果日期有效且尚未存在於 Map 中，則加入
    if (formattedDate && !dateMap.has(formattedDate.value)) {
      dateMap.set(formattedDate.value, formattedDate)
    }
  })

  // 5. 將 Map 中的值轉為陣列並排序後回傳
  return Array.from(dateMap.values()).sort((a, b) => a.value.localeCompare(b.value))
})

/**
 * 計算可用的時間。
 * 依賴 `booking.version`、`booking.date` 和 `currentMovieShowtimes` 狀態。
 */
const availableTimes = computed(() => {
  if (!booking.version || !booking.date || !currentMovieShowtimes.value) return []

  const filtered = currentMovieShowtimes.value.filter((s) => {
    // 用營業日來比較
    // 直接從 formatDateForBooking 取得場次所屬的營業日字串
    const sessionBusinessDate = formatDateForBooking(s.startTime)?.value
    // 與使用者選擇的日期 (本身就是營業日) 進行比較
    return (
      s.theater.theaterType.theaterTypeName === booking.version &&
      sessionBusinessDate === booking.date
    )
  })

  return (
    filtered
      .map((s) => {
        // 呼叫 formatTimeForBooking，自動處理 (隔日) 標示
        return formatTimeForBooking(s.startTime)
      })
      // 這裡的 sort 可能需要特殊處理，因為 "(隔日)" 會影響字串排序
      // 我們可以先排序再格式化
      .sort((a, b) => {
        // 先將時間字串轉換回數字比較，確保排序正確
        const timeA = parseInt(a.replace(/[^0-9]/g, ''))
        const timeB = parseInt(b.replace(/[^0-9]/g, ''))
        return timeA - timeB
      })
  )
})

// ----------------------------------------------------------------
// 表單提交方法 (Form Submission Methods)
// ----------------------------------------------------------------

/**
 * 驗證所有欄位是否都已填寫。
 */
function validateForm() {
  if (!booking.movieId || !booking.version || !booking.date || !booking.time) {
    alert('請填寫所有訂票資訊！')
    return false
  }
  return true
}

/**
 * 前往訂票按鈕的處理函式。
 * 驗證表單，查找對應的場次 ID，然後導航到訂票流程的第一步。
 */
function goToBooking() {
  // 1. 驗證表單是否填寫完整
  if (!validateForm()) return

  // 2. 使用 Pinia Store 的 getter 根據使用者選擇的條件查找場次
  const targetSession = getSessionByCriteria(booking)

  // 3. 檢查是否找到了場次
  if (targetSession) {
    // 4. 如果找到，就使用 Vue Router 進行頁面跳轉
    console.log(`找到了對應的場次 ID: ${targetSession.sessionId}，準備進入訂票流程...`)
    router.push({
      name: 'BookingPaymentMethod', // 跳轉到訂票流程的第一步 (付款方式選擇)
      params: { sessionId: targetSession.sessionId }, // 將場次 ID 作為路由參數傳遞
    })
  } else {
    // 5. 如果找不到，給予使用者提示
    alert('無法找到對應的場次資訊，請稍後再試。')
    console.error('在 Store 中找不到符合以下條件的場次:', booking)
  }
}

/**
 * 查看座位按鈕的處理函式。
 * 未來會跳轉到僅顯示座位圖的頁面。
 */
function viewSeats() {
  if (!validateForm()) return

  // 1. 使用 Store 提供的 getter 來查找符合條件的場次
  //    我們將使用者在 booking 物件中選擇的值作為篩選條件傳入
  const targetSession = getSessionByCriteria(booking)

  // 2. 檢查是否找到了對應的場次
  if (targetSession) {
    // 如果找到了，就使用它的 sessionId 進行跳轉
    console.log(`找到了對應的場次 ID: ${targetSession.sessionId}`)
    router.push({
      name: 'SeatSelectionPage',
      params: { sessionId: targetSession.sessionId },
    })
  } else {
    // 如果因為某些原因沒找到（例如資料延遲），給予使用者提示
    alert('無法找到對應的場次資訊，請稍後再試。')
    console.error('在 Store 中找不到符合以下條件的場次:', booking)
  }
}
</script>

<style scoped>
/* 整個卡片的基礎樣式 */
.booking-form-card {
  background-color: #2a2a2a; /* 深灰色背景 */
  padding: 30px; /* 內部間距 */
  border-radius: 8px; /* 圓角 */
}

/* "快速訂票" 標題樣式 */
h3 {
  font-size: 24px;
  margin-top: 0; /* 移除頂部外距 */
  margin-bottom: 20px; /* 與下方表單的間距 */
  border-left: 4px solid #e50914; /* 左側的紅色裝飾線 */
  padding-left: 15px; /* 讓文字與裝飾線保持距離 */
  color: #fff; /* 文字顏色 */
}

/* 每個輸入項 (Icon + Select) 的容器 */
.form-group {
  margin-bottom: 20px; /* 每個群組之間的垂直間距 */
  display: flex; /* 使用 Flexbox 佈局 */
  align-items: center; /* 讓內部的 Icon 和 select-wrapper 垂直置中對齊 */
  gap: 15px; /* Icon 和 select-wrapper 之間的水平間距 */
}

/* 左側輸入框圖示的樣式 */
.input-icon {
  font-size: 24px; /* 圖示大小 */
  color: #ccc; /* 圖示顏色 */
  flex-shrink: 0; /* 防止圖示在空間不足時被壓縮 */
}

/* 包裹 select 和清除按鈕的容器 */
.select-wrapper {
  flex-grow: 1; /* 讓此容器填滿 form-group 中剩餘的所有空間 */
  position: relative; /* 設為相對定位，作為內部絕對定位元素(清除按鈕)的基準點 */
  min-width: 0; /* 處理 flexbox 中內容溢出的關鍵，允許容器被壓縮 */
}

/* 下拉選單 (select) 的主要樣式 */
.form-group select {
  width: 100%; /* 寬度填滿父容器 .select-wrapper */
  padding: 12px; /* 內部文字的上下左右間距 */
  padding-right: 40px; /* 右側特別加寬，為清除按鈕騰出空間 */
  background-color: #333; /* 背景色 */
  border: 1px solid #555; /* 邊框 */
  border-radius: 4px; /* 圓角 */
  color: #fff; /* 文字顏色 */
  font-size: 16px; /* 字體大小 */
  box-sizing: border-box; /* 確保 padding 不會影響整體寬度計算 */

  /* 隱藏瀏覽器原生的下拉箭頭，以便我們自訂 */
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;

  /* 處理長文字截斷三件套 */
  white-space: nowrap; /* 1. 強制文字不換行 */
  overflow: hidden; /* 2. 隱藏超出範圍的文字 */
  text-overflow: ellipsis; /* 3. 在結尾顯示省略號 */
}

/* 浮動在 select 上方的清除按鈕 */
.clear-button-overlay {
  position: absolute; /* 設定為絕對定位，相對於 .select-wrapper */
  top: 50%; /* 垂直位置在容器中間 */
  transform: translateY(-50%); /* 透過 transform 精確校正垂直置中 */
  right: 12px; /* 距離右側邊界 12px */

  font-size: 20px;
  color: #888; /* 預設顏色 */
  cursor: pointer; /* 滑鼠移上去時顯示為手形 */
  transition: color 0.3s ease; /* 顏色變化時有 0.3 秒的過渡動畫 */
  background-color: #333; /* 加上與 select 相同的背景色，避免 select 中的文字透出來 */
}

/* 清除按鈕滑鼠懸停時的樣式 */
.clear-button-overlay:hover {
  color: #f40612; /* 顏色變為更亮的紅色 */
}

/* 當 select 未選擇任何有效值時的 placeholder 樣式 */
select.placeholder-style {
  color: #888; /* 文字顏色設為淡灰色 */
}

/* 下拉選單展開時，每個選項 (option) 的樣式 */
select option {
  color: #fff; /* 確保選項文字是白色 */
  /* 同樣加上文字截斷，讓選單內的長選項也能正常顯示 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 包裹兩個主要操作按鈕的容器 */
.button-group {
  display: flex;
  gap: 15px; /* 按鈕之間的間距 */
  margin-top: 30px; /* 與上方表單的間距 */
}

/* 基礎按鈕樣式 */
.btn {
  width: 100%; /* 初始寬度 */
  flex-grow: 1; /* 讓兩個按鈕平均分配 button-group 的寬度 */
  padding: 12px;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s; /* 所有屬性變化都有過渡效果 */
}

/* 主要按鈕 (前往訂票) */
.btn-primary {
  background-color: #e50914; /* 主題紅色 */
}

.btn-primary:hover {
  background-color: #f40612; /* 懸停時更亮的紅色 */
  transform: translateY(-2px); /* 懸停時輕微向上移動，增加立體感 */
}

/* 次要按鈕 (查看座位) */
.btn-secondary {
  background-color: #555; /* 深灰色 */
}

.btn-secondary:hover {
  background-color: #666; /* 懸停時變亮 */
  transform: translateY(-2px);
}
</style>
