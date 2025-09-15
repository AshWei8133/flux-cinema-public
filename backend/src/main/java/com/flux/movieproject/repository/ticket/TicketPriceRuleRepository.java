package com.flux.movieproject.repository.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.theater.TicketPriceId;
import com.flux.movieproject.model.entity.theater.TicketPriceRule;

public interface TicketPriceRuleRepository extends JpaRepository<TicketPriceRule, TicketPriceId> {
	/**
	 * 根據票價名稱查詢票價列表
	 * 
	 * @param ticketTypeName 票價名稱
	 * @return 符合票價名稱的票價列表
	 */
	List<TicketPriceRule> findByTicketTypeTicketTypeName(String ticketTypeName);
}
