package com.flux.movieproject.model.entity.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
	@Getter
	@Setter
	@NoArgsConstructor
	@Table(name = "tmdb_movie_director")
	public class TMDBMovieDirector {
	    @EmbeddedId
	    private TMDBMovieDirectorId id = new TMDBMovieDirectorId();

	    @JsonIgnore
	    @ManyToOne(fetch = FetchType.LAZY)
	    @MapsId("tmdbMovieId")
	    @JoinColumn(name = "tmdb_movie_id")
	    private TMDBMovie tmdbMovie;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @MapsId("tmdbDirectorId") 
	    @JoinColumn(name = "tmdb_director_id") 
	    private TMDBDirector tmdbDirector;
	}
