package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.model.entity.movie.TMDBMovieDirector;
import com.flux.movieproject.model.entity.movie.TMDBMovieDirectorId;

public interface TMDBMovieDirectorRepository extends JpaRepository<TMDBMovieDirector, TMDBMovieDirectorId> {
	void deleteByTmdbMovie(TMDBMovie tmdbMovie);
}
