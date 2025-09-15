package com.flux.movieproject.model.dto.product.cart;

import java.time.Instant;
import java.util.List;

public record CartDTO(Integer cartId,
                      Integer memberId,
                      Instant createdAt,
                      Instant updatedAt,
                      List<CartItemDTO> items) {
}
