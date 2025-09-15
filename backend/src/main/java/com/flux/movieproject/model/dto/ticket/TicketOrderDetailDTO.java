package com.flux.movieproject.model.dto.ticket;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TicketOrderDetailDTO {
	private String orderNumber;
	private MemberInfoForTicketDTO member;
	private Integer totalAmount;
	private Integer totalTicketAmount;
	private Integer totalDiscount;
	private CouponInfoForTicketDTO coupon;
	private String status;
	private String paymentType;
	private LocalDateTime createdTime;
	private LocalDateTime paymentTime;
	private List<TicketItemDTO> ticketOrderDetails;
}
