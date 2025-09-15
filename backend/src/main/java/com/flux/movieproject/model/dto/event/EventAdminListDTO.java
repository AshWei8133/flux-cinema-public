package com.flux.movieproject.model.dto.event;

import java.time.LocalDate;

/**
 * 這個 DTO 專為後台「活動列表」頁面設計。
 * 它的特點是輕量級，只包含列表所需的欄位，
 * 並且不直接攜帶圖片的 Base64 內容，只用一個布林值標記圖片是否存在。
 */
public record EventAdminListDTO(
    Integer eventId,
    String title,
    String content, // 列表頁可能不需要完整內容，未來可考慮移除以進一步減輕重量
    Integer eventCategoryId,
    String eventCategoryName,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    Integer sessionCount,
    boolean hasImage // 核心：用布林值取代 Base64 字串
) {}
