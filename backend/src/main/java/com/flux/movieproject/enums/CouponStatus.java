package com.flux.movieproject.enums;

public enum CouponStatus {
    UNUSED("未使用"),

    USED("已使用"),

    EXPIRED("已過期"),

    CANCELLED("已取消"),
    
    UNRECEIVED("未領取"),
    
	RECEIVED("已領取");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}