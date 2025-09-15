package com.flux.movieproject.model.dto.moviesession;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovieSessionUpdateDTO {
	private Long sessionId;

	private Integer movieId;
	private Integer theaterId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
