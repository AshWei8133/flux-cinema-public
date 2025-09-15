<template>
  <div class="discount-form-container">
    <h2>選擇優惠類型與內容</h2>
    <el-form
      :model="formData"
      :rules="formRules"
      ref="formRef"
      label-width="120px"
      class="discount-form"
    >
      <!-- 優惠券類型 -->
      <el-form-item label="優惠券類型" prop="couponCategoryId" required>
        <el-select v-model="formData.couponCategoryId" placeholder="請選擇優惠券類型" clearable>
          <el-option
            v-for="c in categories"
            :key="c.couponCategoryId"
            :label="c.couponCategoryName"
            :value="c.couponCategoryId"
          />
        </el-select>
        <small v-if="!categories.length" class="text-muted">
          目前沒有類型，請先到「優惠券類型」頁新增。
        </small>
      </el-form-item>

      <!-- 優惠券名稱 -->
      <el-form-item label="優惠券名稱" prop="couponName">
        <el-input v-model="formData.couponName" placeholder="例如：新會員專享優惠"></el-input>
      </el-form-item>

      <!-- 描述 -->
      <el-form-item label="描述" prop="couponDescription">
        <el-input
          v-model="formData.couponDescription"
          type="textarea"
          :rows="3"
          placeholder="請描述此優惠券的詳細內容"
        />
      </el-form-item>

      <!-- 折扣類型（可取消） -->
      <el-form-item label="折扣類型" prop="discountType">
        <!-- v-model 綁 "localDiscountType"（computed getter/setter） -->
        <el-radio-group v-model="localDiscountType">
          <!-- ⚠️ 用 value，而不是 label（消除 deprecate 警告） -->
          <el-radio :value="'PERCENTAGE'">百分比折扣 (e.g., 80 代表八折)</el-radio>
          <el-radio :value="'FIXED'">固定金額折抵 (e.g., 100 代表折抵 $100)</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 折扣數值 -->
      <el-form-item
        v-if="formData.discountType === 'PERCENTAGE'"
        label="折扣百分比"
        prop="discountAmount"
      >
        <el-input-number v-model="formData.discountAmount" :min="1" :max="99"></el-input-number> %
      </el-form-item>

      <el-form-item v-if="formData.discountType === 'FIXED'" label="折抵金額" prop="discountAmount">
        <el-input-number v-model="formData.discountAmount" :min="0" :step="10"></el-input-number> 元
      </el-form-item>

      <!-- 最低消費金額 -->
      <el-form-item label="最低消費金額" prop="minimumSpend">
        <el-input-number v-model="formData.minimumSpend" :min="0" :step="100"></el-input-number> 元
      </el-form-item>

      <!-- 優惠券圖片 -->
      <el-form-item label="優惠券圖片" prop="couponImageBase64">
        <el-upload
          ref="uploadRef"
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :limit="1"
          :file-list="couponFileList"
          :on-change="handleCouponImageChange"
          :on-remove="handleCouponImageRemove"
          :on-preview="handleCouponImagePreview"
          accept="image/jpeg, image/png, image/webp"
        >
          <template #default>
            <el-icon><Plus /></el-icon>
          </template>
          <template #tip>
            <div class="el-upload__tip">只能上傳一張 JPG/PNG/WEBP 圖片，且不超過 10MB。</div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <el-dialog v-model="uploadPreviewVisible" title="圖片預覽">
      <img :src="uploadPreviewUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useCouponCategoryStore } from '@/features/Event/store/useCouponCategoryStore'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ formData: { type: Object, required: true } })
const emit = defineEmits(['data-change', 'next', 'prev'])

const couponCategoryStore = useCouponCategoryStore()
const { categories } = storeToRefs(couponCategoryStore)

const formRef = ref(null)

const formData = computed(() => props.formData)
// script setup 內

/**
 * 可寫 computed：
 * - 讀：回傳目前選中的折扣型態（字串或空字串）
 * - 寫：如果寫入的值 == 目前值 → 清空（達成「點同一顆取消」）
 *       否則就切換成新值；順手處理 amount 預設或清空
 */
const localDiscountType = computed({
  get: () => formData.value.discountType || '',
  set: (val) => {
    if (val === formData.value.discountType) {
      // 再按一次同選項 → 取消
      formData.value.discountType = ''
      formData.value.discountAmount = null
    } else {
      formData.value.discountType = val
      // 給個安全初值（避免驗證時為空）
      if (
        val === 'PERCENTAGE' &&
        (formData.value.discountAmount == null || formData.value.discountAmount === '')
      ) {
        formData.value.discountAmount = 1
      }
      if (
        val === 'FIXED' &&
        (formData.value.discountAmount == null || formData.value.discountAmount === '')
      ) {
        formData.value.discountAmount = 1
      }
    }
  },
})

