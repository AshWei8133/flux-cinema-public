// src/features/Event/store/useCouponCategoryStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import couponCategoryService from '../services/couponCategoryService' // 依實際路徑調整

export const useCouponCategoryStore = defineStore('couponCategory', () => {
  // --- 狀態 (State) ---
  const categories = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // --- 內部工具 ---
  function setError(msg, err) {
    console.error('[CouponCategoryStore] ', msg, err)
    error.value = msg
  }

  // --- 動作 (Actions) ---
  /**
   * 從後端獲取所有優惠券類別
   * @param {Object} params 例如：{ name: '關鍵字', page: 1, size: 10 }
   */
  async function fetchCategories(params) {
    isLoading.value = true
    error.value = null
    try {
      const data = await couponCategoryService.getAllCategories(params)
      // 若後端直接回陣列：data = [{...}, ...]
      // 若後端包了分頁：data = { content: [...], total: 123, ... }
      categories.value = Array.isArray(data) ? data : (data?.content ?? [])
      return data
    } catch (err) {
      setError('無法載入優惠券類別。', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 新增優惠券類別
   * @param {{ couponCategoryName: string, description?: string }} payload
   */
  async function createCouponCategory(payload) {
    error.value = null
    try {
      const created = await couponCategoryService.createCategory(payload)
      // 樂觀更新：直接推進列表（若後端回完整物件含 id）
      if (created && typeof created === 'object') {
        categories.value = [created, ...categories.value]
      }
      return created
    } catch (err) {
      setError('新增優惠券類別失敗。', err)
      throw err
    }
  }

  /**
   * 更新優惠券類別
   * @param {number|string} id
   * @param {{ couponCategoryName?: string, description?: string }} payload
   */
  async function updateCouponCategory(id, payload) {
    error.value = null
    try {
      const updated = await couponCategoryService.updateCategory(id, payload)
      // 同步到本地列表
      const idx = categories.value.findIndex((c) => c.couponCategoryId === id)
      if (idx !== -1 && updated && typeof updated === 'object') {
        categories.value.splice(idx, 1, { ...categories.value[idx], ...updated })
      }
      return updated
    } catch (err) {
      setError('更新優惠券類別失敗。', err)
      throw err
    }
  }

  /**
   * 刪除優惠券類別
   * @param {number|string} id
   */
  async function deleteCouponCategory(id) {
    error.value = null
    try {
      await couponCategoryService.deleteCategory(id)
      categories.value = categories.value.filter((c) => c.couponCategoryId !== id)
    } catch (err) {
      setError('刪除優惠券類別失敗。', err)
      throw err
    }
  }

  /**
   * 批次刪除
   * @param {Array<number|string>} ids
   */
  async function deleteCouponCategoriesInBatch(ids) {
    error.value = null
    try {
      await couponCategoryService.deleteCategoriesInBatch(ids)
      const set = new Set(ids)
      categories.value = categories.value.filter((c) => !set.has(c.couponCategoryId))
    } catch (err) {
      setError('批次刪除優惠券類別失敗。', err)
      throw err
    }
  }

  // --- 回傳 ---
  return {
    categories,
    isLoading,
    error,
    fetchCategories,
    createCouponCategory,
    updateCouponCategory,
    deleteCouponCategory,
    deleteCouponCategoriesInBatch,
  }
})
