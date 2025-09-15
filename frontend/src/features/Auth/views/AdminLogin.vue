<template>
  <div class="admin-login-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>管理員登入</span>
        </div>
      </template>
      <!-- 傳遞 isLoading 狀態給 LoginForm，並監聽 submit 事件 -->
      <LoginForm :isLoading="authStore.isLoading" @submit="handleLogin" />
    </el-card>
  </div>
</template>

<script setup>
// 引入通用 LoginForm 組件
import LoginForm from '@/components/LoginForm.vue'
// 引入 Auth Pinia Store，用於管理認證狀態
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
// 引入 Vue Router 的 useRouter 函式
import { useRouter } from 'vue-router'

// 獲取 Auth Pinia Store 實例
const authStore = useAuthStore()
// 獲取 Vue Router 實例
const router = useRouter()

const handleLogin = async (credentials) => {
  try {
    // 呼叫 Pinia Store 的登入 action
    await authStore.login(credentials)
    // 登入成功後，authStore.login() 內部會自行跳轉到 /admin/dashboard
  } catch (error) {
    // 捕獲並記錄登入錯誤
    console.error('AdminLogin.vue 捕獲到登入錯誤:', error)
    // 具體的錯誤提示已由 Pinia Store 中的 ElMessage 處理，這裡無需重複提示
  }
}

// 在組件建立時檢查是否已登入。
// 在組件掛載時檢查登入狀態
onMounted(() => {
  // Pinia Plugin Persistedstate 會自動還原狀態
  // 因此，只要 authStore.isAuthenticated 為 true，就直接跳轉
  if (authStore.isAuthenticated) {
    router.replace('/admin')
  }
})
</script>

<style scoped>
.admin-login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5; /* 輕微的背景色 */
}

.login-card {
  width: 400px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 輕微的陰影 */
  border-radius: 8px; /* 圓角 */
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}
</style>
