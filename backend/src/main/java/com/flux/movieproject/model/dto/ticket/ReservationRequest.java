package com.flux.movieproject.model.dto.ticket;

import java.util.List;

import lombok.Data;

@Data
public class ReservationRequest {
	private Integer sessionId;
	private List<Integer> seatIds; // 這裡是 SessionSeat 的 ID 列表
	private String paymentMethod; // "online" 或 "counter"
	private List<TicketRequest> tickets;
}
