
import httpClient from "@/services/api";

// 定義此 Service 模組所對應的後端 Controller 基礎路徑
const API_URL = '/admin/ticket';

// 建立一個物件來集中管理所有與票種相關的 API 函式
const AdminTicketService = {
    /**
     * @description 從後端 API 取得所有票種資料
     * @async
     * @returns {Promise<Array>} 後端返回的票種資料陣列
     * @throws {Error} 當 API 請求失敗時，由 Axios 攔截器統一處理並拋出錯誤
     */
    async fetchAllTicketTypes() {
        try {
            const response = await httpClient.get(`${API_URL}/ticket-types`)
            return response;
        } catch (error) {
            console.log('獲取票種資料失敗:', error.message);
            throw error;
        }
    },

    /**
     * @description 向後端 API 發送請求，以新增一筆票種資料
     * @async
     * @param {object} ticketTypeData - 包含新票種資訊的物件 (來自表單)
     * @returns {Promise<object>} 後端返回的操作結果物件 (例如 { success: true, message: '...' })
     * @throws {Error} 當 API 請求失敗時，由 Axios 攔截器統一處理並拋出錯誤
     */
    async createTicketType(ticketTypeData) {
        try {
            const response = await httpClient.post(`${API_URL}/ticket-types`, ticketTypeData);
            return response;
        } catch (error) {
            // 雖然攔截器會處理，但在這裡也可以加上額外的日誌
            console.log('新增票種資料失敗:', error.message);
            throw error;
        }
    },

    /**
     * @description 向後端 API 發送請求，以更新一筆指定的票種資料
     * @async
     * @param {number} ticketTypeId - 要更新的票種 ID
     * @param {object} ticketTypeData - 包含更新資訊的物件
     * @returns {Promise<object>} 後端返回的操作結果
     */
    async updateTicketType(ticketTypeId, ticketTypeData) {
        try {
            // 使用 httpClient.put 方法發送 PUT 請求
            const response = await httpClient.put(`${API_URL}/ticket-types/${ticketTypeId}`, ticketTypeData);
            return response;
        } catch (error) {
            console.log(`更新 ID 為 ${ticketTypeId} 的票種資料失敗:`, error.message);
            throw error;
        }
    },

    /**
     * @description 向後端 API 發送請求，以刪除一筆指定的票種資料
     * @async
     * @param {number} ticketTypeId - 要刪除的票種 ID
     * @returns {Promise<object>} 後端返回的操作結果
     */
    async deleteTicketType(ticketTypeId) {
        try {
            // 使用 httpClient.delete 方法發送 DELETE 請求
            const response = await httpClient.delete(`${API_URL}/ticket-types/${ticketTypeId}`);
            return response;
        } catch (error) {
            console.log(`刪除 ID 為 ${ticketTypeId} 的票種資料失敗:`, error.message);
            throw error;
        }
    },

    /**
     * @description 從後端 API 取得所有影廳類型的基礎票價
     * @async
     * @returns {Promise<object>} 後端返回的基礎票價物件，格式為 { theaterTypeId: price }
     */
    async fetchBasePrices() {
        try {
            const response = await httpClient.get(`${API_URL}/price-rules/base-prices`);
            return response;
        } catch (error) {
            console.error('獲取基礎票價資料失敗:', error.message);
            throw error;
        }
    },
    /**
     * @description 向後端 API 發送請求，批次儲存所有票價規則
     * @async
     * @param {Array<object>} priceRules - 包含所有票價規則的陣列
     * @returns {Promise<object>} 後端返回的操作結果
     */
    async saveAllPriceRules(priceRules) {
        try {
            const response = await httpClient.post(`${API_URL}/price-rules/batch-update`, priceRules);
            return response;
        } catch (error) {
            console.error('儲存票價規則失敗:', error.message);
            throw error;
        }
    }

}

// 導出這個服務物件，讓 Store 可以引入使用
export default AdminTicketService;