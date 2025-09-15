package com.flux.movieproject.model.dto.ticket;

import lombok.Data;

@Data
public class UpdateOrderResponse {
	private String orderId; // 回傳給綠界使用的訂單編號
	private Integer finalAmount; // 最終應付總額
	private String itemName; // 商品名稱
}
