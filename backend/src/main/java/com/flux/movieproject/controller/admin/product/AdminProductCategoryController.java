package com.flux.movieproject.controller.admin.product;


import com.flux.movieproject.model.dto.product.product.CreateCategoryDTO;
import com.flux.movieproject.model.entity.product.ProductCategory;
import com.flux.movieproject.service.product.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public AdminProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<ProductCategory> createCategory(@Valid @RequestBody CreateCategoryDTO createCategoryDTO) {
        ProductCategory createdCategory = productCategoryService.addCategory(createCategoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }
}
