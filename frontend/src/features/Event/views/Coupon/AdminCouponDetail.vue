<!-- src/features/Event/views/admin/AdminCouponDetail.vue -->
<template>
  <div class="coupon-editor">
    <!-- 工具列 -->
    <div class="toolbar">
      <div class="left">
        <el-button :icon="Back" @click="goList">返回列表</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="fetchOne">重新整理</el-button>
      </div>
      <div class="right">
        <el-popconfirm
          title="確定刪除這張優惠券？"
          confirm-button-text="刪除"
          cancel-button-text="取消"
          @confirm="remove"
        >
          <template #reference>
            <el-button type="danger" :loading="removing" :icon="Delete">刪除</el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <el-card class="box" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>編輯優惠券</span>
          <el-tag :type="form.status === 'ACTIVE' ? 'success' : 'info'">{{ form.status }}</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="coupon-form">
        <!-- 1) 基本欄位 -->
        <el-form-item label="所屬活動" prop="eventId">
          <el-select
            v-model="form.eventId"
            placeholder="請選擇活動"
            filterable
            :loading="eventsLoading"
            style="width: 100%"
            @change="onEventChange"
          >
            <el-option v-for="e in eventOptions" :key="e.value" :label="e.label" :value="e.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="優惠券類型" prop="couponCategoryId">
          <el-select
            v-model="form.couponCategoryId"
            placeholder="請選擇類型"
            :loading="categoriesLoading"
            style="width: 100%"
            @change="syncCategoryName"
          >
            <el-option
              v-for="c in categoryOptions"
              :key="c.value"
              :label="c.label"
              :value="c.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="優惠券名稱" prop="couponName">
          <el-input v-model="form.couponName" placeholder="例如：新會員專享優惠" />
        </el-form-item>

        <el-form-item label="描述" prop="couponDescription">
          <el-input
            v-model="form.couponDescription"
            type="textarea"
            :rows="3"
            placeholder="請描述此優惠券的詳細內容"
          />
        </el-form-item>

        <!-- 2) 折扣設定 -->
        <el-form-item label="折扣類型" prop="discountType">
          <el-radio-group v-model="localDiscountType">
            <el-radio :value="''">不設定</el-radio>
            <el-radio :value="'PERCENTAGE'">百分比折扣</el-radio>
            <el-radio :value="'FIXED'">固定金額折抵</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="form.discountType === 'PERCENTAGE'"
          label="折扣百分比"
          prop="discountAmount"
        >
          <el-input-number v-model="form.discountAmount" :min="1" :max="99" /> %
        </el-form-item>

        <el-form-item v-if="form.discountType === 'FIXED'" label="折抵金額" prop="discountAmount">
          <el-input-number v-model="form.discountAmount" :min="1" :step="10" /> 元
        </el-form-item>

        <el-form-item label="最低消費金額" prop="minimumSpend">
          <el-input-number v-model="form.minimumSpend" :min="0" :step="100" /> 元
        </el-form-item>

        <el-form-item label="張數（quantity）" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" />
        </el-form-item>

        <el-form-item label="每人可兌換次數" prop="redeemableTimes">
          <el-input-number v-model="form.redeemableTimes" :min="1" :max="99" />
        </el-form-item>

        <el-form-item label="狀態" prop="status">
          <el-segmented v-model="form.status" :options="['ACTIVE', 'INACTIVE']" />
        </el-form-item>

        <!-- 3) 圖片上傳 -->
        <el-form-item label="優惠券圖片">
          <el-upload
            ref="uploadRef"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :on-preview="handleImagePreview"
            accept="image/jpeg, image/png, image/webp"
          >
            <template #default>
              <el-icon><Plus /></el-icon>
            </template>
            <template #tip>
              <div class="el-upload__tip">只能上傳一張 JPG/PNG/WEBP 圖片，且不超過 10MB。</div>
            </template>
          </el-upload>
          <el-dialog v-model="previewVisible" title="圖片預覽">
            <img :src="previewUrl" alt="Preview" style="width: 100%" />
          </el-dialog>
        </el-form-item>

        <!-- 動作 -->
        <el-form-item>
          <el-button @click="resetEdit" :disabled="saving">重設</el-button>
          <el-button type="primary" :loading="saving" @click="save">儲存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Refresh, Delete, Plus } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import httpClient from '@/services/api'

