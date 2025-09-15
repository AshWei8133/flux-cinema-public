package com.flux.movieproject.model.dto.event;

import com.flux.movieproject.enums.DiscountType;

import lombok.Data;

@Data
public class CouponDTO {

	private Integer couponId;

	private String serialNumber;

	private String couponName;

	private String couponDescription;

	private String eventTitle; // 關聯活動的標題，而非整個 Event 物件

	private Integer eventId; // 關聯活動的 ID

	private String couponCategoryName; // 關聯類別的名稱，而非整個 CouponCategory 物件

	private Integer couponCategoryId; // 關聯類別的 ID

	// 圖片通常會以 Base64 字串的形式傳輸，而不是二進位位元組
	private String couponImageBase64;

	private Integer discountAmount;

	private Integer minimumSpend;

	private String status;

	private Integer redeemableTimes;

	private Integer quantity;

	private Integer movieId;

	private Integer memberLevelId;

	private Integer sessionId;
	
	private DiscountType discountType;
}
