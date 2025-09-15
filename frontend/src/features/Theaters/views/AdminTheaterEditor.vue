<template>
  <div class="theater-editor-container">
    <el-card class="editor-card">
      <el-form :model="theaterForm" label-width="120px" ref="theaterFormRef" :rules="formRules">
        <el-form-item label="影廳名稱" prop="theaterName">
          <el-input v-model="theaterForm.theaterName" placeholder="請輸入影廳名稱" />
        </el-form-item>

        <el-form-item label="影廳照片">
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
          >
            <template #default>
              <el-icon><Plus /></el-icon>
            </template>
            <template #tip>
              <div class="el-upload__tip">只能上傳一張 JPG/PNG 圖片，且不超過 2MB。</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="影廳類別" prop="theaterTypeId">
          <TheaterTypeSelect v-model="theaterForm.theaterTypeId" />
        </el-form-item>

        <el-form-item label="座位編輯" prop="seats">
          <AdminButton @click="openSeatEditorDialog" type="primary" plain>編輯座位</AdminButton>
          <div v-if="theaterForm.seats.length > 0" class="seats-info">
            已建立 {{ theaterForm.seats.length }} 個座位
          </div>
        </el-form-item>

        <el-form-item>
          <AdminButton v-if="!isUpdateMode" type="primary" @click="submitForm">新增</AdminButton>
          <AdminButton v-if="isUpdateMode && isFormModified" type="warning" @click="updateForm"
            >更新</AdminButton
          >
          <AdminButton @click="goBack">返回</AdminButton>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      v-model="seatEditorDialogVisible"
      title="座位編輯"
      width="80%"
      :close-on-click-modal="false"
    >
      <SeatEditor ref="seatEditorRef" />
      <template #footer>
        <span class="dialog-footer">
          <AdminButton @click="seatEditorDialogVisible = false">取消</AdminButton>
          <AdminButton type="primary" @click="saveSeats">確認</AdminButton>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="圖片預覽" @closed="handlePreviewDialogClosed">
      <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
// 從 vue 匯入 Composition API 相關函式
import { reactive, ref, watch, nextTick } from 'vue'
// 從 vue-router 匯入路由相關函式
import { useRoute, useRouter } from 'vue-router'
// 從 Element Plus 匯入訊息提示和對話框組件
import { ElMessage } from 'element-plus'
// 從 Element Plus 匯入加號圖示
import { Plus } from '@element-plus/icons-vue'
// 匯入自定義的 store
import { useTheaterStore } from '../store/useTheaterStore'
// 匯入自定義的子組件
import TheaterTypeSelect from '../components/TheaterTypeSelect.vue'
import AdminButton from '@/components/admin/AdminButton.vue'
import SeatEditor from '../components/SeatEditor.vue'

// 呼叫 useRouter 取得路由實例
const router = useRouter()

// 呼叫 route 取得當前路由
const route = useRoute()

// 呼叫 useTheaterStore 取得 store 實例
const theaterStore = useTheaterStore()

// 模板引用，用於訪問表單和子組件的實例
const theaterFormRef = ref(null) // 表單實例
const seatEditorRef = ref(null) // SeatEditor 子組件實例

// 使用 reactive 建立響應式表單資料物件
const theaterForm = reactive({
  theaterName: '',
  theaterPhoto: null, // 影廳照片的檔案物件
  theaterTypeId: null,
  totalSeats: 0,
  seats: [], // 儲存座位資料的一維陣列
})

// 使用 ref 建立響應式變數
const fileList = ref([]) // 儲存上傳圖片列表
const formRules = {
  // 表單驗證規則
  theaterName: [{ required: true, message: '請輸入影廳名稱', trigger: 'blur' }],
  theaterTypeId: [{ required: true, message: '請選擇影廳類別', trigger: 'change' }],
  seats: [{ required: false, message: '請編輯座位圖', trigger: 'change' }],
}
const uploadRef = ref(null)
const dialogVisible = ref(false) // 控制圖片預覽對話框
const dialogImageUrl = ref('') // 圖片預覽的 URL

