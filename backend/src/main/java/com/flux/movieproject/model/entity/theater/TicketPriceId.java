package com.flux.movieproject.model.entity.theater;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPriceId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "theater_type_id", nullable = false)
	private Integer theaterTypeId;

	@Column(name = "ticket_type_id", nullable = false)
	private Integer ticketTypeId;

	@Override
	public int hashCode() {
		return Objects.hash(theaterTypeId, ticketTypeId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TicketPriceId other = (TicketPriceId) obj;
		return Objects.equals(theaterTypeId, other.theaterTypeId) && Objects.equals(ticketTypeId, other.ticketTypeId);
	}

}
