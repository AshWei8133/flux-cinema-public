<template>
  <div class="coupon-condition-container">
    <div class="coupon-condition-container" :key="$route.fullPath"></div>
    <h2>設定優惠券發放條件</h2>

    <div class="form-section">
      <!-- 活動日期（父層沒傳 sessionDate 時，子層自選） -->
      <div class="form-item" v-if="!sessionDate">
        <label for="date-picker">選擇活動日期</label>
        <el-date-picker
          id="date-picker"
          v-model="localSessionDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="請選擇日期以載入場次"
          @change="loadSessionsByDate"
        />
      </div>

      <!-- 電影 -->
      <div class="form-item">
        <label for="movie-select">選擇電影</label>
        <el-select
          id="movie-select"
          v-model="selectedMovieId"
          placeholder="請選擇電影"
          :loading="moviesLoading"
          clearable
        >
          <el-option
            v-for="movie in allMoviesSafe"
            :key="String(movie.id)"
            :label="movie.titleLocal"
            :value="String(movie.id)"
          />
        </el-select>
      </div>

      <!-- 會員等級 -->
      <div class="form-item">
        <label for="member-level-select">選擇會員等級</label>
        <el-select
          id="member-level-select"
          v-model="selectedMemberLevelId"
          placeholder="請選擇會員等級"
          :loading="memberLevelsLoading"
          clearable
        >
          <el-option
            v-for="opt in memberLevelOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>

      <!-- 場次 -->
      <div class="form-item">
        <label for="session-select">選擇場次</label>
        <el-select
          id="session-select"
          v-model="selectedSessionId"
          placeholder="請選擇場次"
          :loading="sessionsLoading"
          :disabled="!effectiveSessionDate"
          clearable
        >
          <el-option
            v-for="opt in sessionOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <small v-if="!effectiveSessionDate" class="hint">
          未指定日期，暫不載入場次（可由上一頁傳入或在上方選取日期）
        </small>
      </div>
    </div>

    <div v-if="showFooter" class="navigation-buttons">
      <el-button @click="$emit('prev')">上一步</el-button>
      <el-button type="primary" @click="tryNext">下一步</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onActivated, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useMovieStore } from '@/features/Movies/store/useMovieStore'
import { useMemberLevelStore } from '@/features/MemberLevel/store/memberLevelStore'
import { useMovieSessionStore } from '@/features/MovieSessions/store/useMovieSessionStore'
import { useRoute } from 'vue-router'

// debug（開發時才輸出）
const dbg = (...args) => import.meta.env.DEV && console.log('[CouponCondition]', ...args)

/** props / emits */
const props = defineProps({
  formData: { type: Object, default: () => ({}) },
  sessionDate: { type: String, default: '' }, // e.g. '2025-08-30'
  showFooter: { type: Boolean, default: false },
})
const emit = defineEmits(['data-change', 'prev', 'next'])
const route = useRoute()

/** stores */
const movieStore = useMovieStore()
const memberLevelStore = useMemberLevelStore()
const movieSessionStore = useMovieSessionStore()

const { movies, isMoviesLoading: moviesLoading } = storeToRefs(movieStore)
const { levels, isLoading } = storeToRefs(memberLevelStore)
const memberLevelsLoading = isLoading
const { scheduleData, isMovieSessionUpdateLoading } = storeToRefs(movieSessionStore)
const sessionsLoading = isMovieSessionUpdateLoading

/** 安全陣列 */
const allMoviesSafe = computed(() => (Array.isArray(movies.value) ? movies.value : []))
const memberLevelsSafe = computed(() => (Array.isArray(levels.value) ? levels.value : []))
const allSessionsSafe = computed(() =>
  Array.isArray(scheduleData.value) ? scheduleData.value : [],
)

/** options：會員等級 */
const memberLevelOptions = computed(() =>
  memberLevelsSafe.value.map((l) => ({
    value: String(l.memberLevelId ?? l.id),
    label: l.levelName,
  })),
)

// 從電影清單補片名
const lookupMovieTitle = (s) => {
  const movieId = s.movieId ?? s.movie?.id
  if (!movieId) return null
  const m = allMoviesSafe.value.find((mm) => Number(mm.id) === Number(movieId))
  return m?.titleLocal || m?.title || null
}

// 時間轉 HH:mm
const normalizeTime = (v) => {
  if (!v) return null
  if (/^\d{2}:\d{2}(:\d{2})?$/.test(v)) return v.slice(0, 5)
  const m = String(v).match(/T(\d{2}:\d{2})/)
  return m ? m[1] : null
}

// normalize session
const normalizeSession = (s) => {
  const id = s.id ?? s.sessionId ?? s.movieSessionId
  const date = s.sessionDate ?? (s.startAt ? String(s.startAt).slice(0, 10) : null)
  const time =
    normalizeTime(s.sessionTime) ?? normalizeTime(s.startTime) ?? normalizeTime(s.startAt)

  const rawTitle =
    s.movieTitle ??
    s.titleLocal ??
    s.title ??
    s.movie?.titleLocal ??
    s.movie?.title ??
    lookupMovieTitle(s)

  const title = rawTitle || '（無片名）'

  return {
    value: id != null ? String(id) : null,
    label: `${title} - ${time ?? '（無時間）'}`,
    sessionDate: date,
    raw: s,
  }
}

