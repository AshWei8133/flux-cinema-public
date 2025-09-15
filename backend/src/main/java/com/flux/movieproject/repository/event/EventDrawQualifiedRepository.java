package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.EventDrawQualified;

public interface EventDrawQualifiedRepository extends JpaRepository<EventDrawQualified, Integer> {

}
