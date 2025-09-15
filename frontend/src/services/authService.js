// authservice.js

// 引入已配置的 axios 實例，用於發送 HTTP 請求
import httpClient from './api'

// ------------------ 管理員 Admin ------------------
const AuthService = {
  async login(loginData) {
    try {
      // 發送 POST 請求到 'admin/login' 端點
      const response = await httpClient.post('admin/login', {
        adminName: loginData.adminName,
        password: loginData.password,
      })
      // 返回後端響應數據
      return response
    } catch (error) {
      // 登入失敗時，直接拋出錯誤，讓 Pinia Store 進行統一處理
      throw error
    }
  },

  // ------------------ 會員 Member ------------------
  /**
   * 會員登入
   * @param {object} loginData - 包含 email 和 password
   */
  async memberLogin(loginData) {
    try {
      // 發送 POST 請求到後端 /member/login
      const response = await httpClient.post('/member/login', {
        email: loginData.email,
        password: loginData.password,
      })

      const { token, memberInfo } = response

      // 存入 localStorage，改成 member_jwt_token，且存成 JSON
      localStorage.setItem('member_jwt_token', JSON.stringify({ token, memberInfo }))

      return response // 成功時，攔截器會自動加 Authorization header
    } catch (error) {
      throw error // 失敗時拋出
    }
  },

  /**
   * 會員註冊
   * @param {object} registerData - 包含所有註冊欄位的物件
   */
  async memberRegister(registerData) {
    try {
      // 發送 POST 請求到 '/member/register'
      // 我們後端是用 JSON 格式接收 (包含 Base64)，所以可以直接傳物件
      const response = await httpClient.post('/member/register', registerData)
      return response
    } catch (error) {
      throw error
    }
  },

  /**
   * 驗證 Email 和電話號碼是否存在
   * @param {object} payload - 包含 email 和 phone
   */
  async verifyEmailAndPhone(payload) {
    try {
      return await httpClient.post('/member/verify-identity', payload);
    } catch (error) {
      throw error;
    }
  },

  /**
   * 重設密碼
   * @param {*} resetData
   * @returns
   */
  insecureResetPassword(resetData) {
    return httpClient.post('/member/reset-password-insecure', resetData)
  },
  // 判斷是否登入
  isLoggedIn() {
    return !!localStorage.getItem('member_jwt_token') // 有 token 就視為已登入
  },

  getToken() {
    return localStorage.getItem('member_jwt_token')
  },

  /**
   * 會員 Google 登入
   * @param {*} credential 
   * @returns 
   */
  async memberGoogleLogin(credential) {
    try {
      const response = await httpClient.post('/member/google-login', { credential });
      // 假設後端驗證成功後，回傳的格式與一般登入相同
      const { token, memberInfo } = response;
      // 存入 localStorage  
      localStorage.setItem('member_jwt_token', JSON.stringify({ token, memberInfo }));
      return response;
    } catch (error) {
      throw error;
    }
  },
}

// 暴露 AuthService 對象給其他組件使用
export default AuthService
