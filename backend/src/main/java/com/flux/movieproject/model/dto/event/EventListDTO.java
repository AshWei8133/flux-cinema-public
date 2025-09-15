package com.flux.movieproject.model.dto.event;

import java.time.LocalDate;

public record EventListDTO(
        Integer eventId,
        String title,
        LocalDate startDate,
        LocalDate endDate
) {}
