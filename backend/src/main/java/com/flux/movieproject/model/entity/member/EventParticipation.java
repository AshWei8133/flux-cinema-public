package com.flux.movieproject.model.entity.member;

import java.time.LocalDateTime;

import com.flux.movieproject.model.entity.event.Event;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "event_participation")
public class EventParticipation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventRecordId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	private String eventCategory;

	private LocalDateTime participationTime;

}
