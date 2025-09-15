package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.MovieDirector;
import com.flux.movieproject.model.entity.movie.MovieDirectorId;

public interface MovieDirectorRepository extends JpaRepository<MovieDirector, MovieDirectorId>{

}
