package com.flux.movieproject.enums;

public enum OrderStatus {
	PENDING,       // 待處理 (臨時預訂)
    PAID,          // 已付款
    CANCELLED,     // 已取消
    COMPLETED,     // 已完成 (場次結束後)
    REFUNDED       // 已退款
}
