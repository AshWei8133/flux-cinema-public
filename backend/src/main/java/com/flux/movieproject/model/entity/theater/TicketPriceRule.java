package com.flux.movieproject.model.entity.theater;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TicketPriceRule {

	@EmbeddedId
	private TicketPriceId ticketPriceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("theaterTypeId")
	@JoinColumn(name = "theater_type_id")
	private TheaterType theaterType;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("ticketTypeId")
	@JoinColumn(name = "ticket_type_id")
	private TicketType ticketType;

	private Integer price;

	@Column(name = "valid_s_date")
	private LocalDateTime validSDate;

	@Column(name = "valid_e_date")
	private LocalDateTime validEDate;

	private String description;

}
