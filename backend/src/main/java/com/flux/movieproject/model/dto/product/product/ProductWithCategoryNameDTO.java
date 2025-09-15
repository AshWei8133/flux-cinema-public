package com.flux.movieproject.model.dto.product.product;

public record ProductWithCategoryNameDTO(Integer productId,
                                         String productName,
                                         Integer price,
                                         String imageUrl,
                                         String categoryName,
                                         String description) {
}
