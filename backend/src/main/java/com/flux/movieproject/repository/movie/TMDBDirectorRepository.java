package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBDirector;

public interface TMDBDirectorRepository extends JpaRepository<TMDBDirector, Integer>{

}
