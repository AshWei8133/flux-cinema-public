package com.flux.movieproject.model.dto.product.product;

public record ProductDTO(
        Integer productId,
        String productName,
        Integer price,

        String description,
        String imageUrl,
        Integer stock,
        Integer categoryId // 直接回傳類別ID
) {}
