<template>
  <el-dialog
    :model-value="visible"
    :title="isEditMode ? '編輯會員等級' : '新增會員等級'"
    width="500px"
    @close="handleClose"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="等級名稱" prop="levelName">
        <el-input v-model="form.levelName" placeholder="請輸入等級名稱" />
      </el-form-item>

      <el-form-item label="升級門檻" prop="thresholdLowerBound">
        <el-input-number
          v-model="form.thresholdLowerBound"
          :min="0"
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="條件描述" prop="upgradeConditionDescription">
        <el-input
          v-model="form.upgradeConditionDescription"
          type="textarea"
          :rows="3"
          placeholder="請輸入升級條件的描述"
        />
      </el-form-item>

      <el-form-item label="等級圖示">
        <el-upload
          ref="uploadRef"
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="fileList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">儲存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  visible: Boolean,
  isEditMode: Boolean,
  levelData: Object,
})

const emit = defineEmits(['close', 'submit'])

const formRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const form = reactive({
  memberLevelId: null,
  levelName: '',
  thresholdLowerBound: 0,
  upgradeConditionDescription: '',
})
const iconFile = ref(null)

const rules = {
  levelName: [{ required: true, message: '請輸入等級名稱', trigger: 'blur' }],
  thresholdLowerBound: [{ required: true, message: '請輸入升級門檻', trigger: 'blur' }],
}

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // 重置狀態
      fileList.value = []
      iconFile.value = null

      if (props.isEditMode && props.levelData) {
        // 編輯模式：載入資料
        Object.assign(form, props.levelData)
        if (props.levelData.levelIcon) {
          fileList.value.push({
            name: 'existing-icon.png',
            url: `data:image/png;base64,${props.levelData.levelIcon}`,
          })
        }
      } else {
        // 新增模式：清空 form
        Object.assign(form, {
          memberLevelId: null,
          levelName: '',
          thresholdLowerBound: 0,
          upgradeConditionDescription: '',
        })
      }
    }
  },
)

const handleFileChange = (file) => {
  // 驗證檔案類型和大小
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上傳圖片檔案!')
    uploadRef.value?.clearFiles()
    return
  }
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB!')
    uploadRef.value?.clearFiles()
    return
  }
  iconFile.value = file.raw
}

const handleFileRemove = () => {
  iconFile.value = null
}

const handleClose = () => {
  emit('close')
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      emit('submit', { ...form }, iconFile.value)
    } else {
      ElMessage.error('請檢查表單輸入是否正確')
    }
  })
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
