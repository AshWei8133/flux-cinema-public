package com.flux.movieproject.model.dto.event.event;

import lombok.Data;

@Data
public class EventEligibilityCreateRequest {
    private Integer eventId;        // 必填
    private Integer movieId;        // 選填
    private Integer sessionId;      // 選填
    private Integer memberLevelId;  // 選填
}

