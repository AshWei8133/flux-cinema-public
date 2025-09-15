package com.flux.movieproject.model.dto.product.product;

public record UpdateProductDTO(Integer productId,
                               String productName,
                               Integer categoryId,
                               Integer price,
                               String description,
                               String imageUrl,
                               Integer stockQuantity,
                               Boolean isAvailable) {


}
