package com.flux.movieproject.model.dto.product.order;

import com.flux.movieproject.model.entity.product.PaymentMethod;

import java.util.List;

public record CreateOrderDTO(Integer memberId, List<CreateProductOrderDetailDTO> orderDetails
        , Integer couponId, PaymentMethod paymentMethod,String email) {

    @Override
    public String toString() {
        return "couponId = " + couponId;
    }
}
