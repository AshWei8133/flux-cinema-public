package com.flux.movieproject.model.dto.event; // 建議的套件路徑

import java.time.LocalDate;

public record EventDTO(
		Integer eventId,
	    String title,
	    String content,
	    Integer eventCategoryId,
	    String eventCategoryName,
	    String status,
	    LocalDate startDate,
	    LocalDate endDate,
	    Integer sessionCount,
	    String base64ImageString
) {}

