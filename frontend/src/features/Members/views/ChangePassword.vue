<template>
  <div class="change-password-wrapper">
    <div class="change-password-container">
      <h1>修改密碼</h1>
      <form @submit.prevent="handlePasswordChange" class="password-form">
        <div class="form-group">
          <label for="oldPassword">舊密碼</label>
          <input
            type="password"
            id="oldPassword"
            v-model="passwordData.oldPassword"
            class="form-control"
            required
            placeholder="請輸入您的舊密碼"
          />
        </div>
        <div class="form-group">
          <label for="newPassword">新密碼</label>
          <input
            type="password"
            id="newPassword"
            v-model="passwordData.newPassword"
            class="form-control"
            required
            placeholder="請設定您的新密碼"
          />
        </div>
        <div class="form-group">
          <label for="confirmNewPassword">確認新密碼</label>
          <input
            type="password"
            id="confirmNewPassword"
            v-model="passwordData.confirmNewPassword"
            class="form-control"
            required
            placeholder="請再次輸入新密碼"
          />
        </div>
        <div class="button-group">
          <button type="submit" :disabled="isChangingPassword" class="save-button">
            {{ isChangingPassword ? '儲存中...' : '確認修改' }}
          </button>
          <button type="button" @click="goBack" class="cancel-button">返回</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePublicMemberProfileStore } from '../store/usePublicMemberProfileStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const profileStore = usePublicMemberProfileStore()

const { isChangingPassword, error } = storeToRefs(profileStore)
const { changePassword } = profileStore

const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
})

const handlePasswordChange = async () => {
  if (passwordData.newPassword !== passwordData.confirmNewPassword) {
    ElMessage.error('新密碼與確認密碼不相符')
    return
  }
  if (!passwordData.oldPassword || !passwordData.newPassword) {
    ElMessage.error('請填寫所有欄位')
    return
  }

  try {
    const response = await changePassword({
      oldPassword: passwordData.oldPassword,
      newPassword: passwordData.newPassword,
    })
    ElMessage.success(response || '密碼變更成功！')
    goBack()
  } catch (err) {
    ElMessage.error(error.value || '密碼變更失敗')
  }
}

const goBack = () => {
  router.push('/member/profile')
}
</script>

<style scoped>
.change-password-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #121212;
  padding: 2rem;
}

.change-password-container {
  width: 100%;
  max-width: 500px;
  background-color: #1a1a1a;
  padding: 2rem 3rem;
  border-radius: 8px;
  border: 1px solid #333;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7);
}

h1 {
  font-family: 'Inter', sans-serif;
  color: #fff;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #aaa;
  font-family: 'Inter', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  background-color: #333;
  border: 1px solid #555;
  border-radius: 4px;
  color: #fff;
  font-size: 1rem;
  transition:
    border-color 0.3s,
    box-shadow 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 10px rgba(74, 144, 226, 0.3);
}

.form-control::placeholder {
  color: #777;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
  gap: 1rem;
}

.button-group button {
  flex: 1;
  border: none;
  border-radius: 8px;
  padding: 12px 20px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
}

.save-button {
  background-color: #4a90e2;
  color: white;
}

.save-button:hover {
  background-color: #357abd;
}

.save-button:disabled {
  background-color: #555;
  cursor: not-allowed;
}

.cancel-button {
  background-color: #888;
  color: white;
}

.cancel-button:hover {
  background-color: #666;
}
</style>
