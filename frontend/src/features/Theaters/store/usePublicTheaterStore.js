import { defineStore } from 'pinia'
import { ref } from 'vue'
import PublicTheaterService from '../services/PublicTheaterService'

export const usePublicTheaterStore = defineStore('publicTheater', () => {
    // ------------------- State -------------------
    const theaterTypes = ref([])
    const isLoading = ref(false)
    const error = ref(null)

    // ------------------- Actions -------------------
    const fetchAllTheaterTypes = async () => {
        // 如果已有資料，就不重複獲取
        if (theaterTypes.value.length > 0) {
            return
        }

        isLoading.value = true
        error.value = null
        try {
            const data = await PublicTheaterService.fetchAllTheaterTypes()
            theaterTypes.value = data
        } catch (err) {
            error.value = err.message || '獲取影廳版本資料時發生錯誤'
            console.error(error.value)
        } finally {
            isLoading.value = false
        }
    }

    return {
        theaterTypes,
        isLoading,
        error,
        fetchAllTheaterTypes,
    }
})