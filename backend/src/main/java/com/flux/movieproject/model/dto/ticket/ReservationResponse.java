package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
	private Integer ticketOrderId;
	private LocalDateTime reservedExpiredDate;
}
