package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBMovie;

public interface TMDBMovieRepository extends JpaRepository<TMDBMovie, Integer>{

}
