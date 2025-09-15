import httpClient from '@/services/api'
// 引入已配置的 axios 實例

/**
 * AnnouncementService 負責處理所有與公告相關的 API 請求
 */
const announcementService = {
  /**
   * 獲取所有公告的列表
   * @param {object} params - 包含所有查詢條件的物件，例如 { title, sortBy, sortOrder }
   * @returns {Promise<Object>} 後端響應的資料
   */
  async getAllAnnouncements(params = {}) {
    try {
      // 將整個 params 物件交給 Axios 的 params 選項處理。
      // Axios 會自動將其轉換為正確的 URL 查詢字串，
      // 例如：/admin/announcements?sortBy=publishDate&sortOrder=desc
      const response = await httpClient.get('/admin/announcements', { params })
      return response // Axios 攔截器會處理 .data，所以這裡直接回傳 response
    } catch (error) {
      console.error('獲取公告列表失敗:', error)
      throw error
    }
  },

  /**
   * 獲取所有公告的摘要資訊 (DTO 列表)
   * @returns {Promise<Object>} 後端響應的資料
   */
  async getAllAnnouncementsDTO() {
    try {
      const response = await httpClient.get('/admin/announcements/dto')
      return response
    } catch (error) {
      console.error('獲取公告摘要列表失敗:', error)
      throw error
    }
  },

  /**
   * 根據 ID 獲取單一公告的詳細資料
   * @param {number} announcementId - 公告的唯一 ID
   * @returns {Promise<Object>} 後端響應的資料
   */
  async getAnnouncementById(announcementId) {
    try {
      const response = await httpClient.get(`/admin/announcements/${announcementId}`)
      return response
    } catch (error) {
      console.error(`獲取公告 (ID: ${announcementId}) 失敗:`, error)
      throw error
    }
  },

  /**
   * 新增一則公告
   * @param {Object} announcementData - 包含新公告資料的物件
   * @returns {Promise<Object>} 後端響應的資料
   */
  async createAnnouncement(announcementData) {
    try {
      const response = await httpClient.post('/admin/announcements', announcementData)
      return response
    } catch (error) {
      console.error('新增公告失敗:', error)
      throw error
    }
  },

  /**
   * 更新一則公告
   * @param {number} announcementId - 要更新的公告 ID
   * @param {Object} updatedData - 包含更新後資料的物件
   * @returns {Promise<Object>} 後端響應的資料
   */
  async updateAnnouncement(announcementId, updatedData) {
    try {
      const response = await httpClient.put(`/admin/announcements/${announcementId}`, updatedData)
      return response
    } catch (error) {
      console.error(`更新公告 (ID: ${announcementId}) 失敗:`, error)
      throw error
    }
  },

  /**
   * 刪除一則公告
   * @param {number} announcementId - 要刪除的公告 ID
   * @returns {Promise<Object>} 後端響應的資料
   */
  async deleteAnnouncement(announcementId) {
    try {
      const response = await httpClient.delete(`/admin/announcements/${announcementId}`)
      return response
    } catch (error) {
      console.error(`刪除公告 (ID: ${announcementId}) 失敗:`, error)
      throw error
    }
  },

  /**
   * 獲取公告圖片
   * @param {number} announcementId - 公告 ID
   * @returns {Promise<Object>} 後端響應的資料 (通常是圖片的 Blob 或 ArrayBuffer)
   */
  async getAnnouncementImage(announcementId) {
    try {
      const response = await httpClient.get(`/admin/announcements/${announcementId}/image`, {
        responseType: 'blob',
      })
      return response
    } catch (error) {
      console.error(`獲取公告圖片 (ID: ${announcementId}) 失敗:`, error)
      throw error
    }
  },
}

export default announcementService
