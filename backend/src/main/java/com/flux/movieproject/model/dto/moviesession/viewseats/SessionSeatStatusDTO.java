package com.flux.movieproject.model.dto.moviesession.viewseats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSeatStatusDTO {
	private Integer sessionSeatId;
    private String status;
    private SeatDTO seat; // 嵌套 SeatDTO，符合前端結構
}
