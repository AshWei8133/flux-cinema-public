// 提供後端的登入資訊(帳號及密碼)
export interface LoginAdmin {
  adminName: string
  password: string
}

// 後端 /api/admin/login 響應類型
export type LoginResponse = boolean
