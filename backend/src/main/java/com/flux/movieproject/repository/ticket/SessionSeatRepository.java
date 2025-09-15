package com.flux.movieproject.repository.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.enums.SeatStatus;
import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.model.entity.theater.SessionSeat;

import jakarta.persistence.LockModeType;

public interface SessionSeatRepository extends JpaRepository<SessionSeat, Integer> {

	/**
	 * 檢查場次是否有被訂位或鎖定的紀錄
	 * 
	 * @param session         場次資訊
	 * @param allowedStatuses 不要被擷取的狀態(這裡為可用)
	 * @return 是否有不該刪除的場次存在
	 */
	boolean existsByMovieSessionAndStatusNotIn(MovieSession session, List<SeatStatus> statuses);
	
	/**
     * 【新增】批次化檢查：檢查傳入的場次列表中，是否有任何一個場次的座位狀態不是 AVAILABLE。
     * @param sessions 要檢查的場次列表
     * @param statuses 要排除的狀態 (這裡會是 AVAILABLE)
     * @return 如果存在任何被佔用的座位，返回 true
     */
    boolean existsByMovieSessionInAndStatusNotIn(List<MovieSession> sessions, List<SeatStatus> statuses);
    
    
    /**
     * 【新增】使用單一 DELETE 語句批次刪除多個場次的所有座位狀態記錄。
     * @Modifying 告訴 Spring Data JPA 這是一個會修改資料庫的查詢。
     */
    @Modifying
    @Query("DELETE FROM SessionSeat ss WHERE ss.movieSession IN :sessions")
    void deleteByMovieSessionIn(@Param("sessions") List<MovieSession> sessions);
    
    /**
     * 【新增】使用原生 SQL 的 INSERT INTO ... SELECT ... 語法，為新建立的場次批次生成座位狀態。
     * 這是最高效的方式，因為它避免了在 Java 層和資料庫之間傳輸大量的 Seat 數據。
     * @param sessionIds 新儲存的 MovieSession 的 ID 列表。
     */
    @Modifying
    @Query(value = 
        "INSERT INTO session_seat (session_id, seat_id, status) " +
        "SELECT ms.session_id, s.seat_id, 'AVAILABLE' " +
        "FROM movie_session ms " +
        "JOIN theater t ON ms.theater_id = t.theater_id " +
        "JOIN seat s ON t.theater_id = s.theater_id " +
        "WHERE ms.session_id IN :sessionIds",
        nativeQuery = true)
    void bulkCreateSessionSeatsForNewSessions(@Param("sessionIds") List<Integer> sessionIds);

	/**
	 * 根據 ID 列表查詢並以悲觀寫鎖鎖定 SessionSeat 記錄。
	 * 當這個查詢在一個交易(Transaction)中被執行時，資料庫會鎖定這些記錄所在的行，
	 * 其他試圖讀取並鎖定或修改這些行的交易將會被阻塞，直到本次交易完成。 這能完美防止 race condition。
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT ss FROM SessionSeat ss WHERE ss.sessionSeatId IN :ids")
	List<SessionSeat> findAndLockBySessionSeatIdIn(List<Integer> ids);
	
	
	/**
     * 【新增】使用單一 UPDATE 語句，批次釋放多個座位。
     * 這是最高效的方式，可以一次性將所有相關座位的狀態改回 AVAILABLE。
     * @param sessionSeatIds 要釋放的 SessionSeat 的 ID 列表
     */
    @Modifying
    @Query("UPDATE SessionSeat ss SET ss.status = com.flux.movieproject.enums.SeatStatus.AVAILABLE, ss.reservedExpiredDate = null WHERE ss.sessionSeatId IN :sessionSeatIds")
    void releaseSeats(@Param("sessionSeatIds") List<Integer> sessionSeatIds);
}
