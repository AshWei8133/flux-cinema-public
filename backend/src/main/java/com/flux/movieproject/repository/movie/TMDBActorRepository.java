package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.TMDBActor;

public interface TMDBActorRepository extends JpaRepository<TMDBActor, Integer>{

}
