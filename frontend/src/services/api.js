import axios from 'axios'
import { ElMessage } from 'element-plus' // 引入 Element Plus 的訊息提示組件

// ====== 設定 API 基礎 URL ======
// 使用環境變數，使在不同部署環境（開發/測試/生產）可以切換後端地址
// 在 Vite 專案中，通常在 .env 文件中定義 VITE_APP_API_BASE_URL
// 例如：.env.development 中 VITE_APP_API_BASE_URL=http://localhost:8888/api
//      .env.production 中 VITE_APP_API_BASE_URL=https://yourdomain.com/api
const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || 'http://localhost:8888/api'

// ====== 創建 Axios 實例 ======
const httpClient = axios.create({
  baseURL: API_BASE_URL, // 所有請求都會以此為基礎路徑
  timeout: 100000, // 請求超時時間 (毫秒)，這裡設定為 10 秒
  headers: {
    'Content-Type': 'application/json', // 預設請求頭，告知伺服器發送 JSON 數據
  },
  withCredentials: true, // 允許跨域請求攜帶憑證（如 Cookie），與後端 WebConfig 的 allowCredentials(true) 對應
})

// ====== 請求攔截器 (Request Interceptors) ======
// 在每次發送請求之前執行，用於添加通用邏輯
httpClient.interceptors.request.use(
  (config) => {
    // 從 localStorage 中取得 Token
    let token = null

    // 檢查請求的url
    if (config.url && config.url.startsWith('/admin')) {
      // 如果是發往 /admin 的請求，就讀取管理員 token
      const adminAuthDataString = localStorage.getItem('admin_jwt_token')
      if (adminAuthDataString) {
        try {
          // 2. 將 JSON 字串解析成物件
          const adminAuthData = JSON.parse(adminAuthDataString)
          // 3. 從物件中，只取出真正的 "信件" (token 的值)
          token = adminAuthData.token
          console.log('[HTTP Request] Admin token extracted and attached.')
        } catch (e) {
          console.error('無法從 localStorage 解析管理員認證資料', e)
        }
      }
    } else {
      // 否則，讀取會員 token，並進行解析
      const memberAuthDataString = localStorage.getItem('member_jwt_token')
      if (memberAuthDataString) {
        try {
          // 1. 將從 localStorage 取出的 JSON 字串，解析成 JavaScript 物件
          const memberAuthData = JSON.parse(memberAuthDataString)
          // 2. 從物件中，只取出真正的 token 值
          token = memberAuthData.token
          console.log('[HTTP Request] Member token extracted and attached.')
        } catch (e) {
          console.error('無法從 localStorage 解析會員認證資料', e)
        }
      }
    }

    // 如果 Token 存在，且請求頭物件存在，則將 Token 添加到 Authorization 頭部
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 提示請求頭帶上token
    console.log(`[HTTP Request] ${config.method?.toUpperCase()} ${config.url}`)
    return config // 必須返回 config 對象
  },
  (error) => {
    console.error('[HTTP Request Error]', error)
    ElMessage.error('請求配置或網路錯誤！')
    return Promise.reject(error)
  },
)

// ====== 響應攔截器 (Response Interceptors) ======
// 在每次接收到伺服器響應之後、數據傳遞給組件之前執行，用於統一處理響應
httpClient.interceptors.response.use(
  (response) => {
    // 只返回響應的 data 部分，簡化組件中的處理
    console.log(
      `[HTTP Response] ${response.config.method?.toUpperCase()} ${response.config.url} - Status: ${response.status}`,
    )
    return response.data // 返回數據部分
  },
  (error) => {
    // 響應錯誤處理（例如 HTTP 狀態碼不是 2xx）
    console.error('[HTTP Response Error]', error.response || error)

    let errorMessage = '未知錯誤發生！'
    if (error.response) {
      // 伺服器有響應，但狀態碼表示錯誤
      const { status, data } = error.response
      switch (status) {
        case 400:
          errorMessage = data.message || '請求數據格式錯誤！'
          break
        case 401:
          errorMessage = data.message || '未經授權，請重新登入！'
          // 可以在這裡處理重定向到登入頁面
          // router.push('/login');
          break
        case 403:
          errorMessage = data.message || '您沒有權限執行此操作！'
          break
        case 404:
          errorMessage = data.message || '請求的資源不存在！'
          break
        case 500:
          errorMessage = data.message || '伺服器內部錯誤，請稍後再試！'
          break
        default:
          errorMessage = `伺服器錯誤：${status} - ${data.message || '未知'}`
      }
    } else if (error.request) {
      // 請求已發出但沒有收到響應（例如網路斷開，後端服務未啟動）
      errorMessage = '網路連接失敗或伺服器無響應！'
    } else {
      // 在設置請求時發生了問題
      errorMessage = `請求錯誤：${error.message}`
    }

    // 使用 Element Plus 顯示錯誤提示
    ElMessage.error(errorMessage)

    // 拒絕 Promise，將錯誤傳遞給調用此 API 的 catch 塊
    return Promise.reject(error)
  },
)

export default httpClient // 導出配置好的 Axios 實例
