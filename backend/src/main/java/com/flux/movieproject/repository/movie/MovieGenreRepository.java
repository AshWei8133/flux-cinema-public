package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.entity.movie.MovieGenre;
import com.flux.movieproject.model.entity.movie.MovieGenreId;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, MovieGenreId> {

	/**
     * 根據電影 ID 刪除所有關聯的電影類型。
     * @param movieId 電影 ID
     */
    @Modifying // 修正點：標記為修改操作
    @Transactional // 修正點：確保在事務中執行
    @Query("DELETE FROM MovieGenre mg WHERE mg.id.movieId = :movieId") // 修正點：自定義刪除查詢
    void deleteByMovieId(@Param("movieId") Integer movieId); // 修正點：新增方法定義
}
