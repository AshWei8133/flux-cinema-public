package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.EventEligibility;

public interface EventEligibilityRepository extends JpaRepository<EventEligibility, Integer> {

}
