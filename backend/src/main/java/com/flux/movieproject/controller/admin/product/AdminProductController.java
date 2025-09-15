package com.flux.movieproject.controller.admin.product;
import java.util.List;

import com.flux.movieproject.model.dto.product.product.*;
import com.flux.movieproject.model.entity.product.ProductCategory;
import com.flux.movieproject.repository.product.ProductCategoryRepository;
import com.flux.movieproject.service.product.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.service.product.CloudinaryService;
import com.flux.movieproject.service.product.ProductService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/admin/products")
public class    AdminProductController {


    private ProductService productService;
    private CloudinaryService cloudinaryService;
    private ProductCategoryService productCategoryService;


    @Autowired
    public AdminProductController(ProductService productService,CloudinaryService cloudinaryService,ProductCategoryService productCategoryService) {

        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
        this.productCategoryService = productCategoryService;
    }


    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {


        Product createdProduct = productService.addProduct(createProductDTO);

        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);

    }


    @GetMapping("/restock")
    public ResponseEntity<List<ProductDTO>> getProductsNeedingRestock() {

        List<ProductDTO> products = productService.needReStock();

        return ResponseEntity.ok(products);

    }


    @PatchMapping("/{id}")

    public ResponseEntity<UpdateResponseDTO> updateProduct(@PathVariable Integer id,
                                                           @Valid @RequestBody UpdateProductDTO updateProductDTO) {

        UpdateResponseDTO updatedProduct = productService.updateProductById(id, updateProductDTO);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);

    }


    @DeleteMapping("/{productId}")

    public ResponseEntity<String> deleteProduct(@PathVariable Integer productId) {

        productService.deleteProductById(productId);

        return ResponseEntity.ok("產品 ID " + productId + " 已成功刪除");

    }


    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam @Nullable String sortBy, @RequestParam @Nullable String direction,
            @RequestParam @Nullable String keyword,@RequestParam(defaultValue = "0")Integer minPrice, // 新增：最低價格
            @RequestParam(defaultValue = "100000")Integer maxPrice

    ) {

    // Controller 接收前端傳來的參數，並直接傳遞給 Service

        Page<Product> products = productService.getProducts(page, size, sortBy, direction, keyword, minPrice, maxPrice);
        Page<ProductResponseDTO> productResponsePage = products.map(ProductResponseDTO::new);
        return new ResponseEntity<>(productResponsePage, HttpStatus.OK);

    }


    @GetMapping("/category")
    public ResponseEntity<List<ProductWithCategoryNameDTO>> getProductsByCategoryName(@RequestParam String categoryName) {
        List<ProductWithCategoryNameDTO> products = productService.getProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/categories")
    public List<ProductCategory> getAllCategories() {
        return productCategoryService.findAll();
    }

    @GetMapping("/price")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceBetween(

            @RequestParam(defaultValue = "0") Integer minPrice,

            @RequestParam(defaultValue = "100000") Integer maxPrice) {

        List<ProductDTO> products = productService.findByPriceBetween(minPrice, maxPrice);

        return new ResponseEntity<>(products, HttpStatus.OK);

    }


    //回傳單一產品
    @GetMapping ("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }
    //回傳下架商品的清單

    @GetMapping("/unavailable")
    public ResponseEntity<List<ProductDTO>> getUnavailableProducts() {
        List<ProductDTO> products = productService.getUnavailableProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

}

