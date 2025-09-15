package com.flux.movieproject.repository.movie;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.movie.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

	/**
	 * 根據 TMDB 電影 ID 查詢本地的電影資料 (使用自定義查詢)
	 * 
	 * @param tmdbMovieId TMDB 的電影 ID
	 * @return 包含電影實體的 Optional 物件
	 */
	@Query("SELECT m FROM Movie m JOIN m.tmdbMovie tm WHERE tm.tmdbMovieId = :tmdbMovieId")
	Optional<Movie> findByTmdbMovieId(@Param("tmdbMovieId") Integer tmdbMovieId);

	/**
	 * 根據本地片名模糊查詢電影
	 * 
	 * @param titleLocal 片名關鍵字
	 * @return 符合條件的電影列表
	 */
	List<Movie> findByTitleLocalContainingIgnoreCase(String titleLocal);

	/**
	 * 根據英文片名模糊查詢電影
	 * 
	 * @param titleEnglish 片名關鍵字
	 * @return 符合條件的電影列表
	 */
	List<Movie> findByTitleEnglishContainingIgnoreCase(String titleEnglish);

	/**
	 * 根據類型名稱查詢電影
	 * 
	 * @param genreName 類型名稱
	 * @return 符合條件的電影列表
	 */
	@Query("SELECT DISTINCT m FROM Movie m JOIN m.movieGenres mg JOIN mg.genre g WHERE g.name = :genreName")
	List<Movie> findByGenreName(@Param("genreName") String genreName);

	/**
	 * 根據導演姓名查詢電影
	 * 
	 * @param directorName 導演姓名
	 * @return 符合條件的電影列表
	 */
	@Query("SELECT DISTINCT m FROM Movie m JOIN m.movieDirectors md JOIN md.director d WHERE d.name = :directorName")
	List<Movie> findByDirectorName(@Param("directorName") String directorName);

	/**
	 * 根據演員姓名查詢電影
	 * 
	 * @param actorName 演員姓名
	 * @return 符合條件的電影列表
	 */
	@Query("SELECT DISTINCT m FROM Movie m JOIN m.movieActors ma JOIN ma.actor a WHERE a.name = :actorName")
	List<Movie> findByActorName(@Param("actorName") String actorName);

	
	
	/**
	 * 查詢所有電影，並預先載入其關聯的類型。
	 * 
	 * @return 所有電影的列表，包含已載入的 movieGenres 及其關聯的 genre。
	 */
	@Override // 覆寫 JpaRepository 的 findAll 方法
	@EntityGraph(attributePaths = { "movieGenres", "movieGenres.genre" }) // 修正點：使用 EntityGraph 預先載入 movieGenres 和其內部的
																			// genre
	List<Movie> findAll();

	/**
	 * 取得當日上映中電影的候選列表(後續還要由service判斷下檔日期)
	 * 
	 * @param queryDate 上映的日期，用於判斷上映日期是否在這個日期之前
	 * @return 候選電影資訊
	 */
	@Query("SELECT m FROM Movie m WHERE " + "m.status = true AND " + "m.releaseDate <= :queryDate")
	List<Movie> findCandidateMoviesForNowPlaying(@Param("queryDate") LocalDate queryDate);

	/**
	 * 即將上映電影的候選列表(後續還要由service判斷上檔日期)
	 * 
	 * @param queryDate 上映的日期，用於判斷上映日期是否在這個日期之前
	 * @return 候選電影資訊
	 */
	@Query("SELECT m FROM Movie m WHERE " + "m.status = true AND " + "m.releaseDate > :queryDate")
	List<Movie> findMoviesForComingSoon(@Param("queryDate") LocalDate queryDate);
}
