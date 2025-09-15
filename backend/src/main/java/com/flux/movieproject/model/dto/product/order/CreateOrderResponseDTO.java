package com.flux.movieproject.model.dto.product.order;

import java.time.Instant;

public record CreateOrderResponseDTO(Integer orderId,
                                     String orderNumber,
                                     Instant orderTime,
                                     String message) {
}
