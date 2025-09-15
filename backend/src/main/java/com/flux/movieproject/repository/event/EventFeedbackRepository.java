package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.EventFeedback;

public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Integer> {

}
