package com.flux.movieproject.repository.product;

import com.flux.movieproject.model.entity.product.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    Optional<Category> findByCategoryName(@NotBlank(message = "類別名稱不能為空") String s);

}