// 判斷當前模式和表單修改狀態的響應式變數
const isUpdateMode = ref(false)
const isFormModified = ref(false)
// 儲存原始資料
let originalFormState = {}
// 新增一個變數來標記資料是否正在載入中，避免 watch 過早觸發
const isLoading = ref(true)

// 處理圖片預覽對話框關閉的函式
const handlePreviewDialogClosed = () => {
  // 這是一個臨時的解決方案，用於強制觸發組件更新
  const tempFileList = [...fileList.value]
  fileList.value = []
  nextTick(() => {
    fileList.value = tempFileList
  })
}

// 圖片上傳相關的事件處理函式
const handleImageChange = (file, uploadedFiles) => {
  // 檢查圖片大小是否超過 2MB
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB!')
    uploadRef.value.clearFiles() // 清除檔案
    fileList.value = [] // 清空檔案列表
    return false
  }
  fileList.value = uploadedFiles // 更新檔案列表
}

const handleImageRemove = (file, uploadedFiles) => {
  fileList.value = uploadedFiles // 移除圖片後更新檔案列表
}

const handleImagePreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url // 設置圖片 URL
  dialogVisible.value = true // 打開預覽對話框
}

const seatEditorDialogVisible = ref(false) // 控制座位編輯器對話框的顯示狀態

/**
 * @description 打開座位編輯對話框。
 */
const openSeatEditorDialog = () => {
  seatEditorDialogVisible.value = true // 顯示座位編輯對話框
  nextTick(() => {
    // 確保子組件 `seatEditorRef` 存在
    if (seatEditorRef.value) {
      // 呼叫子組件暴露出來的 `init` 方法，並傳入父組件已有的座位資料
      seatEditorRef.value.init(theaterForm.seats)
    }
  })
}

/**
 * @description 從座位編輯子組件中儲存座位資料。
 */
const saveSeats = () => {
  // 確保子組件 `seatEditorRef` 存在
  if (seatEditorRef.value) {
    // 呼叫子組件的 `saveSeats` 方法，它會回傳一個包含座位資料和總數的物件
    const { seats, totalSeats } = seatEditorRef.value.saveSeats()
    theaterForm.seats = seats // 將回傳的座位資料賦值給父組件的表單
    theaterForm.totalSeats = totalSeats // 更新總座位數
    ElMessage.success('座位資料已儲存！')
    seatEditorDialogVisible.value = false // 關閉座位編輯對話框
  }
}

/**
 * @description 設定原始表單狀態的函式。
 * 專門用於在資料載入後，儲存一個乾淨的原始狀態，避免 watch 監聽器誤判。
 */
const setOriginalState = () => {
  originalFormState = {
    theaterName: theaterForm.theaterName,
    theaterTypeId: theaterForm.theaterTypeId,
    totalSeats: theaterForm.totalSeats,
    seats: JSON.parse(JSON.stringify(theaterForm.seats)), // 深拷貝座位陣列
    theaterPhoto: theaterForm.theaterPhoto ? { ...theaterForm.theaterPhoto } : null, // 淺拷貝圖片物件
  }
  isFormModified.value = false // 載入完成，表示未修改
  isLoading.value = false // 標記載入完成
  console.log('原始狀態已儲存:', originalFormState)
}

