import { defineStore } from 'pinia'
import { ref } from 'vue'
import PaymentService from '../services/PaymentService'
import BookingApiService from '../services/bookingApiService'

export const usePaymentStore = defineStore('payment', () => {
    // ------------------- State -------------------
    /**
     * @description 標示付款流程是否正在處理中
     */
    const isLoading = ref(false)

    /**
     * @description 儲存處理過程中發生的錯誤
     */
    const error = ref(null)

    /**
     * @description 儲存從後端獲取的 ECPay API URL
     */
    const ecpayApiUrl = ref('')

    /**
     * @description 儲存從後端獲取的 ECPay 表單參數
     */
    const ecpayParams = ref({})

    // ------------------- Actions -------------------
    /**
     * 【新增 Action】更新臨時訂單並發起綠界結帳
     * @param {object} payload - 包含臨時訂單 ID 和優惠券 ID 的物件。
     * @returns {Promise<boolean>} 回傳 true 表示成功，false 表示失敗。
     */
    const updateOrderAndCheckout = async (payload) => {
        isLoading.value = true;
        error.value = null;
        ecpayApiUrl.value = '';
        ecpayParams.value = {};

        try {
            // 呼叫新的 PaymentService 方法
            const data = await PaymentService.updateOrderAndCheckout(payload);

            ecpayApiUrl.value = data.apiUrl;
            ecpayParams.value = data.params;
            return true;
        } catch (err) {
            error.value = err.message || '處理付款時發生未知錯誤';
            console.error(error.value);
            return false;
        } finally {
            isLoading.value = false;
        }
    };

    /**
    * 【新增 Action】處理臨櫃付款：最終確認訂單
    * @param {object} payload - 包含 encodedOrderId 和 couponId
    * @returns {Promise<boolean>} - 是否成功
    */
    async function confirmCounterOrder(payload) {
        isLoading.value = true
        try {
            await BookingApiService.finalizeCounterReservation(payload)
            return true // API 成功，回傳 true
        } catch (error) {
            console.error('臨櫃訂單確認失敗:', error)
            // httpClient 的攔截器會自動顯示錯誤訊息
            return false // API 失敗，回傳 false
        } finally {
            isLoading.value = false
        }
    }


    return {
        isLoading,
        error,
        ecpayApiUrl,
        ecpayParams,
        updateOrderAndCheckout,
        confirmCounterOrder
    }
})