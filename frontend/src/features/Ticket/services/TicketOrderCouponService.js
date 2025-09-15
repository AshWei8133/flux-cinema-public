import httpClient from "@/services/api"; // 引入你封裝好的 axios 實例

/**
 * @typedef {object} ApplicableCoupon
 * @property {number} memberCouponId - 會員優惠券的唯一ID
 * @property {string} couponName - 優惠券名稱
 * @property {string} couponDescription - 優惠券描述
 * @property {number} discountAmount - 【計算後】的實際折扣金額
 * @property {number} minimumSpend - 最低消費金額
 * @property {boolean} isUsable - 目前訂單金額是否可使用此券
 */

// 定義此 Service 模組所對應的後端 Controller 基礎路徑
const API_BASE_URL = '/coupons/ticketorder';

/**
 * @description 集中管理所有與「訂票流程中的優惠券」相關的 API 函式
 */
const TicketOrderCouponService = {
    /**
     * 向後端請求適用於當前訂單的優惠券列表。
     * @param {object} params - 包含 sessionId 和 subtotal 的物件。
     * @param {number} params.sessionId - 當前場次的 ID。
     * @param {number} params.subtotal - 當前訂單的小計金額。
     * @returns {Promise<ApplicableCoupon[]>} 後端返回的適用優惠券陣列。
     */
    async getApplicableCoupons(params) {
        try {
            // 使用 httpClient 的 get 方法，並將 params 物件作為查詢參數傳遞
            // axios 會自動將 { sessionId: 64, subtotal: 550 } 轉換為 ?sessionId=64&subtotal=550
            const response = await httpClient.get(`${API_BASE_URL}/applicable`, { params });

            // httpClient 通常會自動解開 response.data，如果沒有，請使用 response.data
            return response;
        } catch (error) {
            // 統一錯誤處理，將服務器返回的錯誤訊息或預設訊息向上拋出
            const errorMessage = error.response?.data?.message || '獲取優惠券失敗';
            console.error('獲取適用優惠券失敗:', errorMessage);
            throw new Error(errorMessage);
        }
    }
};

// 導出這個服務物件，讓 Store 可以引入使用
export default TicketOrderCouponService;