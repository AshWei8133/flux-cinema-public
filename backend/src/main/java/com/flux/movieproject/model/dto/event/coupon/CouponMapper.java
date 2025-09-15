package com.flux.movieproject.model.dto.event.coupon;

import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.event.EventEligibility;

import java.util.Base64;

public class CouponMapper {

    public static CouponResponse toResponse(Coupon coupon) {
        if (coupon == null) return null;

        // --- 從 eventEligibility 取關聯（避免調到不存在的 coupon.getMovie()/getSession()/getMemberLevel()）---
        EventEligibility eg = coupon.getEventEligibility();

        Integer movieId        = null;
        String  movieTitle     = null;
        Integer memberLevelId  = null;
        String  memberLevelName= null;
        Integer sessionId      = null;
        String  sessionLabel   = null; // 先留空，等你確認欄位（e.g. getSessionName()/getSessionTime()）

        if (eg != null) {
            if (eg.getMovie() != null) {
                movieId    = eg.getMovie().getId();     // 你的 Movie 主鍵看起來是 Integer
                movieTitle = eg.getMovie().getTitleEnglish();       // 依你的 Movie 實體欄位
            }
            if (eg.getMemberLevel() != null) {
                memberLevelId   = eg.getMemberLevel().getMemberLevelId();
                // 你前一個錯誤訊息顯示 getName() 不存在，通常 MemberLevel 會是 getLevelName()
                memberLevelName = eg.getMemberLevel().getLevelName();
            }
            if (eg.getSession() != null) {
                sessionId    = eg.getSession().getSessionId();
                // 你錯誤訊息顯示 MovieSession 沒有 getSessionName()，先別取，等確認欄位名
                // sessionLabel = eg.getSession().getSessionName();
                sessionLabel = null;
            }
        }

        // 圖片：byte[] -> base64
        String imageB64 = (coupon.getCouponImage() != null)
                ? Base64.getEncoder().encodeToString(coupon.getCouponImage())
                : null;
        

        return new CouponResponse(
                coupon.getCouponId(),
                coupon.getSerialNumber(),

                // Event
                (coupon.getEvent() != null ? coupon.getEvent().getTitle() : null),
                (coupon.getEvent() != null ? coupon.getEvent().getEventId() : null),

                // CouponCategory
                (coupon.getCouponCategory() != null ? coupon.getCouponCategory().getCouponCategoryId() : null),
                (coupon.getCouponCategory() != null ? coupon.getCouponCategory().getCouponCategoryName() : null),

                // 基本欄位
                coupon.getCouponName(),
                coupon.getCouponDescription(),

                // 折扣型別：你的 Coupon 沒欄位 -> 先回 null，前端會 fallback
                null,
                coupon.getDiscountAmount(),
                coupon.getMinimumSpend(),

                coupon.getStatus(),
                coupon.getRedeemableTimes(),
                coupon.getQuantity(),

                // 條件（從 EventEligibility 拿）
                movieId, movieTitle,
                memberLevelId, memberLevelName,
                sessionId, sessionLabel,

                imageB64
        );
    }
}
