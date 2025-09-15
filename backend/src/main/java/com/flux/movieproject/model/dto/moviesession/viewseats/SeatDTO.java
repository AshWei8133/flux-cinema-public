package com.flux.movieproject.model.dto.moviesession.viewseats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
	private Integer seatId;
    private String seatType;
    private String rowNumber;
    private Integer columnNumber;
}
