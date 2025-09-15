package com.flux.movieproject.model.dto.theater;

import lombok.Data;

@Data
public class ShowSeatsResponseDTO {
	private Integer seatId;
	private String seatType;
	private String rowNumber;
	private Integer columnNumber;
	private Boolean isActive;
}
