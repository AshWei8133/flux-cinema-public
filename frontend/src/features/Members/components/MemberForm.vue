<template>
  <form @submit.prevent="handleSubmit" class="form-container">
    <div class="form-group">
      <label for="username">帳號</label>
      <input id="username" v-model="form.username" placeholder="帳號" required />
    </div>
    <div class="form-group" v-if="!isThirdParty">
      <label for="password">密碼</label>
      <input
        id="password"
        v-model="form.password"
        type="password"
        :placeholder="editMode ? '留空則不修改密碼' : '密碼'"
        :required="!editMode"
      />
    </div>
    <div class="form-group">
      <label for="email">Email</label>
      <input id="email" v-model="form.email" type="email" placeholder="Email" required />
    </div>
    <div class="form-group">
      <label for="gender">性別</label>
      <select id="gender" v-model="form.gender">
        <option value="M">男</option>
        <option value="F">女</option>
      </select>
    </div>
    <div class="form-group">
      <label for="birthDate">生日</label>
      <input id="birthDate" v-model="form.birthDate" type="date" />
    </div>
    <div class="form-group">
      <label for="phone">電話</label>
      <input id="phone" v-model="form.phone" placeholder="電話" required />
    </div>
    <div class="form-group">
      <label for="loginMethod">登入方式</label>
      <select id="loginMethod" v-model="form.loginMethod">
        <option value="email">Email</option>
        <option value="google">Google</option>
      </select>
    </div>
    <div class="form-group" v-if="form.loginMethod !== 'email'">
      <label for="thirdPartyLoginId">第三方 ID</label>
      <input id="thirdPartyLoginId" v-model="form.thirdPartyLoginId" placeholder="第三方 ID" />
    </div>
    <!-- <div class="form-group">
      <label for="memberPoints">會員點數</label>
      <input
        id="memberPoints"
        v-model.number="form.memberPoints"
        type="number"
        min="0"
        placeholder="會員點數"
      />
    </div> -->

    <div
      v-if="editMode && props.member && props.member.memberId && !imageError && !newAvatarPreview"
      class="image-preview"
    >
      <label>目前頭像</label>
      <img
        :src="`/api/admin/members/photo/${props.member.memberId}`"
        alt="Current Avatar"
        class="avatar-image"
        @error="onImageError"
      />
    </div>

    <div class="form-group">
      <label for="avatar">{{ editMode ? '更換頭像' : '上傳頭像' }}</label>
      <input id="avatar" type="file" @change="handleFileChange" accept="image/*" />
    </div>

    <div v-if="newAvatarPreview" class="image-preview">
      <label>新頭像預覽</label>
      <img :src="newAvatarPreview" class="avatar-image" />
    </div>

    <div class="button-group">
      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '處理中...' : editMode ? '更新' : '新增' }}
      </button>
      <button type="button" class="btn-cancel" @click="$emit('cancel')">取消</button>
    </div>
  </form>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  member: Object,
  editMode: Boolean,
  isSubmitting: Boolean, // 新增 prop 用於控制按鈕狀態
})
const emit = defineEmits(['submit', 'cancel'])

const form = ref({})
const file = ref(null)

const imageError = ref(false)
const newAvatarPreview = ref(null)

const onImageError = () => {
  imageError.value = true
}

// 監聽 props.member 的變化，以初始化表單
// 這個 watch 很重要，它確保了新增時表單是空的，編輯時能正確載入資料
watch(
  () => props.member,
  (newMember) => {
    imageError.value = false
    newAvatarPreview.value = null
    if (props.editMode && newMember) {
      // 編輯模式：從 props 載入資料
      // 日期格式需要轉換為 YYYY-MM-DD
      const birthDate = newMember.birthDate
        ? new Date(newMember.birthDate).toISOString().split('T')[0]
        : ''
      form.value = { ...newMember, password: '', birthDate }

      if (form.value.memberPoints == null) form.value.memberPoints = 0
    } else {
      // 新增模式：重設為初始狀態
      form.value = {
        username: '',
        password: '',
        email: '',
        gender: 'M',
        birthDate: '',
        phone: '',
        loginMethod: 'email',
        thirdPartyLoginId: '',
        memberPoints: 0,
        avatar: null,
      }
    }
  },
  { immediate: true, deep: true },
)

// 上傳檔案
const handleFileChange = (e) => {
  const selectedFile = e.target.files[0]
  if (selectedFile) {
    file.value = selectedFile

    const reader = new FileReader()
    reader.onload = (event) => {
      newAvatarPreview.value = event.target.result
    }
    reader.readAsDataURL(selectedFile)
    imageError.value = false
  }
}
// 提交表單
const handleSubmit = () => emit('submit', form.value, file.value)
// 計算是否為第三方登入
const isThirdParty = computed(() => form.value.loginMethod !== 'email')
</script>

<style scoped>
.form-container {
  display: flex;
  flex-direction: column;
  gap: 16px; /* Increased gap for better spacing */
  max-width: 500px; /* Increased max-width */
  margin: 0 auto;
  padding: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #374151; /* Darker text for better readability */
}

.form-container input,
.form-container select {
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition:
    border 0.2s,
    box-shadow 0.2s;
}

.form-container input:focus,
.form-container select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

.image-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center; /* Center the image */
  margin-bottom: 1rem; /* Add some space below the preview */
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%; /* Make it circular */
  object-fit: cover; /* Ensure the image covers the area without distortion */
  border: 2px solid #e5e7eb; /* Add a light border */
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 16px; /* Add margin to separate from the form fields */
}

button {
  flex: 1;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

button[type='submit'] {
  background-color: #3b82f6;
  color: white;
}

button[type='submit']:hover:not(:disabled) {
  background-color: #2563eb;
}

button[type='submit']:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-cancel {
  background-color: #e5e7eb;
  color: #374151;
}

.btn-cancel:hover {
  background-color: #d1d5db;
}
</style>
