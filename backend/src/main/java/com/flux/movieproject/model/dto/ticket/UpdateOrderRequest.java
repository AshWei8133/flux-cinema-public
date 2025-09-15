package com.flux.movieproject.model.dto.ticket;

import lombok.Data;

@Data
public class UpdateOrderRequest {
	private String encodedOrderId; // 接收加密過的訂單編號字串
	private Integer couponId; // 使用的優惠券 ID，可能為 null
}
