package com.flux.movieproject.repository.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.theater.TicketType;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {

}
