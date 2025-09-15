package com.flux.movieproject.model.dto.product.product;// ProductResponseDTO.java (修正後)

import com.flux.movieproject.model.entity.product.Product;
import lombok.Data;

@Data
public class ProductResponseDTO {
    private Integer productId;
    private String productName;
    private Integer price;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Integer categoryId;

    public ProductResponseDTO(Product product) {
        this.productId = product.getProductId(); // 確保 productId 被賦值
        this.productName = product.getProductName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.imageUrl = product.getImageUrl();
        this.stock = product.getStock();

        // 只在這裡賦值一次，並確保邏輯清晰
        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getCategoryId();
        } else {
            this.categoryId = null;
        }
    }
}