package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.model.entity.movie.TMDBMovieGenre;
import com.flux.movieproject.model.entity.movie.TMDBMovieGenreId;

public interface TMDBMovieGenreRepository extends JpaRepository<TMDBMovieGenre, TMDBMovieGenreId> {
	void deleteByTmdbMovie(TMDBMovie tmdbMovie);
}
