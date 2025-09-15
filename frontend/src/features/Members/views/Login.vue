<template>
  <div class="login-page">
    <div class="login-form-container">
      <form class="login-form" @submit.prevent="handleLogin">
        <h2 class="form-title">會員登入</h2>

        <div class="input-group">
          <label for="email">電子郵件</label>
          <input type="email" id="email" v-model="email" placeholder="請輸入您的 Email" required />
        </div>

        <div class="input-group">
          <label for="password">密碼</label>
          <input
            type="password"
            id="password"
            v-model="password"
            placeholder="請輸入您的密碼"
            required
          />
        </div>

        <button type="submit" class="btn btn-login" :disabled="memberAuthStore.isLoading">
          {{ memberAuthStore.isLoading ? '登入中...' : '登入' }}
        </button>

        <div class="links">
          <a href="#" @click.prevent="handleForgotPassword">忘記密碼</a>
          <span class="divider">|</span>
          <a href="#" @click.prevent="handleRegister">立即註冊</a>
        </div>

        <div class="separator">
          <span class="line"></span>
          <span class="text">或</span>
          <span class="line"></span>
        </div>

        <!-- Google 登入按鈕的容器 -->
        <div id="google-signin-container" class="google-btn-container"></div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useMemberAuthStore } from '@/stores/memberAuth' // 引入我們新的 Store
import { useRouter } from 'vue-router' // 引入 useRouter 來跳轉頁面
import { ElMessage } from 'element-plus' // 引入 ElMessage

// 建立響應式變數來綁定表單輸入
const email = ref('')
const password = ref('')

// 獲取 MemberAuth Store 實例
const memberAuthStore = useMemberAuthStore()
// 獲取 router 實例
const router = useRouter()

// 登入按鈕的處理函式
const handleLogin = async () => {
  if (!email.value || !password.value) {
    ElMessage.warning('請輸入 Email 和密碼！')
    return
  }
  try {
    // 呼叫 Pinia Store 的登入 action
    await memberAuthStore.login({
      email: email.value,
      password: password.value,
    })
    // 登入成功後的跳轉邏輯已在 store 中處理
    router.push({ name: 'Home' })
  } catch (error) {
    // 錯誤提示已由 Axios 攔截器和 Store 處理，這裡可以不用做任何事
    console.error('Login page caught an error:', error)
  }
}

// 導向到註冊頁面
const handleRegister = () => {
  console.log('導向到註冊頁面')
  router.push('/register') // 假設你的註冊頁面路由是 /register
}

// 導向到忘記密碼頁面
const handleForgotPassword = () => {
  console.log('導向到忘記密碼頁面')
  router.push('/forgot-password')
}

// Google 登入成功後的回呼函式
const handleGoogleCredentialResponse = async (response) => {
  try {
    // 成功後的跳轉和提示已在 store 中處理
    await memberAuthStore.googleLogin(response.credential)
  } catch (error) {
    console.error('Google 登入失敗:', error)
    ElMessage.error('Google 登入失敗，請稍後再試。')
  }
}

onMounted(() => {
  if (typeof google !== 'undefined') {
    google.accounts.id.initialize({
      client_id: '721650577954-3d28d0hu4r31lutmi1op5j5tloougota.apps.googleusercontent.com',
      callback: handleGoogleCredentialResponse,
    })

    google.accounts.id.renderButton(document.getElementById('google-signin-container'), {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'signin_with',
      shape: 'rectangular',
      logo_alignment: 'left',
      width: '300',
    })
    // google.accounts.id.prompt() // 可選：啟用 One Tap 自動登入提示
  }
})
</script>

<style scoped>
/* 你的樣式非常棒，無需修改 */
/* ... 所有 style 內容 ... */
.login-page {
  width: 100%;
  height: 100vh;
  background: url('@/assets/images/login-background.png') center/100% 100% no-repeat;
  display: flex;
  justify-content: flex-end; /* 將內容推到右邊 */
  align-items: center;
}
.login-form-container {
  background-color: rgba(30, 30, 30, 0.577);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 40px;
  padding-left: 80px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  width: 100%;
  max-width: 420px;
  margin-right: 11.5%;
}
.form-title {
  color: #ffffff;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 2rem;
  font-weight: 600;
}
.input-group {
  margin-bottom: 1.5rem;
}
.input-group label {
  display: block;
  color: #cccccc;
  margin-bottom: 8px;
  font-size: 0.9rem;
}
.input-group input {
  width: 80%;
  padding: 12px 15px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #ffffff;
  font-size: 1rem;
  transition: all 0.3s ease;
}
.input-group input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.3);
}
.input-group input::placeholder {
  color: #888888;
}
.btn {
  width: 87.5%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.btn-login {
  background-color: #4a90e2;
  color: white;
  margin-top: 1rem; /* 稍微縮小與密碼框的間距 */
}
.btn-login:hover {
  background-color: #357abd;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.4);
}
.btn-google {
  background-color: #ffffff;
  color: #333333;
}
.btn-google:hover {
  background-color: #f1f1f1;
}
.google-icon {
  width: 20px;
  height: 20px;
}
.links {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1.5rem;
  gap: 15px;
  width: 87.5%;
}
.links a {
  color: #cccccc;
  text-decoration: none;
  font-size: 0.9rem;
}
.links a:hover {
  color: #ffffff;
  text-decoration: underline;
}
.links .divider {
  color: #555555;
}
.separator {
  width: 87.5%;
  display: flex;
  align-items: center;
  text-align: center;
  color: #888888;
  margin: 2rem 0;
}
.separator .line {
  flex-grow: 1;
  height: 1px;
  background-color: #555555;
}
.separator .text {
  padding: 0 15px;
}

.google-btn-container {
  width: 87.5%;
  margin: 0;
  display: flex;
  justify-content: center;
  padding-top: 1rem;
}
</style>
