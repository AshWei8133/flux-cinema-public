import httpClient from "@/services/api"

// 定義此 Service 模組所對應的後端 Controller 基礎路徑
const API_URL = '/ticketOrder'

/**
 * @description 集中管理所有與「付款」相關的 API 函式
 */
const PaymentService = {
    /**
     * 【新增方法】向後端發起更新訂單並同時取得綠界支付參數的請求。
     * @param {object} updateData - 包含臨時訂單 ID (字串) 和優惠券 ID 的物件。
     * @returns {Promise<object>} 後端返回的物件，包含 apiUrl 和 params。
     */
    async updateOrderAndCheckout(updateData) {
        try {
            const response = await httpClient.post(`${API_URL}/update-and-checkout`, updateData);
            return response;
        } catch (error) {
            console.error('發起更新並結帳請求失敗:', error.message);
            throw error;
        }
    },

    // 未來可以新增其他付款相關的 API 函式，例如：
    // async fetchPaymentResult(orderId) { ... }
}

// 導出這個服務物件，讓 Store 可以引入使用
export default PaymentService