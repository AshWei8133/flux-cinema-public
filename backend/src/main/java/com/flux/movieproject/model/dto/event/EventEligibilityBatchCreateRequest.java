// 可選：批次建立
package com.flux.movieproject.model.dto.event;

import lombok.Data;
import java.util.List;

@Data
public class EventEligibilityBatchCreateRequest {
    private Integer eventId;  // 同一活動下批次建立
    private List<Integer> movieIds;
    private List<Integer> sessionIds;
    private List<Integer> memberLevelIds;
}