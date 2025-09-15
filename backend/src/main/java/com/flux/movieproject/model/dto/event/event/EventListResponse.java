package com.flux.movieproject.model.dto.event.event; // 建議的套件路徑

import java.util.List;

/**
 * 活動列表響應資料傳輸物件 (DTO)。
 * 包含活動的總數和 EventDTO 列表。
 */
public record EventListResponse(
    Long count,             // 活動總數
    List<EventDTO> list     // EventDTO 列表
) {}

