package com.flux.movieproject.model.dto.event;

public record EventCategoryDTO(
    Integer eventCategoryId,
    String eventCategoryName,
    String description
) {
}