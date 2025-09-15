package com.flux.movieproject.config;

import java.util.Arrays;
import java.util.List;

public class ApiWhiteList {

    /**
     * 定義一個公開 API 的靜態列表 (白名單)
     * 團隊成員若有新增的公開 API，請將路徑模式 (Ant-style path pattern) 加入此列表
     * 範例:
     * - /api/movies/hot : 完全匹配的單一路徑
     * - /api/movies/** : 匹配 /api/movies/ 開頭的所有路徑 (包含子目錄)
     * - /api/articles/* : 匹配 /api/articles/ 後面接任何字串的路徑，但不包含子目錄 (例如 /api/articles/123, 但不匹配 /api/articles/123/comments)
     */
    public static final List<String> PUBLIC_API_LIST = Arrays.asList(
        "/api/member/**",      // 註冊與登入相關 API
        "/api/movie/**" ,   // 查詢電影資訊
        "/api/movieSession/**",// 查詢特定電影未來7天場次資訊
        "/api/public/product/**",
        "/api/payment/ecpay/notify", // 綠界金流支付
        "/api/payment/ecpay/result", // 綠界金流支付
        "/api/announcements/**", // 公告列表
        "/api/events/**", // 活動列表
        "/api/coupons/List" /* 前台優惠券清單（未登入也能看） */
        // --- 請團隊成員在此處加入新的公開 API 路徑 ---
        
    );

}