package com.flux.movieproject.model.dto.product.product;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryDTO(@NotBlank(message = "類別名稱不能為空")
                                  String categoryName)  {
}
