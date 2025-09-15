import { defineStore } from 'pinia'
import httpClient from '@/services/api' // baseURL=/api，response = data

export const useMemberCouponListStore = defineStore('memberCoupon', {
  state: () => ({
    list: [],
    total: 0,
    page: 1,
    size: 12,
    keyword: '',
    status: 'ALL', // ALL / 未使用 / 已使用
    loading: false,
  }),
  actions: {
    async reload() {
      this.loading = true
      try {
        const params = { page: this.page, size: this.size, status: this.status }
        if (this.keyword && this.keyword.trim()) params.keyword = this.keyword.trim()

        const pageResp = await httpClient.get('/coupons/me', { params })

        this.list = pageResp?.content || []
        this.total = pageResp?.totalElements || 0
      } finally {
        this.loading = false
      }
    },
  },
})
