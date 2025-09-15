<template>
  <div class="forgot-password-page">
    <div class="form-container">
      <form class="forgot-password-form" @submit.prevent="handleNextStep">
        <h2 class="form-title">忘記密碼</h2>
        <p class="form-subtitle">請輸入您的電子郵件地址與電話號碼，我們將引導您重設密碼。</p>

        <div class="input-group">
          <label for="email">電子郵件</label>
          <input
            type="email"
            id="email"
            v-model="email"
            placeholder="請輸入您註冊的 Email"
            required
          />
        </div>

        <div class="input-group">
          <label for="phone">電話號碼</label>
          <input
            type="tel"
            id="phone"
            v-model="phone"
            placeholder="請輸入您註冊的電話號碼"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary" :disabled="isLoading">
          {{ isLoading ? '驗證中...' : '下一步' }}
        </button>

        <div class="back-to-login">
          <router-link to="/login">返回登入</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberAuthStore } from '@/stores/memberAuth'
import { ElMessage } from 'element-plus'

const email = ref('')
const phone = ref('')
const router = useRouter()
const memberAuthStore = useMemberAuthStore()

const isLoading = computed(() => memberAuthStore.isLoading)

const handleNextStep = async () => {
  if (!email.value || !phone.value) {
    ElMessage.warning('請輸入電子郵件地址與電話號碼。')
    return
  }

  try {
    await memberAuthStore.verifyEmailAndPhoneForPasswordReset({
      email: email.value,
      phone: phone.value,
    })

    ElMessage.success('驗證成功，請重設您的密碼。')
    router.push('/reset-password')
  } catch (error) {
    if (error.responser && error.response.status === 404) {
      ElMessage.error('電子郵件或電話號碼不正確。')
    } else {
      ElMessage.error(error.message || '驗證時發生錯誤，請稍後再試。')
    }
  }
}
</script>

<style scoped>
.forgot-password-page {
  width: 100%;
  height: 100vh;
  background: url('@/assets/images/login-background.png') center/100% 100% no-repeat;
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-container {
  background-color: rgba(30, 30, 30, 0.85);
  backdrop-filter: blur(10px);
  padding: 40px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  width: 100%;
  max-width: 450px;
}

.form-title {
  color: #ffffff;
  text-align: center;
  margin-bottom: 1rem;
  font-size: 2rem;
  font-weight: 600;
}

.form-subtitle {
  color: #cccccc;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1rem;
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
  width: 100%;
  padding: 12px 15px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #ffffff;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.3);
}

.btn-primary {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  background-color: #4a90e2;
  color: white;
  transition: background-color 0.3s ease;
}

.btn-primary:hover {
  background-color: #357abd;
}

.back-to-login {
  text-align: center;
  margin-top: 1.5rem;
}

.back-to-login a {
  color: #cccccc;
  text-decoration: none;
  font-size: 0.9rem;
}

.back-to-login a:hover {
  color: #ffffff;
  text-decoration: underline;
}
</style>
