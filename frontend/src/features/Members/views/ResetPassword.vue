<template>
  <div class="reset-password-page">
    <div class="form-container">
      <form class="reset-password-form" @submit.prevent="handleResetPassword">
        <h2 class="form-title">重設密碼</h2>
        <p class="form-subtitle">請為您的帳號設定一組新密碼。</p>

        <div class="input-group">
          <label for="newPassword">新密碼</label>
          <input type="password" id="newPassword" v-model="newPassword" placeholder="請輸入新密碼" required />
        </div>

        <div class="input-group">
          <label for="confirmPassword">確認新密碼</label>
          <input type="password" id="confirmPassword" v-model="confirmPassword" placeholder="請再次輸入新密碼" required />
        </div>

        <button type="submit" class="btn btn-primary" :disabled="isLoading">{{ isLoading ? '處理中...' : '確認重設' }}</button>

        <div class="back-to-login">
          <router-link to="/login">返回登入</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberAuthStore } from '@/stores/memberAuth';
import AuthService from '@/services/authService';
import { ElMessage } from 'element-plus';

const newPassword = ref('');
const confirmPassword = ref('');
const isLoading = ref(false);

const router = useRouter();
const memberAuthStore = useMemberAuthStore();

const emailForReset = ref('');

onMounted(() => {
  emailForReset.value = memberAuthStore.emailForPasswordReset;
  if (!emailForReset.value) {
    ElMessage.error('無效的操作，請先完成上一步驟。');
    router.push('/forgot-password');
  }
});

const handleResetPassword = async () => {
  if (!newPassword.value || !confirmPassword.value) {
    ElMessage.warning('請輸入並確認您的新密碼。');
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('兩次輸入的密碼不一致。');
    return;
  }

  isLoading.value = true;
  try {
    await AuthService.insecureResetPassword({
      email: emailForReset.value,
      newPassword: newPassword.value,
    });

    ElMessage.success('密碼已成功重設，請使用新密碼登入。');
    memberAuthStore.setEmailForReset(''); // Clear the email from the store
    router.push('/login');

  } catch (error) {
    const message = error.response?.data?.message || '重設密碼時發生錯誤，請稍後再試。';
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* Using the same styles as ForgotPassword.vue for consistency */
.reset-password-page {
  width: 100%;
  height: 100vh;
  background: url('@/assets/images/login-background.png') center/cover no-repeat;
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