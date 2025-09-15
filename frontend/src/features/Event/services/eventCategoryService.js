import httpClient from '@/services/api' // 引入我們配置好的 axios 實例

/**
 * eventCategoryService 物件
 * 封裝了所有與後端「活動類型」API 相關的 HTTP 請求。
 */
const eventCategoryService = {
  /**
   * 獲取所有活動類型
   * @returns {Promise<Array>} 後端響應的資料 (活動類型陣列)
   */
  async getAll() {
    try {
      console.log('[DEBUG] getAll: 正在發送 GET 請求到 /admin/events-categories')
      const response = await httpClient.get('/admin/events-categories')
      console.log('[DEBUG] getAll: 成功獲取活動類型列表，響應數據為:', response)
      return response
    } catch (error) {
      console.error('Service 獲取活動類型列表失敗:', error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error // 將錯誤向上拋出，讓 store 可以捕獲
    }
  },

  /**
   * 根據 ID 獲取單一活動類型的詳細資料
   * @param {number} categoryId - 類型的唯一 ID
   * @returns {Promise<Object>} 後端響應的資料 (單一活動類型物件)
   */
  async getById(categoryId) {
    try {
      console.log(`[DEBUG] getById: 正在發送 GET 請求到 /admin/events-categories/${categoryId}`)
      const response = await httpClient.get(`/admin/events-categories/${categoryId}`)
      console.log(`[DEBUG] getById: 成功獲取活動類型 (ID: ${categoryId})，響應數據為:`, response)
      return response
    } catch (error) {
      console.error(`Service 獲取活動類型 (ID: ${categoryId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 新增一個活動類型
   * @param {object} categoryData - 包含新活動類型資料的物件
   * @returns {Promise<Object>} 後端響應的資料 (新增後的物件)
   */
  async create(categoryData) {
    try {
      console.log('[DEBUG] create: 正在發送 POST 請求到 /admin/events-categories:', categoryData)
      const response = await httpClient.post('/admin/events-categories', categoryData)
      console.log('[DEBUG] create: 成功新增活動類型，響應數據為:', response)
      return response
    } catch (error) {
      console.error('Service 新增活動類型失敗:', error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 更新一個現有的活動類型
   * @param {number} categoryId - 要更新的類型 ID
   * @param {object} updatedData - 包含更新後資料的物件
   * @returns {Promise<Object>} 後端響應的資料 (更新後的物件)
   */
  async update(categoryId, updatedData) {
    try {
      console.log(
        `[DEBUG] update: 正在發送 PUT 請求到 /admin/events-categories/${categoryId}，請求主體為:`,
        updatedData,
      )
      const response = await httpClient.put(`/admin/events-categories/${categoryId}`, updatedData)
      console.log(`[DEBUG] update: 成功更新活動類型 (ID: ${categoryId})，響應數據為:`, response)
      return response
    } catch (error) {
      console.error(`Service 更新活動類型 (ID: ${categoryId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 刪除一個活動類型
   * @param {number} categoryId - 要刪除的類型 ID
   * @returns {Promise<Object>} 後端響應的資料
   */
  async delete(categoryId) {
    try {
      console.log(`[DEBUG] delete: 正在發送 DELETE 請求到 /admin/events-categories/${categoryId}`)
      const response = await httpClient.delete(`/admin/events-categories/${categoryId}`)
      console.log(`[DEBUG] delete: 成功刪除活動類型 (ID: ${categoryId})，響應數據為:`, response)
      return response
    } catch (error) {
      console.error(`Service 刪除活動類型 (ID: ${categoryId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },
}

export default eventCategoryService

/**
 * 集中處理 Axios 錯誤日誌的輔助函式。
 * @param {object} error - Axios 錯誤物件
 */
function logAxiosError(error) {
  if (error.response) {
    console.error('  -> 錯誤類型: 來自伺服器的響應 (非 2xx 狀態碼)')
    console.error('  -> 狀態碼:', error.response.status)
    console.error('  -> 錯誤資料:', error.response.data)
    console.error('  -> 響應頭部:', error.response.headers)
  } else if (error.request) {
    console.error('  -> 錯誤類型: 請求無響應')
    console.error('  -> 請求物件:', error.request)
  } else {
    console.error('  -> 錯誤類型: 設定請求時發生錯誤')
    console.error('  -> 錯誤訊息:', error.message)
  }
}
