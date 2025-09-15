package com.flux.movieproject.controller.admin.ticket;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.ticket.PriceRuleUpdateDTO;
import com.flux.movieproject.model.dto.ticket.TicketTypeResponseDTO;
import com.flux.movieproject.model.dto.ticket.UpdateTicketTypeDTO;
import com.flux.movieproject.model.entity.theater.TicketType;
import com.flux.movieproject.service.ticket.TicketService;


@RestController
@RequestMapping("/api/admin/ticket")
public class AdminTicketController {
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
		return ResponseEntity.ok(ticketTypes);
	}

	/**
	 * 新增票券類型(票種資料)
	 * 
	 * @param ticketTypeDTO
	 * @return
	 */
	@PostMapping("/ticket-types")
	public ResponseEntity<TicketTypeResponseDTO> createTicketType(@RequestBody UpdateTicketTypeDTO ticketTypeDTO) {
		try {
			TicketTypeResponseDTO response = ticketService.createTicketType(ticketTypeDTO);

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (RuntimeException e) {

			// 建立失敗時的 Response 物件
			TicketTypeResponseDTO errorResponse = new TicketTypeResponseDTO();
			errorResponse.setSuccess(false);
			// 設定一個固定的錯誤訊息，不包含 e.getMessage()
			errorResponse.setMessage("新增票種失敗");

			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 更新指定的票種資料
	 * 
	 * @param ticketTypeId 從 URL 路徑中獲取的 ID
	 * @param dto          從請求 Body 中獲取的更新資料
	 * @return
	 */
	@PutMapping("/ticket-types/{id}")
	public ResponseEntity<TicketTypeResponseDTO> updateTicketType(@PathVariable("id") Integer ticketTypeId,
			@RequestBody UpdateTicketTypeDTO dto) {
		try {
			TicketTypeResponseDTO response = ticketService.updateTicketType(ticketTypeId, dto);
			return new ResponseEntity<>(response, HttpStatus.OK); // 更新成功通常回傳 200 OK
		} catch (RuntimeException e) {
			TicketTypeResponseDTO errorResponse = new TicketTypeResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("更新票種失敗: " + e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 刪除指定的票種資料
	 * 
	 * @param ticketTypeId 從 URL 路徑中獲取的 ID
	 * @return
	 */
	@DeleteMapping("/ticket-types/{id}")
	public ResponseEntity<TicketTypeResponseDTO> deleteTicketType(@PathVariable("id") Integer ticketTypeId) {
		try {
			TicketTypeResponseDTO response = ticketService.deleteTicketType(ticketTypeId);
			return new ResponseEntity<>(response, HttpStatus.OK); // 刪除成功也回傳 200 OK
		} catch (RuntimeException e) {
			TicketTypeResponseDTO errorResponse = new TicketTypeResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("刪除票種失敗: " + e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/price-rules/base-prices")
	public ResponseEntity<Map<Integer, Integer>> getBasePrices() {
		Map<Integer, Integer> basePrices = ticketService.findBasePrices();
		return ResponseEntity.ok(basePrices);
	}
	
	/**
	 * 批次儲存票價規則
	 * @param dtoList
	 * @return
	 */
	@PostMapping("/price-rules/batch-update")
	public ResponseEntity<TicketTypeResponseDTO> batchUpdatePriceRules(@RequestBody List<PriceRuleUpdateDTO> dtoList) {
		try {
			TicketTypeResponseDTO response = ticketService.batchUpdatePriceRules(dtoList);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (RuntimeException e) {
			TicketTypeResponseDTO errorResponse = new TicketTypeResponseDTO();
            errorResponse.setSuccess(false);
			errorResponse.setMessage("儲存票價規則失敗: " + e.getMessage()); 
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

}