/* stores */
import { useEventStore } from '@/features/Event/store/useEventStore'
import { useMovieStore } from '@/features/Movies/store/useMovieStore'
import { useMemberLevelStore } from '@/features/MemberLevel/store/memberLevelStore'
import { useMovieSessionStore } from '@/features/MovieSessions/store/useMovieSessionStore'
import { useCouponCategoryStore } from '../../store/useCouponCategoryStore'

/* 路由 */
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id)

/* ====== 外部資源載入 ====== */
const eventStore = useEventStore()
const { events, loading: eventsLoading } = storeToRefs(eventStore)

const couponCategoryStore = useCouponCategoryStore()
const { categories } = storeToRefs(couponCategoryStore)
const categoriesLoading = ref(false)

const movieStore = useMovieStore()
const { movies, isMoviesLoading: moviesLoading } = storeToRefs(movieStore)

const memberLevelStore = useMemberLevelStore()
const { levels, isLoading: levelsLoading } = storeToRefs(memberLevelStore)

const movieSessionStore = useMovieSessionStore()
const { scheduleData, isMovieSessionUpdateLoading: sessionsLoading } =
  storeToRefs(movieSessionStore)

/* options */
const eventOptions = computed(() =>
  (events.value?.list ?? []).map((e) => ({ value: e.eventId, label: e.title })),
)
const categoryOptions = computed(() =>
  (categories.value ?? []).map((c) => ({
    value: c.couponCategoryId,
    label: c.couponCategoryName,
  })),
)
const movieOptions = computed(() =>
  (Array.isArray(movies.value) ? movies.value : []).map((m) => ({
    value: Number(m.id),
    label: m.titleLocal || m.title,
  })),
)
const memberLevelOptions = computed(() =>
  (Array.isArray(levels.value) ? levels.value : []).map((l) => ({
    value: Number(l.memberLevelId ?? l.id),
    label: l.levelName,
  })),
)

/* 場次（若後面又要開啟條件時會用到） */
const sessionDateLocal = ref('')
const sessionDateEffective = computed(() => sessionDateLocal.value || form.sessionDate || '')
const sessionOptions = computed(() => {
  const base = (Array.isArray(scheduleData.value) ? scheduleData.value : [])
    .map((s) => {
      const id = s.id ?? s.sessionId ?? s.movieSessionId
      const time =
        normalizeTime(s.sessionTime) ?? normalizeTime(s.startTime) ?? normalizeTime(s.startAt)
      const movieId = s.movieId ?? s.movie?.id
      const title =
        (s.movieTitle && s.movieTitle.trim()) ||
        (s.titleLocal && s.titleLocal.trim()) ||
        (s.title && s.title.trim()) ||
        (s.movie?.titleLocal && s.movie?.titleLocal.trim()) ||
        (s.movie?.title && s.movie?.title.trim()) ||
        '（無片名）'
      return {
        value: id != null ? String(id) : null,
        label: `${title} - ${time ?? '（無時間）'}`,
        movieId,
        sessionDate: s.sessionDate ?? (s.startAt ? String(s.startAt).slice(0, 10) : null),
      }
    })
    .filter((o) => !!o.value)

  if (!form.movieId) return base
  return base.filter((o) => Number(o.movieId) === Number(form.movieId))
})
const normalizeTime = (v) => {
  if (!v) return null
  if (/^\d{2}:\d{2}(:\d{2})?$/.test(v)) return v.slice(0, 5)
  const m = String(v).match(/T(\d{2}:\d{2})/)
  return m ? m[1] : null
}

/* ====== 表單狀態 ====== */
const loading = ref(false)
const saving = ref(false)
const removing = ref(false)
const formRef = ref(null)

/** 表單模型 */
const form = reactive({
  couponId: null,
  eventId: null,
  couponCategoryId: null,
  couponCategoryName: '',
  couponName: '',
  couponDescription: '',
  discountType: '', // '', 'PERCENTAGE', 'FIXED'
  discountAmount: null,
  minimumSpend: 0,
  status: 'ACTIVE',
  quantity: 1,
  redeemableTimes: 1,
  // 條件（目前畫面未開，但模型保留）
  movieId: null,
  movieTitle: '',
  memberLevelId: null,
  memberLevelName: '',
  sessionId: null,
  sessionLabel: '',
  sessionDate: '',
  // 圖片（保留完整 dataURL）
  couponImageBase64: '',
})

