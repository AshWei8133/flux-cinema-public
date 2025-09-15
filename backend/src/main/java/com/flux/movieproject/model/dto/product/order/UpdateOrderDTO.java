package com.flux.movieproject.model.dto.product.order;

import com.flux.movieproject.model.entity.product.ProductOrderDetail;

public record UpdateOrderDTO(String shippingAddress, ProductOrderDetail quantity) {
    //TODO 處理DTO參數設定
}
