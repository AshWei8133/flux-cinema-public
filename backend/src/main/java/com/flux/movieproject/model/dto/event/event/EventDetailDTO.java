package com.flux.movieproject.model.dto.event.event;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventDetailDTO {
    private Integer eventId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String content;
    private String base64ImageString;
}
