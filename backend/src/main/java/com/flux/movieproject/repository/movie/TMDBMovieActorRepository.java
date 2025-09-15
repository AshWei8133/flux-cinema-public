package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.model.entity.movie.TMDBMovieActor;
import com.flux.movieproject.model.entity.movie.TMDBMovieActorId;

public interface TMDBMovieActorRepository extends JpaRepository<TMDBMovieActor, TMDBMovieActorId> {
	void deleteByTmdbMovie(TMDBMovie tmdbMovie);
}
