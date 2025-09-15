package com.flux.movieproject.repository.theater;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.theater.TheaterType;

public interface TheaterTypeRepository extends JpaRepository<TheaterType, Integer> {
	boolean existsByTheaterTypeName(String theaterTypeName);
}
