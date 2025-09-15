// src/features/Event/services/announcementPublicService.js

import httpClient from '@/services/api'

// 使用共用的 httpClient（已含 baseURL=/api、token、錯誤處理與 data-only 回傳）
/**
 * 前台公告 API（公開端點，不需要會員/管理員 token）
 */
const announcementPublicService = {
  /**
   * 取得公告列表
   * @param {{ page?: number, size?: number, sort?: string, keyword?: string, search?: string }} params
   * @returns {Promise<any>}
   */
  async list(params = {}) {
    const q = {
      page: params.page ?? 0,
      size: params.size ?? 10,
      sort: params.sort ?? 'publishDate,desc',
      keyword: params.keyword ?? params.search ?? '',
    }
    return httpClient.get('/announcements', { params: q })
  },

  /**
   * 取得單筆詳情
   * @param {string|number} id
   * @returns {Promise<any>}
   */
  async getById(id) {
    return httpClient.get(`/announcements/${id}`)
  },

  /**
   * 取得公告圖片（Blob）
   * @param {string|number} id
   * @returns {Promise<Blob>}
   */
  async getImage(id) {
    return httpClient.get(`/announcements/${id}/image`, { responseType: 'blob' })
  },
}

export default announcementPublicService
