package com.flux.movieproject.controller.fluxapp;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.entity.theater.TicketType;
import com.flux.movieproject.service.ticket.TicketService;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {
	@Autowired
	private TicketService ticketService;

	/**
	 * 取得所有票券類型(票種資料)
	 * 
	 * @return
	 */
	@GetMapping("/ticket-types")
	public ResponseEntity<List<TicketType>> getAllTicketTypes() {
		List<TicketType> ticketTypes = ticketService.findAllTicketTypes();
		ticketTypes.removeIf(ticketType -> !ticketType.getIsEnabled());
		return ResponseEntity.ok(ticketTypes);
	}

	@GetMapping("/price-rules/base-prices")
	public ResponseEntity<Map<Integer, Integer>> getBasePrices() {
		Map<Integer, Integer> basePrices = ticketService.findBasePrices();
		return ResponseEntity.ok(basePrices);

	}
}
