// src/features/Event/services/couponCategoryService.js
import httpClient from '@/services/api'

/**
 * CouponCategoryService：處理優惠券類別的後台 API
 * 注意：此版本使用 /admin 路由，會觸發 admin token 攔截器。
 * 若要公開給前台讀取，改 BASE = '/api/coupon-categories'
 */
const BASE = '/admin/coupon-categories'

const couponCategoryService = {
  /**
   * 取得全部優惠券類別
   * @param {object} params 可選查詢參數（如分頁、關鍵字）
   */
  async getAllCategories(params) {
    try {
      console.log('[DEBUG] getAllCategories: GET', BASE, 'params=', params)
      const data = await httpClient.get(BASE, { params }) // 攔截器已回 data
      console.log('[DEBUG] getAllCategories: success, data=', data)
      return data
    } catch (error) {
      console.error('取得優惠券類別失敗:', error)
      logAxiosError(error)
      throw error
    }
  },

  /**
   * 依 ID 取得單一類別
   */
  async getCategoryById(id) {
    try {
      console.log(`[DEBUG] getCategoryById: GET ${BASE}/${id}`)
      const data = await httpClient.get(`${BASE}/${id}`)
      console.log('[DEBUG] getCategoryById: success')
      return data
    } catch (error) {
      console.error(`取得優惠券類別（ID: ${id}）失敗:`, error)
      logAxiosError(error)
      throw error
    }
  },

  /**
   * 新增類別
   */
  async createCategory(payload) {
    try {
      console.log('[DEBUG] createCategory: POST', BASE, 'payload=', payload)
      const data = await httpClient.post(BASE, payload)
      console.log('[DEBUG] createCategory: success, data=', data)
      return data
    } catch (error) {
      console.error('新增優惠券類別失敗:', error)
      logAxiosError(error)
      throw error
    }
  },

  /**
   * 更新類別
   */
  async updateCategory(id, payload) {
    try {
      console.log('[DEBUG] updateCategory: PUT', `${BASE}/${id}`, 'payload=', payload)
      const data = await httpClient.put(`${BASE}/${id}`, payload)
      console.log('[DEBUG] updateCategory: success, data=', data)
      return data
    } catch (error) {
      console.error(`更新優惠券類別（ID: ${id}）失敗:`, error)
      logAxiosError(error)
      throw error
    }
  },

  /**
   * 刪除類別
   */
  async deleteCategory(id) {
    try {
      console.log('[DEBUG] deleteCategory: DELETE', `${BASE}/${id}`)
      const data = await httpClient.delete(`${BASE}/${id}`)
      console.log('[DEBUG] deleteCategory: success, data=', data)
      return data
    } catch (error) {
      console.error(`刪除優惠券類別（ID: ${id}）失敗:`, error)
      logAxiosError(error)
      throw error
    }
  },

  /**
   * 批次刪除
   * @param {Array<number>} ids
   */
  async deleteCategoriesInBatch(ids) {
    try {
      console.log('[DEBUG] deleteCategoriesInBatch: DELETE', `${BASE}/batch`, 'body=', ids)
      const data = await httpClient.delete(`${BASE}/batch`, { data: ids })
      console.log('[DEBUG] deleteCategoriesInBatch: success, data=', data)
      return data
    } catch (error) {
      console.error('批次刪除優惠券類別失敗:', error)
      logAxiosError(error)
      throw error
    }
  },
}

export default couponCategoryService

/**
 * 集中處理 Axios 錯誤日誌
 */
function logAxiosError(error) {
  if (error?.response) {
    console.error('  -> 錯誤類型: 伺服器已回應（非 2xx）')
    console.error('  -> 狀態碼:', error.response.status)
    console.error('  -> 錯誤資料:', error.response.data)
    console.error('  -> 響應標頭:', error.response.headers)
  } else if (error?.request) {
    console.error('  -> 錯誤類型: 已送出請求但無響應（可能是 CORS/網路/伺服器未啟動）')
    console.error('  -> 請求物件:', error.request)
  } else {
    console.error('  -> 錯誤類型: 設定請求時失敗')
    console.error('  -> 訊息:', error?.message)
  }
}