/* 上傳預覽狀態 */
const uploadPreviewVisible = ref(false)
const uploadPreviewUrl = ref('')
const couponFileList = ref([])

/** 折扣欄位規則（維持你原本邏輯） */
const validateDiscountAmount = (rule, value, callback) => {
  const t = props.formData?.discountType
  if (!t) return callback()
  if (value === undefined || value === null || value === '')
    return callback(new Error('請輸入折扣數值'))
  if (t === 'PERCENTAGE' && (value < 1 || value > 99))
    return callback(new Error('百分比需介於 1~99'))
  if (t === 'FIXED' && value < 1) return callback(new Error('折抵金額需 ≥ 1'))
  callback()
}

const formRules = reactive({
  couponCategoryId: [{ required: true, message: '請選擇優惠券類型', trigger: 'change' }],
  couponName: [
    { required: true, message: '請輸入優惠券名稱', trigger: 'blur' },
    { min: 2, max: 50, message: '長度需在 2 到 50 個字元', trigger: 'blur' },
  ],
  couponDescription: [
    { required: true, message: '請輸入描述', trigger: 'blur' },
    { max: 200, message: '長度不能超過 200 個字元', trigger: 'blur' },
  ],
  discountAmount: [{ validator: validateDiscountAmount, trigger: ['blur', 'change'] }],
})

/* === 關鍵 1：選擇類型時，同步名稱到 formData，讓 Step4 能顯示 === */
watch(
  () => props.formData.couponCategoryId,
  (id) => {
    if (!id) {
      props.formData.couponCategoryName = ''
    } else {
      const m = (categories.value || []).find((c) => c.couponCategoryId === id)
      props.formData.couponCategoryName = m?.couponCategoryName || ''
    }
    emit('data-change', props.formData)
  },
  { immediate: true },
)

/* === 關鍵 2：上傳圖片時寫入 base64，並支援預覽/移除 === */
const readFileAsBase64 = (rawFile) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(rawFile)
  })

const handleCouponImageChange = async (file, fileList) => {
  try {
    const dataUrl = await readFileAsBase64(file.raw)
    // data:image/jpeg;base64,xxxx
    const base64 = String(dataUrl).split(',')[1] || ''
    props.formData.couponImageBase64 = base64
    couponFileList.value = fileList.slice(-1) // 僅保留 1 張
    emit('data-change', props.formData)
  } catch (e) {
    console.error(e)
    ElMessage.error('讀取圖片失敗')
  }
}

const handleCouponImageRemove = () => {
  props.formData.couponImageBase64 = ''
  couponFileList.value = []
  emit('data-change', props.formData)
}

const handleCouponImagePreview = async (file) => {
  if (file?.url) {
    uploadPreviewUrl.value = file.url
  } else if (props.formData.couponImageBase64) {
    uploadPreviewUrl.value = `data:image/*;base64,${props.formData.couponImageBase64}`
  } else if (file?.raw) {
    const dataUrl = await readFileAsBase64(file.raw)
    uploadPreviewUrl.value = dataUrl
  } else {
    return
  }
  uploadPreviewVisible.value = true
}

/* 下一步 */
const handleNext = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      emit('next')
    } else {
      ElMessage.error('請檢查表單輸入是否正確。')
    }
  })
}

const handlePrev = () => emit('prev')

onMounted(async () => {
  if ((categories.value || []).length === 0) {
    await couponCategoryStore.fetchCategories()
  }
  // 若已有 base64，建立對應的檔案列，讓 UI 有縮圖
  if (props.formData.couponImageBase64 && couponFileList.value.length === 0) {
    couponFileList.value = [
      {
        name: 'coupon.jpg',
        url: `data:image/*;base64,${props.formData.couponImageBase64}`,
        status: 'success',
      },
    ]
    await nextTick()
  }
})
</script>

<style scoped>
.discount-form-container {
  padding: 20px;
}
.discount-form {
  max-width: 600px;
  margin: 0 auto;
}
.navigation-buttons {
  margin-top: 30px;
  text-align: right;
}
:deep(.el-upload-list--picture-card .el-upload-list__item + .el-upload--picture-card) {
  display: none !important;
}
</style>
