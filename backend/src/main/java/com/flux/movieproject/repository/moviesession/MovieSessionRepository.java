package com.flux.movieproject.repository.moviesession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.model.entity.theater.SessionSeat;

public interface MovieSessionRepository extends JpaRepository<MovieSession, Integer> {

	/**
	 * 取得場次起始區間介於某個時段的資料
	 * 
	 * @param start 區間起始時間
	 * @param end   區間結束時間
	 * @return 符合條件的場次資料
	 */
	List<MovieSession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

	/**
	 * 根據開始時間區間刪除場次
	 * 
	 * @param start
	 * @param end
	 */
	void deleteByStartTimeBetween(LocalDateTime start, LocalDateTime end);

	/**
	 * 查詢特定電影某段區間內的場次
	 * 
	 * @param movieId 電影id
	 * @param start   區間開始時間
	 * @param end     區間結束時間
	 * @return 符合條件的場次資料
	 */
	@Query("SELECT ms FROM MovieSession ms " + "WHERE ms.movie.id = :movieId "
			+ "AND ms.startTime BETWEEN :start AND :end "
			+ "ORDER BY ms.theater.theaterType.theaterTypeId , ms.startTime ASC")
	List<MovieSession> findShowtimesForMovie(@Param("movieId") Integer movieId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	/**
	 * 第一步：查詢 MovieSession 及其 To-One 關聯的實體。 這個查詢非常快，因為它總是只返回一筆資料列。
	 */
	@Query("SELECT ms FROM MovieSession ms " + "JOIN FETCH ms.movie " + "JOIN FETCH ms.theater t "
			+ "JOIN FETCH t.theaterType " + "WHERE ms.sessionId = :sessionId")
	Optional<MovieSession> findSessionWithToOneRelationsById(@Param("sessionId") Integer sessionId);

	/**
	 * 第二步：專門查詢 SessionSeat 及其關聯的實體。 透過傳入第一步查到的 MovieSession 物件，Hibernate
	 * 會自動將結果關聯起來。
	 */
	@Query("SELECT ss FROM SessionSeat ss " + "LEFT JOIN FETCH ss.seat " + "LEFT JOIN FETCH ss.ticketOrderDetails "
			+ "WHERE ss.movieSession = :movieSession")
	List<SessionSeat> findSessionSeatsWithDetailsBySession(@Param("movieSession") MovieSession movieSession);
	
	
	// 為了實作前端電影詳細資料中的場次座位呈現提供的資料庫方法
	/**
     * 【第一查】: 查詢主要的 MovieSession，並 JOIN FETCH 所有 ToOne 關聯。
     * 這個查詢【不會】去抓取 sessionSeats 集合，從而避免了笛卡爾積。
     */
    @Query("SELECT DISTINCT ms FROM MovieSession ms " +
           "JOIN FETCH ms.theater t " +
           "JOIN FETCH t.theaterType " +
           "WHERE ms.movie.id = :movieId AND ms.startTime BETWEEN :startTime AND :endTime " +
           "ORDER BY ms.startTime ASC")
    List<MovieSession> findShowtimesWithToOneRelations(
        @Param("movieId") Integer movieId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 【第二查】: 根據第一查的結果，一次性抓取所有相關的 SessionSeat 集合。
     * Hibernate 會自動將查出來的 SessionSeat 關聯回傳入的 MovieSession 物件中。
     */
    @Query("SELECT ss FROM SessionSeat ss " +
           "WHERE ss.movieSession IN :sessions")
    List<SessionSeat> findSessionSeatsForSessions(@Param("sessions") List<MovieSession> sessions);

}
