package com.flux.movieproject.model.dto.event.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventEligibilityResponse {
 private Integer id;

 private Integer eventId;
 private String  eventTitle;

 private Integer movieId;
 private String  movieTitle;

 private Integer sessionId;
 private String  sessionLabel;   // 例：影廳/時間等你想怎麼組

 private Integer memberLevelId;
 private String  memberLevelName;
}