<template>
  <div class="announcement-editor-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEditMode ? '編輯公告' : '新增公告' }}</span>
          <el-button class="button" style="background-color: lightgray" text @click="goBack"
            >返回列表</el-button
          >
        </div>
      </template>

      <el-form
        :model="announcementForm"
        :rules="rules"
        ref="announcementFormRef"
        label-width="120px"
      >
        <el-form-item label="公告標題" prop="title">
          <el-input v-model="announcementForm.title" placeholder="請輸入公告標題"></el-input>
        </el-form-item>

        <el-form-item label="發布日期" prop="publishDate">
          <el-date-picker
            v-model="announcementForm.publishDate"
            type="date"
            placeholder="選擇日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>

        <el-form-item label="公告內容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="5"
            placeholder="請輸入公告內容"
          ></el-input>
        </el-form-item>

        <el-form-item label="公告圖片">
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
            {{ isEditMode ? '更新公告' : '新增公告' }}
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
import { useAnnouncementStore } from '@/features/Event/store/useAnnouncementStore'

const route = useRoute()
const router = useRouter()
const announcementStore = useAnnouncementStore()

const { currentAnnouncement } = storeToRefs(announcementStore)

const isEditMode = ref(false)
const announcementFormRef = ref(null)

// 用於控制 el-upload 顯示的檔案列表
const fileList = ref([])

// 用於圖片預覽對話框的狀態
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 表單數據模型
const announcementForm = reactive({
  announcementId: null,
  title: '',
  content: '',
  publishDate: null,
  base64ImageString: null,
})

// 表單驗證規則
const rules = reactive({
  title: [
    { required: true, message: '請輸入公告標題', trigger: 'blur' },
    { min: 3, max: 50, message: '標題長度應在 3 到 50 個字元之間', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '請輸入公告內容', trigger: 'blur' },
    { min: 10, message: '內容至少需要 10 個字元', trigger: 'blur' },
  ],
  publishDate: [{ required: true, message: '請選擇發布日期', trigger: 'change' }],
})

/**
 * 處理圖片檔案選擇、驗證與轉換。
 * @param {object} file - ElUpload 提供的當前檔案物件。
 * @param {Array} uploadFiles - ElUpload 維護的完整檔案列表。
 */
const handleImageChange = (file, uploadFiles) => {
  // 驗證檔案格式與大小
  const imageFormat = ['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)
  const imageSize = file.raw.size / 1024 / 1024 < 10

  if (!imageFormat) {
    ElMessage.error('上傳圖片只能是 JPG/PNG/WEBP 格式!')
    uploadFiles.pop()
  }
  if (!imageSize) {
    ElMessage.error('上傳圖片大小不能超過 10MB!')
    uploadFiles.pop()
    return
  }

  // 將檔案轉換為 Base64
  const reader = new FileReader()
  reader.readAsDataURL(file.raw)
  reader.onload = () => {
    announcementForm.base64ImageString = reader.result.split(',')[1]
  }
  reader.onerror = (error) => {
    console.error('圖片讀取失敗:', error)
    ElMessage.error('圖片讀取失敗，請重試！')
    announcementForm.base64ImageString = null
  }

  // 因為 limit=1，確保 fileList 中永遠只有一個檔案
  fileList.value = uploadFiles.slice(-1)
}

/**
 * 處理圖片移除事件。
 */
const handleImageRemove = () => {
  announcementForm.base64ImageString = null
  fileList.value = []
}

/**
 * 處理圖片預覽事件。
 * @param {object} uploadFile - ElUpload 提供的檔案物件。
 */
const handleImagePreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

/**
 * 提交表單（新增或更新公告）。
 */
const submitForm = async () => {
  if (!announcementFormRef.value) return
  try {
    await announcementFormRef.value.validate()
    const payload = { ...announcementForm }
    if (isEditMode.value) {
      await announcementStore.updateExistingAnnouncement(announcementForm.announcementId, payload)
      ElMessage.success('公告更新成功！')
    } else {
      await announcementStore.addAnnouncement(payload)
      ElMessage.success('公告新增成功！')
    }
    goBack()
  } catch (validationError) {
    if (validationError === false) {
      ElMessage.error('表單驗證失敗，請檢查輸入內容！')
    } else {
      console.error('公告儲存失敗:', validationError)
      ElMessage.error('公告儲存失敗，請檢查控制台。')
    }
  }
}

/**
 * 重置表單。
 */
const resetForm = () => {
  if (announcementFormRef.value) {
    announcementFormRef.value.resetFields()
  }
  announcementForm.base64ImageString = null
  fileList.value = [] // 清空檔案列表
}

/**
 * 返回列表頁。
 */
const goBack = () => {
  router.push({ name: 'AdminAnnouncements' })
}

// 監聽路由參數變化，判斷是否為編輯模式並載入數據
watch(
  () => route.params.id,
  async (newId) => {
    resetForm() // 切換時先重置表單
    if (newId) {
      isEditMode.value = true
      await announcementStore.fetchAnnouncementById(parseInt(newId))
      if (currentAnnouncement.value) {
        Object.assign(announcementForm, currentAnnouncement.value)

        // 如果公告帶有圖片，則建立一個假的 file 物件來顯示預覽圖
        if (announcementForm.base64ImageString) {
          fileList.value = [
            {
              name: 'existing-image.jpg', // 檔名不重要，僅供顯示
              url: `data:image/jpeg;base64,${announcementForm.base64ImageString}`,
            },
          ]
        }
      } else {
        ElMessage.error('無法載入公告資料！')
        goBack()
      }
    } else {
      isEditMode.value = false
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.announcement-editor-container {
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
:deep(.el-upload-list--picture-card .el-upload-list__item + .el-upload--picture-card) {
  display: none !important;
}
</style>
