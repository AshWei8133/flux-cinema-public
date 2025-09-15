package com.flux.movieproject.repository.ticket;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.enums.OrderStatus;
import com.flux.movieproject.model.dto.ticket.TicketOrderHistoryDTO;
import com.flux.movieproject.model.entity.theater.TicketOrder;

public interface TicketOrderRepository
		extends JpaRepository<TicketOrder, Integer>, JpaSpecificationExecutor<TicketOrder> {
	/**
	 * 【新增】根據訂單 ID，查詢其所有訂單明細關聯的 SessionSeat ID。 這樣可以避免在 Service 層中進行不必要的物件載入。
	 * 
	 * @param orderId 訂單 ID
	 * @return SessionSeat ID 的列表
	 */
	@Query("SELECT tod.sessionSeat.sessionSeatId FROM TicketOrderDetail tod WHERE tod.ticketOrder.ticketOrderId = :orderId")
	List<Integer> findSessionSeatIdsByOrderId(@Param("orderId") Integer orderId);

	/**
	 * 查找所有狀態為指定狀態的訂單
	 */
	List<TicketOrder> findByStatus(OrderStatus status);

	/**
	 * @新增：透過自定義 JPQL 查詢，查找所有狀態為 PENDING 且其關聯座位已過期的訂單。 這裡我們 JOIN TicketOrderDetail 和
	 *           SessionSeat 表，來獲取座位上的 reservedExpiredDate。 DISTINCT
	 *           關鍵字是必要的，因為一個訂單有多個座位，不加會重複。
	 */
	@Query("SELECT DISTINCT o FROM TicketOrder o " + "JOIN o.ticketOrderDetails tod " + "JOIN tod.sessionSeat ss "
			+ "WHERE o.status = :status AND ss.reservedExpiredDate < :expiryTime")
	List<TicketOrder> findExpiredPendingOrders(@Param("status") OrderStatus status,
			@Param("expiryTime") LocalDateTime expiryTime);

	/**
	 * 根據會員ID，查找所有可用於計算消費總額的電影票訂單。
	 * 包含：所有 'COMPLETED' 的訂單，以及狀態為 'PAID' 且場次時間已是過去的訂單。
	 * 
	 * @param memberId 會員ID
	 * @param now      當前時間，用於比較場次時間
	 * @return
	 */
	@Query("SELECT DISTINCT o FROM TicketOrder o " +
		   "JOIN o.ticketOrderDetails tod " +
		   "JOIN tod.sessionSeat ss " +
		   "JOIN ss.movieSession ms " +
		   "WHERE o.member.memberId = :memberId AND " +
		   "(o.status = com.flux.movieproject.enums.OrderStatus.COMPLETED OR " +
		   "(o.status = com.flux.movieproject.enums.OrderStatus.PAID AND ms.startTime < :now))")
	List<TicketOrder> findEligibleForSpending(@Param("memberId") Integer memberId, @Param("now") LocalDateTime now);


	@Query("SELECT to.ticketOrderId as ticketOrderId, " + "to.createdTime as createdTime, " + "to.status as status, "
			+ "to.totalAmount as totalAmount, " + "m.titleLocal as movieTitle, " + "ms.startTime as startTime, "
			+ "t.theaterName as theaterName, " + "STRING_AGG(CONCAT(s.rowNumber, s.columnNumber), ', ') as seatNumber "
			+ "FROM TicketOrder to " + "JOIN to.ticketOrderDetails tod " + "JOIN tod.sessionSeat ss "
			+ "JOIN ss.seat s " + "JOIN ss.movieSession ms " + "JOIN ms.movie m " + "JOIN ms.theater t "
			+ "WHERE to.member.memberId = :memberId "
			+ "GROUP BY to.ticketOrderId, to.createdTime, to.status, to.totalAmount, m.titleLocal, ms.startTime, t.theaterName "
			+ "ORDER BY to.createdTime DESC")
	List<TicketOrderHistoryDTO> findTicketOrderHistoryByMemberId(@Param("memberId") Integer memberId);
}