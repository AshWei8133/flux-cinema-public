package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class TicketOrderSearchRequestDTO {
	private int page = 1;
	private int pageSize = 10;

	private String orderNumber;
	private String username;
	private String email;
	private String phone;
	private String status;
	private String paymentType;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;
}
