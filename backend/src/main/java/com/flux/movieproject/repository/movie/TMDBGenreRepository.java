package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBGenre;

public interface TMDBGenreRepository extends JpaRepository<TMDBGenre, Integer>{

}
