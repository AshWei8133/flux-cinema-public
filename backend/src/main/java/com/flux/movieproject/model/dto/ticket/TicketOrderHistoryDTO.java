package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDateTime;

public interface TicketOrderHistoryDTO {
	Integer getTicketOrderId();

	LocalDateTime getCreatedTime();

	String getStatus();

	Integer getTotalAmount();

	String getMovieTitle();

	LocalDateTime getStartTime();

	String getTheaterName();

	String getSeatNumber();
}
