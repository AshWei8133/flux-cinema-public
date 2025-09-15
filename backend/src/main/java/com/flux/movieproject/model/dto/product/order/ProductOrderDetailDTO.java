package com.flux.movieproject.model.dto.product.order;

public record ProductOrderDetailDTO(Integer detailId,
                                    ProductInfoDTO productInfo,
                                    Integer quantity,
                                    Integer unitPrice,
                                    Integer subtotal,
                                    Integer extraPrice,
                                    String productName) {
}
