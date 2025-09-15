
// 領取優惠券專區列表用
package com.flux.movieproject.model.dto.event.coupon;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CouponListItemDTO {
	private Integer couponId;
    private String serialNumber;          // 序號
    private Integer eventId;              // coupon.event_id
    private Integer couponCategoryId;     // coupon.coupon_category_id
    private Integer eventEligibilityId;   // coupon.event_eligibility_id（若為 NULL 則前端可忽略）

    private String couponName;            // coupon.coupon_name
    private String couponDescription;     // coupon.coupon_description
    private String couponCategory;        // coupon.coupon_category（字符串版）
    private Integer discountAmount;       // 折扣金額
    private Integer minimumSpend;         // 最少花費多少才能使用此優惠券
    private String status;                // 狀態（已領取、未領取）
    private Integer redeemableTimes;      // 每人可使用次數
    private Integer quantity;             // 總發放量
    private String couponImageBase64;     // coupon.coupon_image → Base64
    

    
}