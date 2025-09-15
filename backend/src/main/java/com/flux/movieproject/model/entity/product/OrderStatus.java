package com.flux.movieproject.model.entity.product;

    public enum OrderStatus {
        PENDING,       // 待付款
        PREPARED,      //準備中
        PROCESSING,    // 處理中
        COMPLETED,     // 已完成
        CANCELLED,     // 已取消
    }