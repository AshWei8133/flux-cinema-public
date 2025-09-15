package com.flux.movieproject.model.entity.theater;

import com.flux.movieproject.enums.OrderDetailStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TicketOrderDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ticketOrderDetailId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_order_id")
	private TicketOrder ticketOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_seat_id", nullable = false) 
	private SessionSeat sessionSeat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id")
	private TicketType ticketType;

	private Integer unitPrice;
	
	@Enumerated(EnumType.STRING)
    private OrderDetailStatus status;

}