/** 有效日期 */
const localSessionDate = ref(props.sessionDate || '')
const effectiveSessionDate = computed(() => props.sessionDate || localSessionDate.value || '')

/** 場次 options（依選電影過濾） */
const sessionOptions = computed(() => {
  if (!effectiveSessionDate.value) return []
  const base = allSessionsSafe.value.map(normalizeSession).filter((o) => !!o.value)
  if (!selectedMovieId.value) return base
  return base.filter((o) => String(o.raw.movieId ?? o.raw.movie?.id) === selectedMovieId.value)
})

// 一律以空白起始，避免帶入上一輪或父層殘留
const selectedMovieId = ref(null)
const selectedMemberLevelId = ref(null)
const selectedSessionId = ref(null)

/** 目前選到的場次 */
const selectedSessionOption = computed(() =>
  sessionOptions.value.find((opt) => opt.value === selectedSessionId.value),
)

// 【新增】目前選到的電影物件
const selectedMovieOption = computed(() =>
  allMoviesSafe.value.find((movie) => String(movie.id) === selectedMovieId.value),
)

// 【新增】目前選到的會員等級選項
const selectedMemberLevelOption = computed(() =>
  memberLevelOptions.value.find((opt) => opt.value === selectedMemberLevelId.value),
)

/** emit 同步回父層 */
watch([selectedMovieId, selectedMemberLevelId, selectedSessionId, effectiveSessionDate], () => {
  const payload = {
    movieId: selectedMovieId.value ? Number(selectedMovieId.value) : null,
    memberLevelId: selectedMemberLevelId.value ? Number(selectedMemberLevelId.value) : null,
    sessionId: selectedSessionId.value ? Number(selectedSessionId.value) : null,

    // 使用可選串聯運算子 (?.) 來避免在找不到選項時出錯
    movieTitle: selectedMovieOption.value?.titleLocal ?? '',
    memberLevelName: selectedMemberLevelOption.value?.label ?? '',
    sessionLabel: selectedSessionOption.value?.label ?? '',

    sessionDate: selectedSessionOption.value?.sessionDate || effectiveSessionDate.value || '',
  }
  dbg('emit:data-change ->', payload)
  emit('data-change', payload)
})

/** 子層挑日期 → 載入場次 */
const loadSessionsByDate = async (date) => {
  if (!date) return
  selectedSessionId.value = null
  await movieSessionStore.getSessionsByDate(date)
}

/** 全域重置：每次進來頁面都清空 */
const resetAll = async (clearDateToo = true) => {
  selectedMovieId.value = null
  selectedMemberLevelId.value = null
  selectedSessionId.value = null
  if (clearDateToo) {
    localSessionDate.value = ''
  }
  // 清掉場次列表的舊資料（若你的 store 有 reset action，呼叫那個更乾淨）
  movieSessionStore.$patch({ scheduleData: [] })
}

/** 掛載初始化 */
onMounted(async () => {
  if (!allMoviesSafe.value.length) await movieStore.fetchAllMovies()
  if (!memberLevelsSafe.value.length) await memberLevelStore.fetchLevels()
  if (effectiveSessionDate.value)
    await movieSessionStore.getSessionsByDate(effectiveSessionDate.value)
  // 進頁面強制重置；若父層有傳日期，再載入場次
  await resetAll(/* clearDateToo = */ !props.sessionDate)
  if (props.sessionDate) {
    localSessionDate.value = props.sessionDate
    await movieSessionStore.getSessionsByDate(props.sessionDate)
  }

  import.meta.env.DEV && console.table(allSessionsSafe.value.slice(0, 3))
})
/** 若有 keep-alive，從其他頁回來時也重置一次（確保 v-model 不殘留） */
onActivated(async () => {
  await resetAll(!props.sessionDate)
  if (props.sessionDate) {
    localSessionDate.value = props.sessionDate
    await movieSessionStore.getSessionsByDate(props.sessionDate)
  }
})

/** 父層日期變化 → reload 場次 */
watch(
  () => props.sessionDate,
  async (d) => {
    // 父層換日期 → 全清空再載入新日期場次
    //     await resetAll(false) // 不清 localSessionDate，馬上覆寫
    if (d) {
      localSessionDate.value = d
      await movieSessionStore.getSessionsByDate(d)
    } else {
      movieSessionStore.$patch({ scheduleData: [] })
    }
  },
)

/** 驗證 API */
const validate = async () => {
  const ok = !!(selectedMovieId.value || selectedMemberLevelId.value || selectedSessionId.value)
  if (!ok) ElMessage.warning('請至少選擇一項條件。')
  return ok
}
defineExpose({ validate })

const tryNext = async () => {
  if (await validate()) emit('next')
}
</script>

<style scoped>
.coupon-condition-container {
  padding: 20px;
}
.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 600px;
  margin-top: 20px;
}
.form-item {
  display: flex;
  flex-direction: column;
}
.form-item label {
  margin-bottom: 8px;
  font-weight: bold;
}
.hint {
  color: var(--el-text-color-secondary);
}
.navigation-buttons {
  max-width: 600px;
  margin: 12px 0 0 auto;
  text-align: right;
}
</style>
