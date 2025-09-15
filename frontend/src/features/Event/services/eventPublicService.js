// src/features/Event/services/eventPublicService.js
import httpClient from '@/services/api'

const eventPublicService = {
  /**
   * 活動列表（分頁 + 可選篩選）
   * @param {{page?:number,size?:number,sort?:string,keyword?:string,cityId?:number,cinemaId?:number,startDateFrom?:string,startDateTo?:string}} params
   */
  async list(params = {}) {
    const q = {
      page: params.page ?? 0,
      size: params.size ?? 12,
      sort: params.sort ?? 'startDate,desc', // 依後端欄位調整
      keyword: params.keyword ?? '',
      cityId: params.cityId ?? undefined,
      cinemaId: params.cinemaId ?? undefined,
      startDateFrom: params.startDateFrom ?? undefined,
      startDateTo: params.startDateTo ?? undefined,
    }
    return httpClient.get('/events', { params: q })
  },

  /** 單筆詳情 */
  async getById(id) {
    return httpClient.get(`/events/${id}`)
  },

  /** 圖片（Blob） */
  async getImage(id) {
    return httpClient.get(`/events/${id}/image`, { responseType: 'blob' })
  },
}

export default eventPublicService
