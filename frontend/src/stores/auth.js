// 引入已配置的 axios 實例，用於發送 HTTP 請求
import AuthService from '@/services/authService'
// 引入 Element Plus 的訊息提示組件
import { ElMessage } from 'element-plus'
// 引入 Pinia 的 defineStore 函式
import { defineStore } from 'pinia'
// 引入 Vue 的響應式 API
import { computed, ref } from 'vue'
// 引入 Vue Router 實例
import router from '@/router'
import httpClient from '@/services/api'

/**
 * 定義並導出 Pinia 管理員認證 Store。
 */
export const useAuthStore = defineStore(
  'auth',
  () => {
    // --- 數據 (State) ---
    const isLoading = ref(false)
    const error = ref(null)
    const token = ref(null)

    // --- 計算屬性 (Getters) ---
    const isAuthenticated = computed(() => !!token.value)

    // --- 動作 (Actions) ---

    /*
     * 登入操作 (保持不變)
     */
    const login = async (loginData) => {
      isLoading.value = true
      error.value = null
      try {
        const loginResponse = await AuthService.login(loginData)
        if (loginResponse && loginResponse.token) {
          token.value = loginResponse.token
          httpClient.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.token}`
          ElMessage.success('登入成功!')
          await router.push('/admin')
        } else {
          // 登入失敗的處理
          error.value = '帳號或密碼錯誤，請重新輸入。'
          ElMessage.error(error.value)
          token.value = null
        }
      } catch (err) {
        // 捕獲網路或其他異常錯誤
        error.value = err.message || '登入請求失敗，請稍後再試。'
        ElMessage.error(error.value)
        token.value = null
      } finally {
        isLoading.value = false
      }
    }

    /**
     * ✅【最終修正版】登出操作
     */
    const logout = () => {
      // 1. 將 Pinia store 中的 token 狀態清空
      token.value = null

      // 2. 【核心】直接、手動、明確地從 localStorage 中「移除」整個 key。
      //    這指令的權限最高，能確保無論插件在做什麼，這個 key 都會被徹底刪除。
      localStorage.removeItem('admin_jwt_token')

      // 3. 清除 axios 實例中儲存的 Authorization 標頭
      delete httpClient.defaults.headers.common['Authorization']

      ElMessage.info('您已登出。')
      router.push('/admin/login')
    }

    // 返回暴露給組件的狀態和動作
    return {
      isAuthenticated,
      isLoading,
      error,
      login,
      logout,
      token,
    }
  },
  {
    // ⭐⭐⭐ 我們不再需要複雜的 serializer，回歸最簡單的設定 ⭐⭐⭐
    persist: {
      key: 'admin_jwt_token',
      paths: ['token'],
    },
  },
)