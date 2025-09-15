package com.flux.movieproject.model.dto.moviesession.movieshowtimes;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
	private Integer sessionId;
	private LocalDateTime startTime;
	private Integer theaterTypeId;
	private String theaterTypeName;
	private Integer totalSeats;
	private Long availableSeats;
}
