import { defineStore } from 'pinia';
import { ref } from 'vue';
import TicketOrderCouponService from '../services/TicketOrderCouponService';

export const useTicketOrderCoupon = defineStore('ticketOrderCoupon', () => {
    // ------------------- State -------------------
    /**
     * @description 儲存適用於目前訂單的優惠券列表
     * @type {import('vue').Ref<import('@/services/CouponService').ApplicableCoupon[]>}
     */
    const applicableCoupons = ref([]);

    /**
     * @description 標示是否正在從後端加載優惠券
     */
    const isLoading = ref(false);

    /**
     * @description 儲存加載過程中發生的錯誤訊息
     */
    const error = ref(null);

    // ------------------- Actions -------------------
    /**
     * @description 從後端獲取適用於當前訂單的優惠券
     * @param {object} context - 包含 sessionId 和 subtotal 的物件。
     * @param {number} context.sessionId - 當前場次的 ID。
     * @param {number} context.subtotal - 當前訂單的小計金額。
     */
    const fetchApplicableCoupons = async (context) => {
        // 開始加載，重置狀態
        isLoading.value = true;
        error.value = null;
        applicableCoupons.value = []; // 清空舊列表

        try {
            // 呼叫 Service 層的方法來執行 API 請求
            const coupons = await TicketOrderCouponService.getApplicableCoupons(context);
            console.log(coupons);
            // 成功後，將獲取的資料存入 state
            applicableCoupons.value = coupons;
        } catch (err) {
            // 失敗後，將錯誤訊息存入 state
            error.value = err.message || '加載優惠券時發生未知錯誤';
            console.error(error.value);
        } finally {
            // 無論成功或失敗，結束加載狀態
            isLoading.value = false;
        }
    };

    /**
     * @description 清空 Store 中的所有狀態，用於離開訂票流程時
     */
    const clearCouponState = () => {
        applicableCoupons.value = [];
        isLoading.value = false;
        error.value = null;
    };

    return {
        // State
        applicableCoupons,
        isLoading,
        error,

        // Actions
        fetchApplicableCoupons,
        clearCouponState,
    };
});