/* 折扣型別的切換（再點同選項=取消） */
const localDiscountType = computed({
  get: () => form.discountType || '',
  set: (val) => {
    if (val === form.discountType) {
      form.discountType = ''
      form.discountAmount = null
    } else {
      form.discountType = val
      if (form.discountAmount == null || form.discountAmount === '') form.discountAmount = 1
    }
  },
})

/* 圖片上傳/預覽 */
const uploadRef = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const readFileAsBase64 = (rawFile) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result) // ← 回傳完整 dataURL
    reader.onerror = reject
    reader.readAsDataURL(rawFile)
  })

const handleImageChange = async (file, list) => {
  const okType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)
  const okSize = file.raw.size / 1024 / 1024 < 10
  if (!okType) return ElMessage.error('上傳圖片只能是 JPG/PNG/WEBP 格式!')
  if (!okSize) return ElMessage.error('上傳圖片大小不能超過 10MB!')

  const dataUrl = await readFileAsBase64(file.raw)
  form.couponImageBase64 = String(dataUrl) // ← 保留完整 dataURL
  fileList.value = list.slice(-1)
}

const handleImageRemove = () => {
  form.couponImageBase64 = ''
  fileList.value = []
}

const handleImagePreview = (uploadFile) => {
  if (!uploadFile?.url) {
    console.warn('[DEBUG][preview] 沒有 url 可預覽，uploadFile =', uploadFile)
    return
  }
  previewUrl.value = uploadFile.url
  previewVisible.value = true
}

/* 驗證規則 */
const rules = {
  eventId: [{ required: true, message: '請選擇活動', trigger: 'change' }],
  couponCategoryId: [{ required: true, message: '請選擇優惠券類型', trigger: 'change' }],
  couponName: [
    { required: true, message: '請輸入名稱', trigger: 'blur' },
    { min: 2, max: 50, message: '長度需在 2 到 50 個字元', trigger: 'blur' },
  ],
  couponDescription: [{ max: 200, message: '長度不能超過 200 個字元', trigger: 'blur' }],
  discountAmount: [
    {
      validator: (_, v, cb) => {
        const t = form.discountType
        if (!t) return cb()
        if (v == null || v === '') return cb(new Error('請輸入折扣數值'))
        if (t === 'PERCENTAGE' && (v < 1 || v > 99)) return cb(new Error('百分比需介於 1~99'))
        if (t === 'FIXED' && v < 1) return cb(new Error('折抵金額需 ≥ 1'))
        cb()
      },
      trigger: ['blur', 'change'],
    },
  ],
  quantity: [{ required: true, type: 'number', min: 1, message: '至少 1 張', trigger: 'change' }],
  redeemableTimes: [
    {
      validator: (_, v, cb) => {
        const n = Number(v)
        if (!Number.isInteger(n) || n < 1 || n > 99) return cb(new Error('1~99 的整數'))
        cb()
      },
      trigger: 'change',
    },
  ],
}

/* 名稱同步（顯示用） */
const syncCategoryName = () => {
  const m = (categories.value || []).find((c) => c.couponCategoryId === form.couponCategoryId)
  form.couponCategoryName = m?.couponCategoryName || ''
}
const syncMovieTitle = () => {
  const m = (Array.isArray(movies.value) ? movies.value : []).find(
    (mm) => Number(mm.id) === Number(form.movieId),
  )
  form.movieTitle = m?.titleLocal || m?.title || ''
}
const syncMemberLevelName = () => {
  const m = memberLevelOptions.value.find((x) => Number(x.value) === Number(form.memberLevelId))
  form.memberLevelName = m?.label || ''
}
const syncSessionLabel = () => {
  const s = sessionOptions.value.find((x) => String(x.value) === String(form.sessionId))
  form.sessionLabel = s?.label || ''
  form.sessionDate = s?.sessionDate || form.sessionDate || ''
}

