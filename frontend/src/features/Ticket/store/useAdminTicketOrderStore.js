import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import AdminTicketOrderService from "../services/AdminTicketOrderService";

export const useAdminTicketOrderStore = defineStore('adminTicketOrder', () => {

    // =================================================================
    // State (狀態)
    // =================================================================
    const orders = ref([]); // 儲存當前頁的訂單列表
    const totalOrders = ref(0); // 總訂單筆數
    const isLoading = ref(false);

    const selectedOrderDetails = reactive({
        data: null,
        isLoading: false,
    })

    // =================================================================
    // Actions (操作)
    // =================================================================

    /**
     * 根據篩選條件，獲取分頁後的訂單列表
     * @param {object} searchParams - 包含篩選與分頁條件的物件
     */
    const fetchOrders = async (searchParams) => {
        isLoading.value = true;
        try {
            const responsePage = await AdminTicketOrderService.fetchOrders(searchParams);
            // 從後端回傳的 Page 物件中，更新狀態
            orders.value = responsePage.content;
            totalOrders.value = responsePage.totalElements;
        } catch (error) {
            console.error("Store 中獲取訂單列表失敗:", error);
            orders.value = [];
            totalOrders.value = 0;
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 根據專業訂單編號獲取單筆訂單詳情
     * @param {string} orderNumber - 專業訂單編號
     */
    /**
     * @description 根據專業訂單編號獲取單筆訂單詳情
     * @param {string} orderNumber - 專業訂單編號
     */
    const fetchOrderDetail = async (orderNumber) => {
        selectedOrderDetails.isLoading = true;
        try {
            // 呼叫 Service 中修正後的函式
            const data = await AdminTicketOrderService.fetchOrderDetailByNumber(orderNumber);
            selectedOrderDetails.data = data;
        } catch (error) {
            console.error(`Store 中獲取訂單 #${orderNumber} 詳情失敗:`, error);
            selectedOrderDetails.data = null;
        } finally {
            selectedOrderDetails.isLoading = false;
        }
    };

    /**
     * @description 標記訂單為已付款
     * @param {string} orderNumber
     * @param {object} paymentData
     * @returns {Promise<object>}
     */
    const markOrderAsPaid = async (orderNumber, paymentData) => {
        try {
            const response = await AdminTicketOrderService.markAsPaid(orderNumber, paymentData);
            // 操作成功後，可以選擇重新獲取當前訂單詳情或整個列表來更新畫面
            if (response.success && selectedOrderDetails.data?.orderNumber === orderNumber) {
                // 如果正在看的剛好是這筆訂單，就重新載入它的詳情
                await fetchOrderDetail(selectedOrderDetails.data.orderNumber);
            }
            return response;
        } catch (error) {
            console.error(`Store 中標記訂單 #${orderId} 付款失敗:`, error);
            throw error;
        }
    };

    /**
     * @description 執行訂單退款
     * @param {number} orderNumber
     * @returns {Promise<object>}
     */
    const refundOrder = async (orderNumber) => {
        try {
            const response = await AdminTicketOrderService.refundOrder(orderNumber);
            // 操作成功後，更新詳情
            if (response.success && selectedOrderDetails.data?.orderNumber === orderNumber) {
                await fetchOrderDetail(selectedOrderDetails.data.orderNumber);
            }
            return response;
        } catch (error) {
            console.error(`Store 中退款訂單 #${orderId} 失敗:`, error);
            throw error;
        }
    };

    return {
        orders,
        totalOrders,
        isLoading,
        selectedOrderDetails,
        fetchOrders,
        fetchOrderDetail,
        markOrderAsPaid,
        refundOrder
    }
});