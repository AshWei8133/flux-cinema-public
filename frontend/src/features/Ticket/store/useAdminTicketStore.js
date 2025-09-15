
import { defineStore } from "pinia";
import { reactive, ref } from "vue";
// 引入我們定義好的 Service
import AdminTicketService from "../services/AdminTicketService"; // 請確認此路徑與您的專案結構相符

// 使用 defineStore 建立 Store
export const useAdminTicketStore = defineStore('adminTicket', () => {

    // =================================================================
    // State (狀態)
    // 存放此模組共享的響應式資料
    // =================================================================

    /**
     * @description 儲存從後端獲取的票種列表
     * @type {import('vue').Ref<Array>}
     */
    const ticketTypes = ref([]);

    /**
     * @description 標示票種資料是否正在載入中，用於控制 UI 的 loading 效果
     * @type {import('vue').Ref<boolean>}
     */
    const isTicketTypeloading = ref(false);

    /**
     * @description 儲存從後端獲取的基礎票價
     * @type {import('vue').Ref<object>}
     */
    const basePrices = reactive({});

    // =================================================================
    // Actions (操作)
    // 執行非同步操作（如 API 請求）並更新 State 的地方
    // =================================================================

    /**
     * @description 從後端 API 獲取所有票種資料，並更新到 State 中
     * @async
     */
    const fetchAllTicketTypes = async () => {
        isTicketTypeloading.value = true; // 操作開始前，設置為載入中
        try {
            // 呼叫 Service 層的函式來執行 API 請求
            const data = await AdminTicketService.fetchAllTicketTypes();
            // 成功獲取資料後，更新 State
            ticketTypes.value = data;
            console.log('票種資料已成功載入 Store:', ticketTypes.value);
        } catch (error) {
            // 這裡主要負責處理錯誤發生時的 State 狀態
            console.error('在 Store 中獲取票種資料失敗:', error);
            ticketTypes.value = []; // 載入失敗時清空陣列，避免頁面顯示舊資料
        } finally {
            // 無論成功或失敗，最後都要結束載入狀態
            isTicketTypeloading.value = false;
        }
    }


    /**
     * @description 新增一筆票種資料，並在成功後重新獲取全部列表
     * @async
     * @param {object} newTicketTypeData - 要新增的票種資料
     * @returns {Promise<object>} API 回應的操作結果
     */
    const createTicketType = async (newTicketTypeData) => {
        try {
            // 呼叫 Service 層的函式來執行 API 請求
            const response = await AdminTicketService.createTicketType(newTicketTypeData);

            // 檢查 API 回應是否成功
            if (response.success) {
                // 新增成功後，重新觸發一次 fetchAllTicketTypes 來獲取最新最完整的列表
                console.log('新增成功，正在重新獲取所有票種資料...');
                await fetchAllTicketTypes();
            }
            return response;
        } catch (error) {
            console.error('在 Store 中新增票種失敗:', error);
            throw error;
        }
    }


    /**
     * @description 更新一筆票種資料，並在成功後重新獲取全部列表
     * @async
     * @param {number} ticketTypeId - 要更新的票種 ID
     * @param {object} ticketTypeData - 要更新的票種資料
     * @returns {Promise<object>} API 回應的操作結果
     */
    const updateTicketType = async (ticketTypeId, ticketTypeData) => {
        try {
            const response = await AdminTicketService.updateTicketType(ticketTypeId, ticketTypeData);
            if (response.success) {
                console.log('更新成功，正在重新獲取所有票種資料...');
                await fetchAllTicketTypes();
            }
            return response;
        } catch (error) {
            console.error('在 Store 中更新票種失敗:', error);
            throw error;
        }
    }

    /**
     * @description 刪除一筆票種資料，並在成功後重新獲取全部列表
     * @async
     * @param {number} ticketTypeId - 要刪除的票種 ID
     * @returns {Promise<object>} API 回應的操作結果
     */
    const deleteTicketType = async (ticketTypeId) => {
        try {
            const response = await AdminTicketService.deleteTicketType(ticketTypeId);
            if (response.success) {
                console.log('刪除成功，正在重新獲取所有票種資料...');
                await fetchAllTicketTypes();
            }
            return response;
        } catch (error) {
            console.error('在 Store 中刪除票種失敗:', error);
            throw error;
        }
    }

    /**
     * @description 從後端 API 獲取基礎票價資料，並更新到 State 中
     * @async
     */
    const fetchBasePrices = async () => {
        try {
            const data = await AdminTicketService.fetchBasePrices();
            // reactive 物件的更新需要用 Object.assign 來保持其響應性
            Object.assign(basePrices, data);
            console.log('基礎票價資料已成功載入 Store:', basePrices);
        } catch (error) {
            console.error('在 Store 中獲取基礎票價失敗:', error);
            // 載入失敗時清空物件
            Object.keys(basePrices).forEach(key => delete basePrices[key]);
        }
    }

    /**
     * @description 儲存所有票價規則
     * @async
     * @param {Array<object>} priceRules - 要儲存的票價規則陣列
     * @returns {Promise<object>} API 回應的操作結果
     */
    const savePriceRules = async (priceRules) => {
        try {
            const response = await AdminTicketService.saveAllPriceRules(priceRules);
            // 由於儲存成功後，頁面上的價格就是最新的，理論上不需要再重新 fetch
            // 如果有需要，也可以在這裡重新 fetchBasePrices
            return response;
        } catch (error) {
            console.error('在 Store 中儲存票價規則失敗:', error);
            throw error;
        }
    }


    // 將需要給元件使用的 State 和 Actions 返回
    return {
        ticketTypes,
        isTicketTypeloading,
        basePrices,
        fetchAllTicketTypes,
        createTicketType,
        updateTicketType,
        deleteTicketType,
        fetchBasePrices,
        savePriceRules,
    }
})