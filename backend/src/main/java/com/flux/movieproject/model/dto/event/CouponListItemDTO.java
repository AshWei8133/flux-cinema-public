package com.flux.movieproject.model.dto.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CouponListItemDTO
 *
 * 前台「優惠券專區」使用的 DTO。
 * - 包含優惠券的基本資訊
 * - 包含當前會員是否已領取的狀態（已領取 / 未領取）
 * - 提供 Base64 圖片字串，方便前端直接顯示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponListItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 優惠券主鍵 / Coupon ID */
    private Integer couponId;

    /** 系統產生的序號 / System-generated serial number */
    private String serialNumber;

    /** 關聯活動 ID / Related event ID */
    private Integer eventId;

    /** 優惠券類別 ID / Coupon category ID */
    private Integer couponCategoryId;

    /** 活動資格 ID（可選）/ Event eligibility ID (optional) */
    private Integer eventEligibilityId;

    /** 優惠券名稱 / Coupon name */
    private String couponName;

    /** 優惠券描述 / Coupon description */
    private String couponDescription;

    /** 優惠券類別名稱 / Coupon category name */
    private String couponCategory;

    /** 折抵金額 / Discount amount */
    private Integer discountAmount;

    /** 最低消費金額 / Minimum spend */
    private Integer minimumSpend;

    /**
     * 狀態（由會員角度決定）
     * - "已領取" / "未領取"
     * Claim status for current member ("已領取" or "未領取")
     */
    private String status;
    
    private Long myClaimCount;

    /** 可使用次數 / Redeemable times */
    private Integer redeemableTimes;

    /** 總數量 / Available quantity */
    private Integer quantity;

    /** Base64 圖片字串 / Coupon image as Base64 string */
    private String couponImageBase64;
}
