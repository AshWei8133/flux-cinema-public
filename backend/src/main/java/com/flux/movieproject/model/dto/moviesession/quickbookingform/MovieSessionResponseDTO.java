package com.flux.movieproject.model.dto.moviesession.quickbookingform;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovieSessionResponseDTO {
	private Integer sessionId;
	private LocalDateTime startTime;
	private TheaterDTO theater;
}
