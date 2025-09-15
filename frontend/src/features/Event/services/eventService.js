import httpClient from '@/services/api'

/**
 * EventService 負責處理所有與活動相關的 API 請求
 */
const eventService = {
  /**
   * 獲取所有活動
   */
  async getAllEvents(params) {
    try {
      console.log('[DEBUG] getAllEvents: 正在發送 GET 請求到 /admin/events，參數為:', params) // 新增除錯語句
      const data = await httpClient.get('/admin/events', { params })
      // console.log('[DEBUG] getAllEvents: 成功獲取活動列表，響應數據為:', data) // 新增除錯語句
      return data
    } catch (error) {
      console.error('獲取活動列表失敗:', error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 根據 ID 獲取單一活動的詳細資料。
   */
  async getEventById(eventId) {
    try {
      console.log(`[DEBUG] getEventById: 正在發送 GET 請求到 /admin/events/${eventId}`) // 新增除錯語句
      const response = await httpClient.get(`/admin/events/${eventId}`)
      console.log(`[DEBUG] getEventById: 成功獲取活動 ${eventId} 的詳細資料。`) // 新增除錯語句
      return response
    } catch (error) {
      console.error(`獲取活動 (ID: ${eventId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },
  /**
   * 【新增】根據活動 ID 獲取活動圖片
   * 由於圖片是直接的二進位數據，我們不需要特殊的處理，
   * 瀏覽器會直接將其作為圖片源加載。
   * 此處的 httpClient 實際上不會被直接調用，圖片會直接由 <img src="..."> 請求。
   */
  async getEventImage(eventId) {
    try {
      console.log(`[DEBUG] getEventImage: 正在發送 GET 請求到 /admin/events/${eventId}/image`)

      // 1. 透過 httpClient.get 請求圖片，並指定 responseType 為 'blob'
      // 2. 您的攔截器會處理成功響應，並直接返回 response.data，也就是 Blob 物件本身
      const imageBlob = await httpClient.get(`/admin/events/${eventId}/image`, { responseType: 'blob' });

      // 3. 確認收到的確實是 Blob 物件
      if (imageBlob instanceof Blob) {
        // 4. 直接用收到的 Blob 數據創建一個暫時的本地 URL
        return URL.createObjectURL(imageBlob);
      } else {
        // 防禦性程式碼，以防萬一 API 或攔截器行為不符預期
        console.error('預期獲取 Blob 物件，但收到的卻是:', imageBlob);
        return null; // 返回 null，讓前端可以顯示預設圖
      }
    } catch (error) {
      console.error(`獲取活動圖片 (ID: ${eventId}) 失敗`)
      // 錯誤會由攔截器統一處理並提示，這裡可以不再重複 console.error
      throw error
    }
  },
  /**
   * 新增一則活動。
   */
  async createEvent(eventData) {
    try {
      console.log('[DEBUG] createEvent: 正在發送 POST 請求到 /admin/events，請求主體為:', eventData) // 新增除錯語句
      const response = await httpClient.post('/admin/events', eventData)
      console.log('[DEBUG] createEvent: 成功新增活動，響應數據為:', response.data) // 新增除錯語句
      return response
    } catch (error) {
      console.error('新增活動失敗:', error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 更新一則活動。
   */
  async updateEvent(eventId, updatedData) {
    try {
      console.log(
        `[DEBUG] updateEvent: 正在發送 PUT 請求到 /admin/events/${eventId}，請求主體為:`,
        updatedData,
      ) // 新增除錯語句
      const response = await httpClient.put(`/admin/events/${eventId}`, updatedData)
      console.log(`[DEBUG] updateEvent: 成功更新活動 ${eventId}，響應數據為:`, response.data) // 新增除錯語句
      return response
    } catch (error) {
      console.error(`更新活動 (ID: ${eventId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 刪除一則活動。
   */
  async deleteEvent(eventId) {
    try {
      console.log(`[DEBUG] deleteEvent: 正在發送 DELETE 請求到 /admin/events/${eventId}`) // 新增除錯語句
      const response = await httpClient.delete(`/admin/events/${eventId}`)
      console.log(`[DEBUG] deleteEvent: 成功刪除活動 ${eventId}，響應數據為:`, response.data) // 新增除錯語句
      return response
    } catch (error) {
      console.error(`刪除活動 (ID: ${eventId}) 失敗:`, error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },

  /**
   * 根據 ID 列表批量刪除活動。
   */
  async deleteEventsInBatch(eventIds) {
    try {
      console.log(
        '[DEBUG] deleteEventsInBatch: 正在發送 DELETE 請求到 /admin/events/batch，請求主體為:',
        eventIds,
      ) // 新增除錯語句
      const response = await httpClient.delete('/admin/events/batch', {
        data: eventIds,
      })
      console.log('[DEBUG] deleteEventsInBatch: 成功批量刪除活動，響應數據為:', response.data) // 新增除錯語句
      return response
    } catch (error) {
      console.error('批量刪除活動失敗:', error)
      logAxiosError(error) // 呼叫共用除錯函式
      throw error
    }
  },
}

export default eventService

/**
 * 集中處理 Axios 錯誤日誌的輔助函式。
 * @param {object} error - Axios 錯誤物件
 */
function logAxiosError(error) {
  // 檢查 error 物件中是否有 response 屬性，這表示伺服器有響應
  if (error.response) {
    // 伺服器已響應，但狀態碼不是 2xx
    console.error('  -> 錯誤類型: 來自伺服器的響應 (非 2xx 狀態碼)')
    console.error('  -> 狀態碼:', error.response.status)
    console.error('  -> 錯誤資料:', error.response.data) // 這通常包含後端提供的具體錯誤訊息
    console.error('  -> 響應頭部:', error.response.headers)
  } else if (error.request) {
    // 請求已發出，但沒有收到響應
    console.error('  -> 錯誤類型: 請求無響應')
    console.error('  -> 請求物件:', error.request)
    // 這可能發生在網路問題、CORS 錯誤或伺服器根本沒有回應
  } else {
    // 在設定請求時發生錯誤，例如配置問題
    console.error('  -> 錯誤類型: 設定請求時發生錯誤')
    console.error('  -> 錯誤訊息:', error.message)
  }
}
