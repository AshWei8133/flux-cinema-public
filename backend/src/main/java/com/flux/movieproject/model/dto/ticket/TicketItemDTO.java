package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketItemDTO {
	private String movieTitle;
    private LocalDateTime startTime;
    private String theaterName;
    private String seatPosition;
    private String ticketTypeName;
    private Integer unitPrice;
}
