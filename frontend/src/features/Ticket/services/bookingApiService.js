import httpClient from "@/services/api";

const BookingApiService = {
    /**
     * 向後端發送請求，建立一個臨時預訂 (短時鎖定或長期預約)
     * @param {object} payload - 請求的資料，應包含 sessionId, seatIds, paymentMethod, tickets 等
     * @returns {Promise<object>} - 後端返回的響應，包含 ticketOrderId 和 reservedExpiredDate
     */
    reserveSeats(payload) {
        // 呼叫我們在後端 TicketOrderController 中定義的 POST /api/ticketOrder/reserve 端點
        return httpClient.post('/ticketOrder/reserve', payload);
    },

    /**
     * 向後端發送請求，取消一個臨時預訂
     * @param {number} orderId - 要取消的臨時訂單 ID
     * @returns {Promise<object>} - 後端返回的成功或失敗訊息
     */
    cancelReservation(orderNumber) {
        // 這部分我們先定義好，後續步驟（修改 router/index.js）會用到
        // 您需要在後端 TicketOrderController 中新增一個對應的 DELETE 端點
        return httpClient.delete(`/ticketOrder/cancel/${orderNumber}`);
    },

    /**
     * 向後端發送請求，最終確認一筆「臨櫃付款」的訂單
     * @param {object} payload - 請求的資料，應包含 encodedOrderId 和 couponId
     * @returns {Promise<object>} - 後端返回的成功訊息
     */
    finalizeCounterReservation(payload) {
        return httpClient.post('/ticketOrder/finalize-counter-reservation', payload);
    }
}

export default BookingApiService;