import httpClient from "@/services/api";


// 訂單相關 API 的基礎路徑
const API_URL = '/admin/ticketOrder';

const AdminTicketOrderService = {
    /**
     * @description 根據篩選條件，從後端 API 取得分頁後的訂單列表
     * @async
     * @param {object} params - 包含篩選與分頁條件的物件
     * @returns {Promise<object>} 後端返回的分頁物件 (Page<OrderSummaryDTO>)
     */
    async fetchOrders(params) {
        try {
            // httpClient.get 方法可以接受第二個參數，其中 params 物件會被自動轉換為 URL 查詢字串
            // 例如 { page: 1, status: '已付款' } 會變成 "?page=1&status=已付款"
            const response = await httpClient.get(API_URL, { params });
            return response;
        } catch (error) {
            console.error('獲取訂單列表失敗:', error.message);
            throw error;
        }
    },

    /**
     * @description 根據專業訂單編號從後端 API 取得單筆訂單的完整詳情
     * @async
     * @param {string} orderNumber - 要查詢的專業訂單編號 (例如 FX250827-QRTB)
     * @returns {Promise<object>} 後端返回的訂單詳情物件 (OrderDetailDTO)
     */
    async fetchOrderDetailByNumber(orderNumber) {
        try {
            // 將 orderId 改為 orderNumber，以符合後端 API 的路徑
            const response = await httpClient.get(`${API_URL}/${orderNumber}`);
            return response;
        } catch (error) {
            console.error(`獲取訂單 #${orderNumber} 詳情失敗:`, error.message);
            throw error;
        }
    },
    /**
     * @description 將待付款訂單標記為已付款
     * @param {string} orderNumber - 訂單編號
     * @param {object} paymentData - 包含付款方式的物件, e.g., { paymentType: 'Cash' }
     * @returns {Promise<object>} 後端的回應
     */
    async markAsPaid(orderNumber, paymentData) {
        try {
            const response = await httpClient.put(`${API_URL}/${orderNumber}/mark-as-paid`, paymentData);
            return response;
        } catch (error) {
            console.error(`訂單 #${orderNumber} 標記付款失敗:`, error.message);
            throw error;
        }
    },

    /**
     * @description 為已付款訂單辦理退款
     * @param {string} orderNumber - 訂單編號
     * @returns {Promise<object>} 後端的回應
     */
    async refundOrder(orderNumber) {
        try {
            const response = await httpClient.put(`${API_URL}/${orderNumber}/refund`);
            return response;
        } catch (error) {
            console.error(`訂單 #${orderNumber} 退款失敗:`, error.message);
            throw error;
        }
    }
}

export default AdminTicketOrderService;