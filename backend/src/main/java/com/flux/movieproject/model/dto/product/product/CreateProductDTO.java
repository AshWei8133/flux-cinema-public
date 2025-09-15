package com.flux.movieproject.model.dto.product.product;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductDTO(@NotBlank(message = "產品名稱不能為空") String productName,
                               Integer categoryId,
                               @Min(0) Integer price,
                               String description,
                               String imageUrl,
                               Integer stock) {
}
