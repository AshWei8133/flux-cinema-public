package com.flux.movieproject.model.entity.theater;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flux.movieproject.model.entity.movie.Movie;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
public class MovieSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer sessionId;

	@JsonIgnoreProperties("movieSessions")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "theaterId")
	private Theater theater;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movieId")
	private Movie movie;

	private LocalDateTime startTime;

	private LocalDateTime endTime;
	/**
	 * 新增與場次座位表的關聯
	 */
	@OneToMany(mappedBy = "movieSession", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<SessionSeat> sessionSeats = new ArrayList<>();

}
