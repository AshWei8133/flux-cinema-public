package com.flux.movieproject.model.dto.product.product;

import com.flux.movieproject.model.entity.product.Product;

public record UpdateResponseDTO(
        ProductDTO product,
        String message
) {}
