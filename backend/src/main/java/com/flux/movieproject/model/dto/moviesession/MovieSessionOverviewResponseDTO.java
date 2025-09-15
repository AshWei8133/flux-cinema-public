package com.flux.movieproject.model.dto.moviesession;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MovieSessionOverviewResponseDTO {
	private Integer sessionId;
	private String titleLocal;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
