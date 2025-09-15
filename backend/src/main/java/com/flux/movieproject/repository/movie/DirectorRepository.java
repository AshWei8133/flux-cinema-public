package com.flux.movieproject.repository.movie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.Director;

public interface DirectorRepository extends JpaRepository<Director, Integer>{

	Optional<Director> findByName(String name); // 根據姓名查找導演
    Optional<Director> findByTmdbDirectorId(Integer tmdbDirectorId); // 根據 TMDB ID 查找導演
}