const fetchTheaterData = async (theaterId) => {
  isLoading.value = true // 在開始載入資料前，標記為正在載入
  try {
    resetForm()
    const data = await theaterStore.fetchTheaterDetailById(theaterId)

    if (data) {
      console.log(data)
      const theaterBaseData = data.showTheatersResponseDTO
      const seatsData = data.seats

      theaterForm.theaterName = theaterBaseData.theaterName
      theaterForm.theaterTypeId = theaterBaseData.theaterTypeId
      theaterForm.totalSeats = theaterBaseData.totalSeats
      theaterForm.seats = seatsData

      if (theaterBaseData.theaterPhotoImgBase64) {
        const imageUrl = theaterBaseData.theaterPhotoImgBase64
        fileList.value = [
          {
            name: 'theater-photo',
            url: imageUrl,
            uid: new Date().getTime(),
            status: 'success',
          },
        ]
        theaterForm.theaterPhoto = { url: imageUrl, uid: new Date().getTime() }
      } else {
        fileList.value = []
        theaterForm.theaterPhoto = null
      }

      nextTick(() => {
        if (seatEditorRef.value) {
          seatEditorRef.value.init(seatsData)
        }
        setOriginalState() // 在所有資料填充後，再設定原始狀態
      })
    } else {
      ElMessage.error('找不到該影廳資料！')
      goBack()
    }
  } catch (error) {
    ElMessage.error('載入影廳資料時發生錯誤。')
    console.error('載入影廳錯誤:', error)
  }
}

const submitForm = async () => {
  theaterFormRef.value.validate(async (valid) => {
    if (valid) {
      let photoBase64 = null
      if (theaterForm.theaterPhoto && theaterForm.theaterPhoto.raw) {
        photoBase64 = await toBase64(theaterForm.theaterPhoto.raw)
      } else if (theaterForm.theaterPhoto && theaterForm.theaterPhoto.url) {
        // 如果是後端回傳的 base64 url，直接取字串
        const base64Parts = theaterForm.theaterPhoto.url.split(',')
        if (base64Parts.length > 1) {
          photoBase64 = base64Parts[1]
        }
      }

      const rawSeats = theaterForm.seats.slice()

      const newTheaterData = {
        theaterName: theaterForm.theaterName,
        theaterPhoto: photoBase64,
        theaterTypeId: theaterForm.theaterTypeId,
        totalSeats: theaterForm.totalSeats,
        seats: rawSeats,
      }
      console.log('要送出的影廳資料：', newTheaterData)

      try {
        const result = await theaterStore.createTheater(newTheaterData)
        if (result.success) {
          ElMessage.success('影廳新增成功！')
          goBack()
        } else {
          ElMessage.error(result.message)
        }
      } catch (error) {
        ElMessage.error('新增影廳時發生錯誤，請稍後再試。')
        console.error('新增影廳錯誤:', error)
      }
    } else {
      ElMessage.error('表單驗證失敗，請檢查輸入內容！')
      return false
    }
  })
}

const updateForm = async () => {
  console.log('更新影廳~')
  theaterFormRef.value.validate(async (valid) => {
    if (valid) {
      let photoBase64 = null
      if (theaterForm.theaterPhoto && theaterForm.theaterPhoto.raw) {
        photoBase64 = await toBase64(theaterForm.theaterPhoto.raw)
      } else if (theaterForm.theaterPhoto && theaterForm.theaterPhoto.url) {
        // 如果是後端回傳的 base64 url，直接取字串
        const base64Parts = theaterForm.theaterPhoto.url.split(',')
        if (base64Parts.length > 1) {
          photoBase64 = base64Parts[1]
        }
      }

      const rawSeats = theaterForm.seats.slice()

      const newTheaterData = {
        theaterId: route.params.id,
        theaterName: theaterForm.theaterName,
        theaterPhoto: photoBase64,
        theaterTypeId: theaterForm.theaterTypeId,
        totalSeats: theaterForm.totalSeats,
        seats: rawSeats,
      }
      console.log('要送出的影廳資料：', newTheaterData)

      try {
        const theaterId = route.params.id
        const result = await theaterStore.updateTheater(newTheaterData, theaterId)
        if (result.success) {
          ElMessage.success('影廳更新成功！')
          goBack()
        } else {
          ElMessage.error(result.message)
        }
      } catch (error) {
        ElMessage.error('新增影廳時發生錯誤，請稍後再試。')
        console.error('新增影廳錯誤:', error)
      }
    } else {
      ElMessage.error('表單驗證失敗，請檢查輸入內容！')
      return false
    }
  })
}

