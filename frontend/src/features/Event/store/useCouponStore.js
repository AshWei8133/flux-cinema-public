import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
// 您需要建立一個對應的 service 檔案來處理 API 請求
import couponService from '@/features/Event/services/couponService'

/**
 * 管理優惠券的 Pinia Store
 */
export const useCouponStore = defineStore('coupon', () => {
  // --- State (狀態) ---
  // 存放優惠券列表及分頁資訊
  const coupons = ref({
    list: [],
    totalPages: 0,
    totalElements: 0,
  })
  // 存放當前正在編輯或查看的單一優惠券
  const currentCoupon = ref(null)
  // 載入狀態
  const isLoading = ref(false)
  // 錯誤訊息
  const error = ref(null)

  // --- Getters (計算屬性) ---
  const couponList = computed(() => coupons.value.list)
  const totalPages = computed(() => coupons.value.totalPages)
  const totalCoupons = computed(() => coupons.value.totalElements)

  // --- Actions (動作) ---

  /**
   * 從後端獲取分頁後的優惠券列表
   * @param {object} params - 包含查詢條件的物件，例如 { page, size, name }
   */
  async function fetchCoupons(params = { page: 0, size: 10 }) {
    isLoading.value = true
    error.value = null
    try {
      // 呼叫 Service 層來獲取資料
      const response = await couponService.getAll(params)
      // 後端應回傳一個包含列表和分頁資訊的物件
      coupons.value = {
        list: response.content || [],
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0,
      }
    } catch (err) {
      console.error('Store 獲取優惠券列表失敗:', err)
      error.value = '無法載入優惠券資料，請稍後再試。'
      // 發生錯誤時清空列表
      coupons.value = { list: [], totalPages: 0, totalElements: 0 }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 新增一個優惠券
   * @param {object} couponData - 新優惠券的資料
   */
  async function createCoupon(couponData) {
    isLoading.value = true
    error.value = null
    try {
      await couponService.create(couponData)
      // 新增成功後，回到第一頁以查看最新資料
      await fetchCoupons({ page: 0, size: 10 })
    } catch (err) {
      console.error('Store 新增優惠券失敗:', err)
      error.value = err.response?.data?.message || '新增失敗，請檢查輸入內容。'
      throw err // 將錯誤拋出，讓元件可以捕獲並顯示
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 更新一個現有的優惠券
   * @param {number} couponId - 要更新的優惠券 ID
   * @param {object} updatedData - 更新後的資料
   */
  async function updateCoupon(couponId, updatedData) {
    isLoading.value = true
    error.value = null
    try {
      await couponService.update(couponId, updatedData)
      // 停留在當前頁面並刷新
      await fetchCoupons()
    } catch (err) {
      console.error('Store 更新優惠券失敗:', err)
      error.value = err.response?.data?.message || '更新失敗，請稍後再試。'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 刪除一個優惠券
   * @param {number} couponId - 要刪除的優惠券 ID
   */
  async function deleteCoupon(couponId) {
    isLoading.value = true
    error.value = null
    try {
      await couponService.delete(couponId)
      // 刪除成功後刷新當前頁面
      await fetchCoupons()
    } catch (err) {
      console.error('Store 刪除優惠券失敗:', err)
      error.value = err.response?.data?.message || '刪除失敗，請稍後再試。'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  // --- Return ---
  // 將所有需要給外部元件使用的 state, getters, 和 actions 在此導出
  return {
    coupons,
    currentCoupon,
    isLoading,
    error,
    couponList,
    totalPages,
    totalCoupons,
    fetchCoupons,
    createCoupon,
    updateCoupon,
    deleteCoupon,
  }
})