/* 場次載入 */
const loadSessions = async (date) => {
  if (!date) return
  await movieSessionStore.getSessionsByDate(date)
}

/* ====== 載入單筆、序列化、儲存、刪除 ====== */
const parseView = (r) => {
  console.log('[DEBUG][parseView] 原始回傳 res:', r)

  const map = (k, ...alts) => r[k] ?? alts.map((a) => r[a]).find((v) => v != null)
  console.log('[DEBUG][parseView] 原始回傳 res keys:', Object.keys(r))
  console.log('[DEBUG][parseView] 原始回傳 res:', JSON.stringify(r, null, 2))
  const discountTypeRaw = (map('discountType', 'coupon_category') || '').toUpperCase()
  const discountType =
    discountTypeRaw === 'PERCENTAGE' || discountTypeRaw === 'PERCENT'
      ? 'PERCENTAGE'
      : discountTypeRaw === 'FIXED' || discountTypeRaw === 'AMOUNT'
        ? 'FIXED'
        : ''
  const base64 = map(
    'couponImageBase64',
    'couponImage',
    'coupon_image',
    'imageBase64',
    'image_base64',
  )
  console.log('[DEBUG][parseView] 解析後的 couponImageBase64:', base64)
  return {
    couponId: map('couponId', 'id', 'coupon_id'),
    eventId: map('eventId', 'event_id'),
    couponCategoryId: map('couponCategoryId', 'coupon_category_id'),
    couponCategoryName: map('couponCategoryName', 'coupon_category_name') || '',
    couponName: map('couponName', 'coupon_name') || '',
    couponDescription: map('couponDescription', 'coupon_description') || '',
    discountType,
    discountAmount: map('discountAmount', 'discount_amount') ?? null,
    minimumSpend: map('minimumSpend', 'minimum_spend') ?? 0,
    status: (map('status') || 'ACTIVE').toUpperCase(),
    quantity: Number(map('quantity') ?? 1),
    redeemableTimes: Number(map('redeemableTimes', 'redeemable_times') ?? 1),
    movieId: map('movieId', 'movie_id') ?? null,
    movieTitle: map('movieTitle', 'movie_title', 'movieName', 'movie_name') || '',
    memberLevelId: map('memberLevelId', 'member_level_id') ?? null,
    memberLevelName: map('memberLevelName', 'member_level_name') || '',
    sessionId: map('sessionId', 'session_id') ?? null,
    sessionLabel: map('sessionLabel', 'session_name', 'sessionTime', 'session_time') || '',
    sessionDate: map('sessionDate', 'session_date', 'date') || '',
    // 後端多半回純 base64，這裡先存純碼；下面 fetchOne 會補 dataURL
    couponImageBase64:
      map('couponImageBase64', 'couponImage', 'coupon_image', 'imageBase64', 'image_base64') || '',
  }
}

/** 建立 PUT payload（送後端只帶純 base64） */
const buildPayload = () => {
  const toNull = (x) => (x === '' ? null : x)
  const stripDataUrl = (v) => {
    if (!v) return null
    const i = String(v).indexOf(',')
    return i >= 0 ? String(v).slice(i + 1) : v
  }
  return {
    eventId: form.eventId ?? null,
    couponCategoryId: form.couponCategoryId ?? null,
    couponName: form.couponName?.trim(),
    couponDescription: toNull(form.couponDescription?.trim() ?? null),
    discountType: form.discountType || null,
    discountAmount: form.discountType ? Number(form.discountAmount ?? 0) : null,
    minimumSpend:
      form.minimumSpend != null && form.minimumSpend !== '' ? Number(form.minimumSpend) : 0,
    status: form.status || 'ACTIVE',
    quantity: form.quantity != null && form.quantity !== '' ? Number(form.quantity) : Number(1),
    redeemableTimes:
      form.redeemableTimes != null && form.redeemableTimes !== ''
        ? Number(form.redeemableTimes)
        : 1,
    // 這裡才把 dataURL 拆成純 base64
    couponImageBase64: toNull(stripDataUrl(form.couponImageBase64)) ?? null,
    // 條件
    movieId: form.movieId ?? null,
    memberLevelId: form.memberLevelId ?? null,
    sessionId: form.sessionId ?? null,
    sessionDate: form.sessionDate || sessionDateLocal.value || null,
  }
}

