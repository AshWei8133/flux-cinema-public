export interface CouponListItemDTO {
  couponId: number
  serialNumber?: string
  eventId?: number | null
  couponCategoryId?: number | null
  eventEligibilityId?: number | null
  couponName: string
  couponDescription?: string
  couponCategory?: string | null
  discountAmount?: number | null
  minimumSpend?: number | null
  status: '已領取' | '未領取'
  redeemableTimes?: number | null
  quantity?: number | null
  couponImageBase64?: string
}

export interface MemberCouponListDTO {
  memberCouponId: number
  memberId: number
  couponId: number
  status: '未使用' | '已使用'
  acquisitionTime?: string
  usageTime?: string
  serialNumber?: string
  couponName: string
  couponDescription?: string
  couponCategory?: string | null
  discountAmount?: number | null
  minimumSpend?: number | null
  couponImageBase64?: string
}

export interface PageResp<T> {
  content: T[]
  totalElements: number
  number: number // backend page index (1-based in our API)
  size: number
}
