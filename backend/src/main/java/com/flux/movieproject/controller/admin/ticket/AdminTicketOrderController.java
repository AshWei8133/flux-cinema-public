package com.flux.movieproject.controller.admin.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.ticket.ManualPaymentDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderDetailDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderSearchRequestDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderSummaryDTO;
import com.flux.movieproject.model.dto.ticket.UpdateTicketOrderResponseDTO;
import com.flux.movieproject.service.ticket.TicketOrderService;
import com.flux.movieproject.utils.OrderNumberUtils;

@RestController
@RequestMapping("/api/admin/ticketOrder")
public class AdminTicketOrderController {
	@Autowired
	private TicketOrderService ticketOrderService;

	/**
	 * 查詢訂單列表 (支援篩選與分頁) 前端會透過 GET 請求，並將篩選條件放在 URL 參數中 例如:
	 * /api/admin/orders?page=1&pageSize=10&status=已付款
	 */
	@GetMapping
	public ResponseEntity<Page<TicketOrderSummaryDTO>> getOrders(TicketOrderSearchRequestDTO searchDTO) {
		Page<TicketOrderSummaryDTO> orderPage = ticketOrderService.findOrdersByCriteria(searchDTO);
		return ResponseEntity.ok(orderPage);
	}

	/**
	 * 查詢單筆訂單詳情
	 * 
	 * @param orderNumber 專業格式的訂單編號 (例如 FX250826-QRTB)
	 * @return
	 */
	@GetMapping("/{orderNumber}")
	public ResponseEntity<TicketOrderDetailDTO> getOrderDetail(@PathVariable String orderNumber) {
		try {
			// 1. 在 Controller 層將前端傳來的 orderNumber 解碼回資料庫 ID
			Integer orderId = OrderNumberUtils.decode(orderNumber);

			// 2. 將解碼後的 ID 傳遞給 Service 進行查詢
			TicketOrderDetailDTO orderDetail = ticketOrderService.findOrderDetailById(orderId);
			return ResponseEntity.ok(orderDetail);
		} catch (IllegalArgumentException e) {
			// 如果訂單編號格式錯誤導致解碼失敗，回傳 400 Bad Request
			return ResponseEntity.badRequest().build();
		} catch (RuntimeException e) {
			// 如果 Service 拋出例外 (找不到訂單)，則回傳 404 Not Found
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * 標示待付款訂單為已付款
	 * 
	 * @param orderNumber 訂單編號
	 * @param dto         付款方式
	 * @return 是否更新成功訊息
	 */
	@PutMapping("/{orderNumber}/mark-as-paid")
	public ResponseEntity<UpdateTicketOrderResponseDTO> marAspaid(@PathVariable String orderNumber,
			@RequestBody ManualPaymentDTO dto) {
		try {
			UpdateTicketOrderResponseDTO response = ticketOrderService.markOrderAsPaid(orderNumber, dto);
			return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
		} catch (RuntimeException e) {
			UpdateTicketOrderResponseDTO errorResponse = new UpdateTicketOrderResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("標記為已付款失敗: 只有'待付款'的訂單才能標記為已付款。");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 針對已付款資料辦理退款
	 * 
	 * @param orderNumber 訂單編號
	 * @return 標記已付款是否成功訊息
	 */
	@PutMapping("/{orderNumber}/refund")
	public ResponseEntity<UpdateTicketOrderResponseDTO> refundOrder(@PathVariable String orderNumber) {
		try {
			UpdateTicketOrderResponseDTO response = ticketOrderService.refundOrder(orderNumber);
			return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
		} catch (RuntimeException e) {
			UpdateTicketOrderResponseDTO errorResponse = new UpdateTicketOrderResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage(e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

}
