package com.flux.movieproject.service.product;

import com.flux.movieproject.model.dto.product.product.CreateCategoryDTO;
import com.flux.movieproject.model.entity.product.ProductCategory;
import com.flux.movieproject.repository.product.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    public ProductCategory addCategory(CreateCategoryDTO createCategoryDTO) {
        // 在新增類別前，可以先檢查是否已存在
        if (productCategoryRepository.findByCategoryName(createCategoryDTO.categoryName()).isPresent()) {
            throw new IllegalArgumentException("類別名稱已存在");
        }

        ProductCategory category = new ProductCategory();
        category.setCategoryName(createCategoryDTO.categoryName());

        return productCategoryRepository.save(category);
    }

    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }
}
