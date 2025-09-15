import httpClient from '@/services/api'

// 定義此 Service 模組所對應的後端 Controller 基礎路徑 (不含 /admin)
const API_URL = '/theater'

const PublicTheaterService = {
    /**
     * @description 從後端 API 取得所有公開的影廳類型資料
     * @async
     * @returns {Promise<Array>} 後端返回的影廳類型資料陣列
     * @throws {Error} 當 API 請求失敗時，由 Axios 攔截器統一處理並拋出錯誤
     */
    async fetchAllTheaterTypes() {
        try {
            // 呼叫不含 /admin 的 API 路徑
            const response = await httpClient.get(`${API_URL}/theaterTypes`)
            return response
        } catch (error) {
            console.error('獲取公開影廳類別失敗:', error.message)
            throw error
        }
    },
}

export default PublicTheaterService