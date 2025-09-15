<template>
  <div class="register-page">
    <!-- Modal 卡片 -->
    <div class="register-card">
      <h2 class="title">註冊 Flux 會員</h2>
      <p class="subtitle">立即加入，享受專屬電影時光</p>

      <form @submit.prevent="handleSubmit">
        <div class="form-grid">
          <!-- 帳號 -->
          <div class="form-field">
            <label class="form-label">帳號</label>
            <InputField placeholder="請輸入您的帳號" v-model="formData.username" required />
          </div>

          <!-- Email -->
          <div class="form-field">
            <label class="form-label">Email</label>
            <InputField
              placeholder="請輸入您的 Email"
              type="email"
              v-model="formData.email"
              required
            />
          </div>

          <!-- 密碼 -->
          <div class="form-field">
            <label class="form-label">密碼</label>
            <InputField
              placeholder="請設定您的密碼"
              type="password"
              v-model="formData.password"
              required
            />
          </div>

          <!-- 確認密碼 -->
          <div class="form-field">
            <label class="form-label">確認密碼</label>
            <InputField
              placeholder="請再次輸入密碼"
              type="password"
              v-model="confirmPassword"
              required
            />
          </div>

          <!-- 手機 -->
          <div class="form-field">
            <label class="form-label">手機</label>
            <InputField placeholder="請輸入您的手機號碼" v-model="formData.phone" required />
          </div>

          <!-- 生日 -->
          <div class="form-field">
            <label class="form-label">生日</label>
            <InputField type="date" v-model="formData.birthDate" required />
          </div>
        </div>

        <div class="gender-select">
          <label class="form-label">性別</label>
          <div class="options">
            <label><input type="radio" v-model="formData.gender" value="M" /> 男</label>
            <label><input type="radio" v-model="formData.gender" value="F" /> 女</label>
          </div>
        </div>

        <div v-if="error" class="error-message">{{ error }}</div>

        <div class="action-buttons">
          <button type="submit" class="submit-btn" :disabled="isLoading">
            {{ isLoading ? '註冊中...' : '完成註冊' }}
          </button>
          <button type="button" class="cancel-btn" @click="handleCancel">取消</button>
        </div>
      </form>

      <div class="login-link">已經有帳號了？ <router-link to="/login">立即登入</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberAuthStore } from '@/stores/memberAuth'
import InputField from '@/components/InputField.vue'

const router = useRouter()
const memberAuthStore = useMemberAuthStore()

const formData = ref({
  username: '',
  email: '',
  password: '',
  phone: '',
  birthDate: '',
  gender: 'M',
})

const confirmPassword = ref('')
const error = ref(null)
const isLoading = ref(false)

const handleSubmit = async () => {
  error.value = null
  if (formData.value.password !== confirmPassword.value) {
    error.value = '密碼與確認密碼不相符'
    return
  }

  isLoading.value = true
  try {
    await memberAuthStore.register(formData.value)
    alert('註冊成功！將為您導向登入頁面。')
    router.push('/login')
  } catch (err) {
    error.value = err.message || '註冊失敗，請檢查您的輸入。'
  } finally {
    isLoading.value = false
  }
}

const handleCancel = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  background: url('@/assets/images/login-background.png') center/100% 100% no-repeat;
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 100%;
  max-width: 550px;
  padding: 40px;
  background-color: rgba(30, 30, 30, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  color: #ffffff;
}

.title {
  font-size: 28px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: #cccccc;
  text-align: center;
  margin-bottom: 30px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #cccccc;
}

.gender-select {
  margin-bottom: 20px;
}

.options {
  display: flex;
  gap: 20px;
  align-items: center;
}

.error-message {
  color: #ef4444;
  background-color: #fee2e2;
  border: 1px solid #ef4444;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
  text-align: center;
  color: #000; /* Error text should be readable */
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.submit-btn,
.cancel-btn {
  width: 125px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn {
  background-color: #e50914;
}

.cancel-btn {
  background-color: #6b7280; /* Gray color for cancel button */
}

.submit-btn:hover:not(:disabled) {
  background-color: #c40812;
}

.cancel-btn:hover {
  background-color: #4b5563;
}

.submit-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  color: #cccccc;
}

.login-link a {
  color: #e50914;
  font-weight: bold;
  text-decoration: none;
}

/* Making InputField component theme-aware */
:deep(.input-field) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #ffffff !important;
}

:deep(.input-field:focus) {
  border-color: #e50914 !important;
  box-shadow: 0 0 0 3px rgba(229, 9, 20, 0.3) !important;
}

:deep(.input-field::placeholder) {
  color: #888888 !important;
}
</style>
