import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import AuthService from '@/services/authService'
import router from '@/router'
import { ElMessage } from 'element-plus'

/**
 * 定義並導出 Pinia 會員認證 Store。
 * 這個 Store 用於管理前台會員的認證狀態，包括 token、會員資訊、載入狀態等。
 */
export const useMemberAuthStore = defineStore(
  'memberAuth',
  () => {
    // --- 數據 (State) ---
    // 會員的 JWT token
    const token = ref(null)
    // 登入成功後，後端回傳的會員基本資訊
    const memberInfo = ref(null)
    // 載入狀態，表示登入請求是否正在進行中
    const isLoading = ref(false)
    // 錯誤訊息
    const error = ref(null)
    // 用於在忘記密碼流程中暫存電子郵件
    const emailForPasswordReset = ref('')

    // --- 計算屬性 (Getters) ---
    // 判斷會員是否已登入 (更嚴謹的判斷：token 和會員資訊都必須存在)
    const isAuthenticated = computed(() => !!token.value && !!memberInfo.value)

    // --- 動作 (Actions) ---

    /**
     * 【新增】初始化會員認證狀態
     */
    function initializeAuth() {
      const storedToken = localStorage.getItem('member_jwt_token')
      const storedMemberInfo = localStorage.getItem('member_info')
      if (storedToken && storedMemberInfo) {
        token.value = storedToken
        memberInfo.value = JSON.parse(storedMemberInfo)
        // 注意：這裡的 token 設置邏輯需要根據您的 api.js 攔截器來決定
        // 如果攔截器會自動處理，這裡可能不需要手動設定 httpClient 的標頭
      }
    }

    /**
     * 會員登入操作
     * @param {object} credentials - 包含 email 和 password 的物件
     */
    const login = async (credentials) => {
      isLoading.value = true
      error.value = null
      try {
        // 調用 AuthService 的 memberLogin 方法
        const data = await AuthService.memberLogin(credentials)

        // 後端成功回傳 { token, memberInfo }
        token.value = data.token
        memberInfo.value = data.memberInfo

        // pinia-plugin-persistedstate 會自動將 token 和 memberInfo 存入 localStorage

        ElMessage.success(`歡迎回來, ${data.memberInfo.username}！`)

        // 登入成功後跳轉到首頁
        await router.push('/')
      } catch (err) {
        // Axios 攔截器已經彈出錯誤訊息，這裡只需記錄並清空狀態
        error.value = err.message || '登入請求失敗。'
        token.value = null
        memberInfo.value = null
        throw err // 將錯誤再次拋出，以便 Vue 組件可以捕獲
      } finally {
        isLoading.value = false
      }
    }

    /**
     * 會員註冊操作    
     * @param  {object} memberData - 包含註冊表單所有欄位的物件 
     */
    const register = async (memberData) => {
      isLoading.value = true
      error.value = null

      try {
        // 調用 AuthService 中已存在的 memberRegister 方法
        // 註冊成功後，可以選擇顯示成功訊息，但通常不直接登入 
        // 讓使用者去登入頁面重新登入，是比較常見且安全的流程
        await AuthService.memberRegister(memberData)
      } catch (err) {
        // 將錯誤再次拋出，以便註冊組件可以捕獲並顯示
        error.value = err.message || '註冊失敗，請檢查您的資料。'
        throw err
      } finally {
        isLoading.value = false
      }
    }

    /**
     * 會員 Google 登入操作
     * @param {*} credential 
     */
    const googleLogin = async (credential) => {
      isLoading.value = true
      error.value = null

      try {
        // 調用 AuthService 的 memberGoogleLogin 方法
        const data = await AuthService.memberGoogleLogin(credential)
        // 後端成功回傳 { token, memberInfo } 
        token.value = data.token
        memberInfo.value = data.memberInfo

        ElMessage.success(`透過 Google 登入成功, 歡迎您 ${data.memberInfo.username}！`)
        // 登入成功後跳轉到首頁
        await router.push('/')
      } catch (err) {
        error.value = err.message || 'Google登入失敗。'
        token.value = null
        memberInfo.value = null
        throw err
      } finally {
        isLoading.value = false
      }
    }

    /**
     * 會員登出操作
     */
    const logout = () => {
      // 清空所有會員相關狀態
      token.value = null
      memberInfo.value = null
      error.value = null

      // pinia-plugin-persistedstate 會自動同步並清空 localStorage 中的資料

      ElMessage.info('您已成功登出。')
      router.push('/') // 登出後停在首頁
    }

    /**
     * 設置用於密碼重設的電子郵件
     * @param {*} email 
     */
    const setEmailForReset = (email) => {
      emailForPasswordReset.value = email
    }

    /**
     * 驗證 Email 與電話是否存在，用於忘記密碼流程
     * @param {object} payload - 包含 email 和 phone 的物件
     */
    const verifyEmailAndPhoneForPasswordReset = async (payload) => {
      isLoading.value = true;
      error.value = null;
      try {
        // 呼叫 AuthService 檢查 Email 和 Phone
        await AuthService.verifyEmailAndPhone(payload);
        // 如果成功，則將 email 存入 store 中以供重設密碼頁面使用
        setEmailForReset(payload.email);
      } catch (err) {
        error.value = err.message || 'Email 與電話驗證失敗';
        throw err;
      } finally {
        isLoading.value = false;
      }
    }

    // 返回暴露給組件的狀態和動作
    return {
      token,
      memberInfo,
      isLoading,
      error,
      isAuthenticated,
      login,
      googleLogin,
      logout,
      initializeAuth,
      register,
      emailForPasswordReset,
      setEmailForReset,
      verifyEmailAndPhoneForPasswordReset
    }
  },
  {
    // --- Pinia Plugin Persistedstate 的設定 ---
    persist: {
      key: 'member_jwt_token',
      paths: ['token', 'memberInfo'], // 我們可以明確指定只持久化 token 和 memberInfo
    },
  },
)
