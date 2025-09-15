package com.flux.movieproject.model.dto.moviesession.viewseats;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDetailsDTO {
	private Integer sessionId; // 場次id
	private String movieTitle;
	private Integer theaterTypeId;
	private String version; // 電影版本 (影廳類型)
	private String theaterName;
	private LocalDateTime showtime; // 場次開始時間
}
