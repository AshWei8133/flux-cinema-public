<template>
  <div class="event-editor-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEditMode ? '編輯活動' : '新增活動' }}</span>
          <el-button class="button" style="background-color: lightgray" text @click="goBack">
            返回列表
          </el-button>
        </div>
      </template>

      <el-form :model="eventForm" :rules="rules" ref="eventFormRef" label-width="120px">
        <el-form-item label="活動標題" prop="title">
          <el-input v-model="eventForm.title" placeholder="請輸入活動標題" />
        </el-form-item>

        <!-- ✅ 用 eventCategoryId 當唯一欄位 -->
        <el-form-item label="活動分類" prop="eventCategoryId">
          <el-select
            v-model="eventForm.eventCategoryId"
            placeholder="請選擇活動分類"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.eventCategoryId"
              :label="category.eventCategoryName"
              :value="category.eventCategoryId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="開始日期" prop="startDate">
          <el-date-picker
            v-model="eventForm.startDate"
            type="date"
            placeholder="選擇開始日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="結束日期" prop="endDate">
          <el-date-picker
            v-model="eventForm.endDate"
            type="date"
            placeholder="選擇結束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="活動內容" prop="content">
          <el-input
            v-model="eventForm.content"
            type="textarea"
            :rows="5"
            placeholder="請輸入活動內容"
          />
        </el-form-item>

        <el-form-item label="活動圖片">
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
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">
            {{ isEditMode ? '更新活動' : '新增活動' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="dialogVisible" title="圖片預覽">
      <img :src="dialogImageUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useEventStore } from '@/features/Event/store/useEventStore'
import { useEventCategoryStore } from '@/features/Event/store/useEventCategoryStore'

const route = useRoute()
const router = useRouter()
const eventStore = useEventStore()
const eventCategoryStore = useEventCategoryStore()

// ✅ 從 storeToRefs 讀 categories（見下一段 store 修正）
const { categories } = storeToRefs(eventCategoryStore)
const { currentEvent } = storeToRefs(eventStore)

const isEditMode = ref(false)
const eventFormRef = ref(null)

const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

const eventForm = reactive({
  eventId: null,
  title: '',
  eventCategoryId: null, // ✅ 這裡統一改成 Id
  startDate: null,
  endDate: null,
  content: '',
  base64ImageString: null,
})

const rules = reactive({
  title: [
    { required: true, message: '請輸入活動標題', trigger: 'blur' },
    { min: 3, max: 50, message: '標題長度應在 3 到 50 個字元之間', trigger: 'blur' },
  ],
  eventCategoryId: [{ required: true, message: '請選擇活動分類', trigger: 'change' }], // ✅ prop 對上
  startDate: [{ required: true, message: '請選擇開始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '請選擇結束日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && eventForm.startDate && new Date(value) < new Date(eventForm.startDate)) {
          callback(new Error('結束日期不能早於開始日期'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  content: [
    { required: true, message: '請輸入活動內容', trigger: 'blur' },
    { min: 10, message: '內容至少需要 10 個字元', trigger: 'blur' },
  ],
})

/* 圖片處理 */
const handleImageChange = (file, uploadFiles) => {
  const okType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)
  const okSize = file.raw.size / 1024 / 1024 < 10
  if (!okType) {
    ElMessage.error('上傳圖片只能是 JPG/PNG/WEBP 格式!')
    uploadFiles.pop()
    return
  }
  if (!okSize) {
    ElMessage.error('上傳圖片大小不能超過 10MB!')
    uploadFiles.pop()
    return
  }

  const reader = new FileReader()
  reader.readAsDataURL(file.raw)
  reader.onload = () => {
    eventForm.base64ImageString = String(reader.result).split(',')[1] || null
  }
  reader.onerror = () => {
    ElMessage.error('圖片讀取失敗，請重試！')
    eventForm.base64ImageString = null
  }
  fileList.value = uploadFiles.slice(-1)
}
const handleImageRemove = () => {
  eventForm.base64ImageString = null
  fileList.value = []
}
const handleImagePreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

/* 提交 */
const submitForm = async () => {
  if (!eventFormRef.value) return
  try {
    await eventFormRef.value.validate()
    const payload = { ...eventForm } // 後端就吃 eventCategoryId
    if (isEditMode.value) {
      await eventStore.updateExistingEvent(eventForm.eventId, payload)
      ElMessage.success('活動更新成功！')
    } else {
      await eventStore.addEvent(payload)
      ElMessage.success('活動新增成功！')
    }
    goBack()
  } catch (err) {
    if (err === false) ElMessage.error('表單驗證失敗，請檢查輸入內容！')
    else {
      console.error('活動儲存失敗:', err)
      ElMessage.error('活動儲存失敗，請檢查控制台。')
    }
  }
}

const resetForm = () => {
  if (eventFormRef.value) eventFormRef.value.resetFields()
  eventForm.base64ImageString = null
  fileList.value = []
}

const goBack = () => router.push({ name: 'AdminEvents' })

/* 載入流程 */
watch(
  () => route.params.id,
  async (newId) => {
    resetForm()
    // 先確保有分類資料（下拉才有東西）
    await eventCategoryStore.fetchCategories()

    if (newId) {
      isEditMode.value = true
      await eventStore.fetchEventById(parseInt(newId))

      if (currentEvent.value) {
        // 帶入後端資料
        Object.assign(eventForm, currentEvent.value)

        // ✅ 從巢狀物件補 eventCategoryId
        if (!eventForm.eventCategoryId && currentEvent.value.category?.eventCategoryId) {
          eventForm.eventCategoryId = currentEvent.value.category.eventCategoryId
        }

        // 圖片
        if (eventForm.base64ImageString) {
          fileList.value = [
            {
              name: 'existing-image.jpg',
              url: `data:image/jpeg;base64,${eventForm.base64ImageString}`,
            },
          ]
        } else {
          fileList.value = []
        }
      } else {
        ElMessage.error('無法載入活動資料！')
        goBack()
      }
    } else {
      isEditMode.value = false
      eventForm.eventId = null
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.event-editor-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.box-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}
/* 限制只顯示一個上傳格 */
:deep(.el-upload-list--picture-card .el-upload-list__item + .el-upload--picture-card) {
  display: none !important;
}
</style>
