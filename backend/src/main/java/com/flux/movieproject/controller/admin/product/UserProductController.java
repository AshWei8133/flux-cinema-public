package com.flux.movieproject.controller.admin.product;


import com.flux.movieproject.model.dto.product.product.ProductDTO;
import com.flux.movieproject.model.dto.product.product.ProductResponseDTO;
import com.flux.movieproject.model.dto.product.product.ProductWithCategoryNameDTO;
import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.model.entity.product.ProductCategory;
import com.flux.movieproject.service.product.ProductCategoryService;
import com.flux.movieproject.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/product")
public class UserProductController {

    private ProductService productService;
    private ProductCategoryService productCategoryService;

    @Autowired
    public UserProductController(ProductService productService , ProductCategoryService productCategoryService) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;
    }


    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam @Nullable String sortBy, @RequestParam @Nullable String direction,
            @RequestParam @Nullable String keyword, @RequestParam(defaultValue = "0")Integer minPrice, // 新增：最低價格
            @RequestParam(defaultValue = "1000")Integer maxPrice, @RequestParam @Nullable Integer categoryId

    ) {

        // Controller 接收前端傳來的參數，並直接傳遞給 Service
        Page<Product> products = productService.getVisibleProducts(page, size, sortBy, direction, keyword, minPrice, maxPrice, categoryId);
        Page<ProductResponseDTO> productResponsePage = products.map(ProductResponseDTO::new);
        return new ResponseEntity<>(productResponsePage, HttpStatus.OK);

    }

    @GetMapping("/category")
    public ResponseEntity<List<ProductWithCategoryNameDTO>> getProductsByCategoryId(@RequestParam Integer categoryId) {
        List<ProductWithCategoryNameDTO> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceBetween(
            @RequestParam(defaultValue = "0") Integer minPrice,
            @RequestParam(defaultValue = "100000") Integer maxPrice) {

        List<ProductDTO> products = productService.findByPriceBetween(minPrice, maxPrice);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping ("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public List<ProductCategory> getAllCategories() {
        return productCategoryService.findAll();
    }


}


//String path = request.getRequestURI();
//		if (path.startsWith("/api/public/product/category")) {
//			return true;
//		放行路徑
