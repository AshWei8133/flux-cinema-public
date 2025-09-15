import httpClient from "@/services/api"

// 定義此 Service 模組所對應的後端 Controller 基礎路徑 (不含 /admin)
const API_URL = '/ticket'

// 建立一個物件來集中管理所有與「前台票種」相關的 API 函式
const PublicTicketService = {
    /**
     * @description 從後端 API 取得所有公開的票種資料
     * @async
     * @returns {Promise<Array>} 後端返回的票種資料陣列
     * @throws {Error} 當 API 請求失敗時，由 Axios 攔截器統一處理並拋出錯誤
     */
    async fetchAllTicketTypes() {
        try {
            // 呼叫不含 /admin 的 API 路徑
            const response = await httpClient.get(`${API_URL}/ticket-types`)
            return response
        } catch (error) {
            console.log('獲取公開票種資料失敗:', error.message)
            throw error
        }
    },

    /**
   * @description 從後端 API 取得所有影廳類型的基礎票價
   * @returns {Promise<Object>} 後端返回的基礎票價物件 (Map<Integer, Integer>)
   */
    async fetchBasePrices() {
        try {
            const response = await httpClient.get(`${API_URL}/price-rules/base-prices`)
            return response
        } catch (error) {
            console.error('獲取基礎票價資料失敗:', error.message)
            throw error
        }
    },
}

// 導出這個服務物件，讓 Store 可以引入使用
export default PublicTicketService