package com.flux.movieproject.model.dto.event;

import java.time.LocalDate;


public record AnnouncementDTO(
    Integer announcementId,
    LocalDate publishDate,
    String title,
    String content, 
    String base64ImageString // Base64 字串或圖片 URL
) {}
