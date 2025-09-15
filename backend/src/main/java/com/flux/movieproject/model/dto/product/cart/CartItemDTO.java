package com.flux.movieproject.model.dto.product.cart;

public record CartItemDTO(
        Integer cartItemId,
        Integer productId,
        String productName,
        Integer quantity,
        Integer productPrice,
        String imageUrl,
        boolean isAvailable,
        Integer stock
) {}