package com.flux.movieproject.model.dto.moviesession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.flux.movieproject.enums.MovieSessionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSessionDailyOverviewDTO {
	private LocalDate date;
	private MovieSessionStatus status;
	private Map<Integer, List<MovieSessionOverviewResponseDTO>> dailySessionsByTheaterId;
}