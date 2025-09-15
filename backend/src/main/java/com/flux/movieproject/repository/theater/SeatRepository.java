package com.flux.movieproject.repository.theater;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.theater.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer>{

}
