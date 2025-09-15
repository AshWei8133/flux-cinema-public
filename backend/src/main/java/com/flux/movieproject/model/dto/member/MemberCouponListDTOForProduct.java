package com.flux.movieproject.model.dto.member;

import com.flux.movieproject.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCouponListDTOForProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    /** member_coupon 主鍵 / MemberCoupon ID */
    private Integer memberCouponId;

    /** 所屬會員 ID / Member ID */
    private Integer memberId;

    /** 優惠券 ID / Coupon ID */
    private Integer couponId;

    /** 使用狀態：未使用 / 已使用 */
    private String status;

    /** 領取時間 / Acquisition time */
    private LocalDateTime acquisitionTime;

    /** 使用時間（若已使用則有值）/ Usage time */
    private LocalDateTime usageTime;

    // ===== 優惠券基本資訊，避免前端再額外查詢 =====
    /** 序號 / Serial number */
    private String serialNumber;

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

    /** Base64 圖片字串 / Coupon image as Base64 string */
    private String couponImageBase64;

    /** 重複發放優惠券序號不同 */
    private String displaySerial;

    private DiscountType discountType;
}
