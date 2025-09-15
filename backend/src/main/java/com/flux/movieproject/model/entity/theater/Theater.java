package com.flux.movieproject.model.entity.theater;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
public class Theater {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer theaterId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "theater_type_id")
	private TheaterType theaterType;
	
	private String theaterName;
	
	private Integer totalSeats;
	
	private byte[] theaterPhoto;
	
	@JsonIgnoreProperties("theater")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL , mappedBy = "theater",orphanRemoval = true)
	private List<Seat> seats = new ArrayList<Seat>();
	
	@JsonIgnoreProperties("theater")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL , mappedBy = "theater", orphanRemoval = true)
	private List<MovieSession> movieSessions = new ArrayList<>();
	
}
