import { defineStore } from 'pinia'
import http from '@/services/api'

export const usePublicCouponListStore = defineStore('couponMarket', {
  state: () => ({
    list: [],
    total: 0,
    page: 1,
    size: 12,
    keyword: '',
    loading: false,
  }),
  actions: {
    async reload() {
      this.loading = true
      try {
        const params = { page: this.page, size: this.size }
        if (this.keyword && this.keyword.trim()) {
          params.keyword = this.keyword.trim()
        }

        const pageResp = await http.get('/coupons/List', { params })
        console.log(params);
        console.log(pageResp);
        this.list = pageResp?.content || []
        this.total = pageResp?.totalElements || 0
      } finally {
        this.loading = false
      }
    },
    async claim(couponId, options) {
      const idx = this.list.findIndex((x) => x.couponId === couponId)
      const backup = idx >= 0 ? { ...this.list[idx] } : null

      if (idx >= 0) this.list[idx].status = '已領取' // 樂觀更新
      try {
        await http.post(`/coupons/${couponId}/claim`)
        // if (options && options.redirectToMemberCenter) {
        //   router.push({ name: 'MemberProfile', query: { view: 'coupon', from: 'claim', couponId } })
        // }
      } catch (e) {
        if (idx >= 0 && backup) this.list[idx] = backup // 回滾
        throw e
      }
    },
  },
})
