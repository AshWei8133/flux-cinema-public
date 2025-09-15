package com.flux.movieproject.model.dto.ticket;

import lombok.Data;

@Data
public class TicketRequest {
	private Integer ticketTypeId;
	private Integer quantity;
	private Integer unitPrice;
}
