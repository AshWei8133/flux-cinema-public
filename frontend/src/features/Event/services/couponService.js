import httpClient from '@/services/api'

export async function fetchCouponMarket(params) {
  const query = {}
  if (params) {
    if (params.keyword) query.keyword = params.keyword
    if (typeof params.page === 'number') query.page = params.page
    if (typeof params.size === 'number') query.size = params.size
  }
  return httpClient.get('/coupons/market', { params: query })
}

export async function claimCoupon(couponId) {
  return httpClient.post('/member/coupons/claim/' + String(couponId))
}

export async function fetchMyCoupons(params) {
  const query = {}
  if (params) {
    if (params.status) query.status = params.status
    if (params.keyword) query.keyword = params.keyword
    if (typeof params.page === 'number') query.page = params.page
    if (typeof params.size === 'number') query.size = params.size
  }
  return httpClient.get('/member/coupons', { params: query })
}
