package com.flux.movieproject.model.dto.moviesession;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovieSessionDateResponseDTO {
	private Integer sessionId;
	private Integer theaterId;
	private Integer movieId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
