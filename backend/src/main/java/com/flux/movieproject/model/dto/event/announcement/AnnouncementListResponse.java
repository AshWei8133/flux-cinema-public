package com.flux.movieproject.model.dto.event.announcement;

import java.util.List;

// Java Record 定義包含總數和列表的響應
public record AnnouncementListResponse(
    Long count,
    List<AnnouncementDTO> list
) {}
