package com.flux.movieproject.model.dto.event.event;

public record EventCategoryDTO(
    Integer eventCategoryId,
    String eventCategoryName,
    String description
) {
}