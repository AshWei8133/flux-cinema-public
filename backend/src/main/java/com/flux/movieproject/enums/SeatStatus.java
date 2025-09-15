package com.flux.movieproject.enums;

public enum SeatStatus {
	AVAILABLE,     // 可用
    RESERVED,      // 已預訂 (臨時鎖定)
    SOLD,          // 已售出
    UNAVAILABLE    // 不可用 (例如維修中)
}