const toBase64 = (file) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result.split(',')[1])
    reader.onerror = (error) => reject(error)
  })

const goBack = () => {
  router.back()
}

const resetForm = () => {
  Object.assign(theaterForm, {
    theaterName: '',
    theaterPhoto: null,
    theaterTypeId: null,
    totalSeats: 0,
    seats: [],
  })

  fileList.value = []
  originalFormState = {}
  isFormModified.value = false
  isLoading.value = true // 重設時也將載入狀態設為 true

  if (theaterFormRef.value) {
    theaterFormRef.value.resetFields()
  }
}

watch(fileList, (newValue) => {
  if (newValue.length > 0) {
    // 這裡我們將整個物件賦值過去，方便後續比對
    // 儲存一個包含必要屬性的物件，用於後續比對
    theaterForm.theaterPhoto = {
      name: newValue[0].name,
      url: newValue[0].url,
      uid: newValue[0].uid,
      raw: newValue[0].raw, // 儲存原始檔案物件，用於上傳
    }
  } else {
    theaterForm.theaterPhoto = null
  }
})

watch(
  () => [route.name, route.params.id],
  ([newName, newId]) => {
    if (newName === 'AdminTheaterUpdate') {
      isUpdateMode.value = true
      fetchTheaterData(newId)
    } else if (newName === 'AdminTheaterAdd') {
      isUpdateMode.value = false
      resetForm()
    }
  },
  { immediate: true },
)

// 深度監聽 `theaterForm` 變化，以判斷表單是否被修改
watch(
  theaterForm,
  (newVal) => {
    // 新增判斷條件: 只有在更新模式且資料載入完成時才進行比對
    if (isUpdateMode.value && !isLoading.value) {
      const isNameModified = newVal.theaterName !== originalFormState.theaterName
      const isTypeModified = newVal.theaterTypeId !== originalFormState.theaterTypeId
      const isSeatsModified =
        JSON.stringify(newVal.seats) !== JSON.stringify(originalFormState.seats)

      let isPhotoModified = false
      // 處理圖片比對
      const newPhoto = newVal.theaterPhoto
      const originalPhoto = originalFormState.theaterPhoto

      if (!newPhoto && originalPhoto) {
        isPhotoModified = true // 從有圖片變成沒圖片
      } else if (newPhoto && !originalPhoto) {
        isPhotoModified = true // 從沒圖片變成有圖片
      } else if (newPhoto && originalPhoto) {
        // 檢查圖片的 URL 是否改變
        if (newPhoto.url !== originalPhoto.url) {
          isPhotoModified = true
        }
      }

      isFormModified.value = isNameModified || isTypeModified || isSeatsModified || isPhotoModified

      console.log('表單有修改嗎？', isFormModified.value)
      console.log('名稱修改:', isNameModified)
      console.log('類別修改:', isTypeModified)
      console.log('座位修改:', isSeatsModified)
      console.log('圖片修改:', isPhotoModified)
    }
  },
  { deep: true },
)
</script>

<style scoped>
/* 頁面最外層容器的樣式 */
.theater-editor-container {
  padding: 20px;
}
/* 卡片容器樣式 */
.editor-card {
  max-width: 800px; /* 最大寬度為 800px */
  margin: auto; /* 水平置中 */
}
/* 顯示座位數量的文字樣式 */
.seats-info {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}
/*
  隱藏多餘虛線框的 CSS 規則
  :deep() 是 Vue 的一個特殊選擇器，用於深度作用於子組件的樣式
  這裡的目的是隱藏 Element Plus 上傳組件中，當上傳一張圖片後，多餘的那個虛線框
*/
:deep(.el-upload-list--picture-card .el-upload-list__item + .el-upload--picture-card) {
  display: none !important;
}
</style>
