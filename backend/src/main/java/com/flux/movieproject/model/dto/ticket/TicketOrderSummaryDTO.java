package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 回傳給前端訂單管理的主列表資料格式，包含必要的摘要資訊
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderSummaryDTO {
	private String orderNumber;
	private MemberInfoForTicketDTO member; // 直接使用引入的 DTO
	private LocalDateTime createdTime;
	private Integer totalAmount;
	private String status;
	private String paymentType;
}
