package com.flux.movieproject.repository.movie;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.movie.Genre;

public interface GenreRepository extends JpaRepository<Genre, Integer>{

	Optional<Genre> findByName(String name); // 根據名稱查找類型
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId); // 根據 TMDB ID 查找類型
    /**
     * 根據 ID 查詢類型的詳細資訊，並預先載入關聯的電影列表。
     * @param genreId 類型 ID
     * @return 類型實體，包含已載入的 movieGenres 及其關聯的 movie。
     */
    @EntityGraph(attributePaths = {"movieGenres", "movieGenres.movie"}) // 修正點：使用 EntityGraph 預先載入
    Optional<Genre> findGenreWithDetailsByGenreId(Integer genreId); // 新增方法，用於獲取詳細資訊

}
