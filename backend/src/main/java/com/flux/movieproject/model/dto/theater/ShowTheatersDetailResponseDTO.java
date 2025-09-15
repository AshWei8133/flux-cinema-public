package com.flux.movieproject.model.dto.theater;

import java.util.List;

import lombok.Data;

@Data
public class ShowTheatersDetailResponseDTO {
	private ShowTheatersResponseDTO showTheatersResponseDTO;
	private List<ShowSeatsResponseDTO> seats;
	
}
