package com.flux.movieproject.model.dto.event.coupon;

public record CouponUpdateRequest(
    String couponName,
    String couponDescription,
    String status,
    Integer discountAmount,
    Integer minimumSpend,
    Integer quantity,
    Integer redeemableTimes,
    String couponImageBase64
) {}