const fetchOne = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res = await httpClient.get(`/admin/coupons/${id.value}`)
    const v = parseView(res || {})
    Object.assign(form, v)
    console.log('[DEBUG][fetchOne] form.couponImageBase64 after assign:', form.couponImageBase64)

    // 後端回純 base64 → 轉回 dataURL 給 el-upload 預覽
    if (form.couponImageBase64) {
      const isDataUrl = String(form.couponImageBase64).startsWith('data:')
      const dataUrl = isDataUrl
        ? form.couponImageBase64
        : `data:image/jpeg;base64,${form.couponImageBase64}`

      console.log('[DEBUG][fetchOne] 最終組合出的 dataUrl:', dataUrl)

      form.couponImageBase64 = dataUrl
      fileList.value = [{ name: 'coupon.jpg', url: dataUrl, status: 'success' }]

      console.log('[DEBUG][fetchOne] 設定 fileList:', fileList.value)
    } else {
      console.log('[DEBUG][fetchOne] 沒有圖片資料，fileList 清空')

      fileList.value = []
    }

    // （若有場次日期）同步載入
    sessionDateLocal.value = form.sessionDate || ''
    if (sessionDateLocal.value) await loadSessions(sessionDateLocal.value)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

const validateForm = async () => {
  await formRef.value?.validate()
}

const save = async () => {
  try {
    await validateForm()
  } catch (e) {
    return ElMessage.error(e.message || '資料未通過驗證')
  }
  const payload = buildPayload()
  saving.value = true
  try {
    await httpClient.put(`/admin/coupons/${form.couponId}`, payload)
    ElMessage.success('已儲存')

    // 儲存後，同步本地預覽（以目前 form 的 dataURL 為準）
    if (form.couponImageBase64) {
      fileList.value = [{ name: 'coupon.jpg', url: form.couponImageBase64, status: 'success' }]
    } else {
      fileList.value = []
    }

    await fetchOne()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '儲存失敗（伺服器錯誤）')
  } finally {
    saving.value = false
  }
}

const remove = async () => {
  if (!form.couponId) return
  removing.value = true
  try {
    await httpClient.delete(`/admin/coupons/${form.couponId}`)
    ElMessage.success('已刪除')
    goList(true)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '刪除失敗')
  } finally {
    removing.value = false
  }
}

const goList = (refresh = false) => {
  router.push({ name: 'AdminCouponList', query: refresh ? { t: Date.now() } : {} })
}

/* 重設（修掉 resetEdit 未定義的警告） */
const resetEdit = () => {
  form.couponName = ''
  form.couponDescription = ''
  form.discountType = ''
  form.discountAmount = null
  form.minimumSpend = 0
  form.quantity = 1
  form.redeemableTimes = 1
  form.status = 'ACTIVE'
  form.couponImageBase64 = ''
  fileList.value = []
}

onMounted(async () => {
  if (!events.value?.list?.length) await eventStore.fetchEvents()
  categoriesLoading.value = true
  try {
    if (!(categories.value || []).length) await couponCategoryStore.fetchCategories()
  } finally {
    categoriesLoading.value = false
  }
  if (!Array.isArray(movies.value) || !movies.value.length) await movieStore.fetchAllMovies()
  if (!Array.isArray(levels.value) || !levels.value.length) await memberLevelStore.fetchLevels()
  await fetchOne()
})

const onEventChange = () => {
  /* 可依活動帶入預設日期等… */
}

/* 若之後打開場次條件，可用這段 */
watch(sessionDateLocal, async (d) => {
  if (d) {
    form.sessionId = null
    await loadSessions(d)
  }
})
</script>

<style scoped>
.coupon-editor {
  display: grid;
  gap: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.box {
  border-radius: 14px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}
.coupon-form {
  max-width: 900px;
}
.hint {
  color: var(--el-text-color-secondary);
  margin-left: 8px;
}
:deep(.el-upload-list--picture-card .el-upload-list__item + .el-upload--picture-card) {
  display: none !important; /* 僅允許 1 張 */
}
</style>
