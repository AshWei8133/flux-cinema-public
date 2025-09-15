package com.flux.movieproject.model.dto.moviesession.viewseats;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSeatLayoutDTO {
	private SessionDetailsDTO sessionInfo;
    private List<SessionSeatStatusDTO> seats;
}
