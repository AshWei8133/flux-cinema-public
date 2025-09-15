package com.flux.movieproject.model.dto.event;

// 只回前端會用到的欄位
public record CouponResponse(
    Integer couponId,
    String  serialNumber,

    String  eventTitle,
    Integer eventId,

    Integer couponCategoryId,
    String  couponCategoryName,

    String  couponName,
    String  couponDescription,

    String  discountType,     // 目前你的 Coupon 沒這欄，先留著給前端相容，可為 null
    Integer discountAmount,
    Integer minimumSpend,

    String  status,
    Integer redeemableTimes,
    Integer quantity,

    Integer movieId,          String movieTitle,
    Integer memberLevelId,    String memberLevelName,
    Integer sessionId,        String sessionLabel,

    String  couponImageBase64 // 從 byte[] 轉 base64 給前端
) {}
