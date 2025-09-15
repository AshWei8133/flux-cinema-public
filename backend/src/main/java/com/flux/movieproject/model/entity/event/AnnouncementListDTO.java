package com.flux.movieproject.model.entity.event;

import java.time.LocalDate;

public record AnnouncementListDTO(
 Integer announcementId,
 String title,
 LocalDate publishDate,
 String content
) {}

