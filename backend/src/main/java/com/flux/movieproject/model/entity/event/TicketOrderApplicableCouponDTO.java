package com.flux.movieproject.model.entity.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 票券訂單適用的優惠券
 */
@Data
@AllArgsConstructor
public class TicketOrderApplicableCouponDTO {
	private Integer memberCouponId;

	private String couponName;
	private String couponDescription;
	private BigDecimal discountAmount;
	private Integer minimumSpend;
	private boolean isUsable;
}
