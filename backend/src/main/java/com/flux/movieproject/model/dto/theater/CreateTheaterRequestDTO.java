package com.flux.movieproject.model.dto.theater;

import java.util.List;

import com.flux.movieproject.model.entity.theater.Seat;

import lombok.Data;
@Data
public class CreateTheaterRequestDTO {
	private String theaterName;
	
	private Integer theaterTypeId;
	
	private Integer totalSeats;
	
	private String theaterPhoto;
	
	private List<Seat> seats;
}
