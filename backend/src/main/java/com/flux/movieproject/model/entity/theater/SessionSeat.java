package com.flux.movieproject.model.entity.theater;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flux.movieproject.enums.SeatStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SessionSeat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer sessionSeatId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id")
	private MovieSession movieSession;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id")
	private Seat seat;

	@Enumerated(EnumType.STRING)
	private SeatStatus status;

	private LocalDateTime reservedDate;

	private LocalDateTime reservedExpiredDate;

	@OneToMany(mappedBy = "sessionSeat")
	@JsonIgnore
	private List<TicketOrderDetail> ticketOrderDetails = new ArrayList<>();

}
