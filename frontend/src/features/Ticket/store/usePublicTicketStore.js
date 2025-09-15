import { defineStore } from 'pinia'
import { ref } from 'vue'
import PublicTicketService from '../services/PublicTicketService'

export const usePublicTicketStore = defineStore('publicTicket', () => {
    // ------------------- State -------------------
    /**
     * @description 儲存從後端獲取的公開票種列表
     */
    const ticketTypes = ref([])

    /**
   * @description 儲存從後端獲取的基礎票價物件 { theaterTypeId: price }
   */
    const basePrices = ref({}) // 後端 Map<K,V> 對應到前端就是 Object {}
    /**
     * @description 標示資料是否正在載入中
     */
    const isLoading = ref(false)
    /**
     * @description 儲存載入時發生的錯誤
     */
    const error = ref(null)

    // ------------------- Actions -------------------
    /**
     * @description 從後端 API 獲取所有公開票種資料
     */
    const fetchAllTicketTypes = async () => {
        // 如果已有資料，就不重複獲取
        if (ticketTypes.value.length > 0) {
            return
        }

        isLoading.value = true
        error.value = null
        try {
            const data = await PublicTicketService.fetchAllTicketTypes()
            ticketTypes.value = data
        } catch (err) {
            error.value = err.message || '獲取票種資料時發生錯誤'
            console.error(error.value)
        } finally {
            isLoading.value = false
        }
    }

    /**
   * [新增]
   * @description 從後端 API 獲取所有基礎票價資料
   */
    const fetchBasePrices = async () => {
        // 如果已有資料，就不重複獲取
        if (Object.keys(basePrices.value).length > 0) return

        isLoading.value = true
        error.value = null
        try {
            basePrices.value = await PublicTicketService.fetchBasePrices()
        } catch (err) {
            error.value = err.message || '獲取基礎票價時發生錯誤'
            console.error(error.value)
        } finally {
            isLoading.value = false
        }
    }

    return {
        ticketTypes,
        basePrices,
        isLoading,
        error,
        fetchAllTicketTypes,
        fetchBasePrices,
    }
})