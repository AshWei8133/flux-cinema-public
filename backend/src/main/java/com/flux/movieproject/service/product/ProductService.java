package com.flux.movieproject.service.product;

import com.flux.movieproject.model.dto.product.product.*;
import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.model.entity.product.ProductCategory;
import com.flux.movieproject.repository.product.ProductCategoryRepository;
import com.flux.movieproject.repository.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true) // 確保在交易中執行
    public List<ProductWithCategoryNameDTO> getProductsByCategoryId(Integer categoryId) {
        // 查詢會回傳包含代理物件的 Product 列表
        List<Product> products = productRepository.findByCategoryCategoryId(categoryId);

        // 在交易內，將 Product 轉換為 DTO
        return products.stream()
                .map(p -> new ProductWithCategoryNameDTO(
                        p.getProductId(),
                        p.getProductName(),
                        p.getPrice(),
                        p.getImageUrl(),
                        p.getDescription(),
                        p.getCategory().getCategoryName() // 這裡會觸發懶加載並獲取名稱
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // 確保在交易中執行
    public List<ProductWithCategoryNameDTO> getProductsByCategoryName(String categoryName) {
        // 查詢會回傳包含代理物件的 Product 列表
        List<Product> products = productRepository.findByCategoryCategoryName(categoryName);

        // 在交易內，將 Product 轉換為 DTO
        return products.stream()
                .map(p -> new ProductWithCategoryNameDTO(
                        p.getProductId(),
                        p.getProductName(),
                        p.getPrice(),
                        p.getImageUrl(),
                        p.getDescription(),
                        p.getCategory().getCategoryName() // 這裡會觸發懶加載並獲取名稱
                ))
                .collect(Collectors.toList());
    }


    public List<ProductDTO> findByPriceBetween(Integer minPrice, Integer maxPrice) {
        List<Product> products =  productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(product -> new ProductDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getDescription(),
                        product.getStock(),
                        product.getCategory().getCategoryId()
                ))
                .collect(Collectors.toList());
    }




    @Transactional
    public Product addProduct(CreateProductDTO productDto){

        productRepository.findByProductNameAndPrice(productDto.productName(), productDto.price())
                .ifPresent(p -> {
                    throw new IllegalStateException ("產品 '" + p.getProductName() + "' (價格: " + p.getPrice() + ") 已經存在！");
                });

        Product newProduct = new Product();
        newProduct.setProductName(productDto.productName());

        ProductCategory category = productCategoryRepository.findById(productDto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("類別未找到，無法新增產品"));

        String url = productDto.imageUrl();
        newProduct.setCategory(category);
        newProduct.setPrice(productDto.price());
        newProduct.setDescription(productDto.description());
        newProduct.setImageUrl((url != null && !url.isEmpty()) ? url : "http://res.cloudinary.com/dg98ebu8y/image/upload/v1755571478/smgkomzsedilrlhjvjgq.jpg");
        newProduct.setStock(productDto.stock());
        newProduct.setIsAvailable(true);
        newProduct.setCreationTime(Instant.now());

        return productRepository.save(newProduct);


    }

    public List<ProductDTO> getUnavailableProducts() {
        List<Product> products =  productRepository.findByIsAvailableFalse();
        return products.stream()
                .map(product -> new ProductDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getPrice(),
                        product.getDescription(),
                        product.getImageUrl(),
                        product.getStock(),
                        product.getCategory().getCategoryId()
                ))
                .collect(Collectors.toList());
    }


    //完全從資料庫刪除
    @Transactional
    public void deleteProductById(Integer productId){
        if(!productRepository.existsById(productId)){
            throw new RuntimeException("此ID沒有產品，產品ID = " + productId);
        }
        productRepository.deleteById(productId);
    }


    public ProductDTO getProductById(Integer productId) {
        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("產品未找到"));

        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getStock(),
                product.getCategory().getCategoryId() // 從關聯實體中獲取類別 ID
        );
    }

    @Transactional
    public UpdateResponseDTO updateProductById(Integer productId, UpdateProductDTO updateProductDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("產品未找到，無法更新"));

        String message = "產品已成功更新。";

        // 判斷是否修改了上下架狀態，並產生對應訊息
        if (updateProductDTO.isAvailable() != null && updateProductDTO.isAvailable() != existingProduct.getIsAvailable()) {
            if (updateProductDTO.isAvailable()) {
                message = "產品：" + existingProduct.getProductName() + " 已重新上架。";
            } else {
                message = "產品：" + existingProduct.getProductName() + " 已成功下架。";
            }
        }

        if (updateProductDTO.productName() != null) {
            if (updateProductDTO.productName().isBlank()) {
                throw new IllegalArgumentException("產品名稱不能為空。");
            }
            existingProduct.setProductName(updateProductDTO.productName());
        }

        if (updateProductDTO.categoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(updateProductDTO.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("類別未找到"));
            existingProduct.setCategory(category);
        }

        if (updateProductDTO.price() != null) {
            if (updateProductDTO.price() < 0) {
                throw new IllegalArgumentException("價格不能為負數。");
            }
            existingProduct.setPrice(updateProductDTO.price());
        }

        if (updateProductDTO.description() != null) {
            existingProduct.setDescription(updateProductDTO.description());
        }

        if (updateProductDTO.imageUrl() != null) {
            existingProduct.setImageUrl(updateProductDTO.imageUrl());
        }

        if (updateProductDTO.stockQuantity() != null) {
            existingProduct.setStock(updateProductDTO.stockQuantity());
        }

        if (updateProductDTO.isAvailable() != null) {
            existingProduct.setIsAvailable(updateProductDTO.isAvailable());
        }

        // 儲存更新後的產品實體
        Product updatedProduct = productRepository.save(existingProduct);


        ProductDTO productDto = new ProductDTO(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getPrice(),
                updatedProduct.getImageUrl(),
                updatedProduct.getDescription(),
                updatedProduct.getStock(),
                updatedProduct.getCategory().getCategoryId()
        );

        // 回傳包含 DTO 和訊息的 UpdateResponseDTO
        return new UpdateResponseDTO(productDto, message);
    }


    public List<ProductDTO> needReStock() {

        List<Product> products = productRepository.findProductsNeedingRestock();

        // 將Product 實體列表轉換為 ProductDTO 列表
        return products.stream()
                .map(p -> new ProductDTO(
                        p.getProductId(),
                        p.getProductName(),
                        p.getPrice(),
                        p.getDescription(),
                        p.getImageUrl(),
                        p.getStock(),
                        p.getCategory().getCategoryId() // 這裡會自動從關聯實體獲取ID
                ))
                .collect(Collectors.toList());
    }




    /**
     * 一個通用的分頁與排序方法
     * @param page 頁碼 (從 0 開始)
     * @param size 每頁筆數
     * @param sortBy 排序欄位 (例如 "price", "name")，可為 null
     * @param direction 排序方向 ("asc" 或 "desc")，可為 null
     * @param keyword 搜尋的關鍵字
     */
    public Page<Product> getProducts(
            int page,
            int size,
            @Nullable String sortBy,
            @Nullable String direction,
            @Nullable String keyword,
            @Nullable Integer minPrice,
            @Nullable Integer maxPrice
    ) {
        // 建立 Pageable 物件，處理分頁和排序
        Pageable pageable = createPageable(page, size, sortBy, direction);

        // 建立 Specification 物件，處理篩選邏輯
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            //只看上架產品
            predicates.add(criteriaBuilder.isTrue(root.get("isAvailable")));
            // 關鍵字篩選條件
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("productName")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            // 最低價格篩選條件
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // 最高價格篩選條件
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 如果沒有任何篩選條件，回傳一個永遠為 true 的條件
            if (predicates.isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            // 將所有條件用 AND 連結起來
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 執行最終查詢
        return productRepository.findAll(specification, pageable);
    }

    // 給使用者看的方法：只回傳已上架且有庫存的產品
    public Page<Product> getVisibleProducts(
            int page,
            int size,
            @Nullable String sortBy,
            @Nullable String direction,
            @Nullable String keyword,
            @Nullable Integer minPrice, // 新增：最低價格
            @Nullable Integer maxPrice,
            @Nullable Integer categoryId

    ) {
        // 建立 Pageable 物件，處理分頁和排序
        Pageable pageable = createPageable(page, size, sortBy, direction);

        // 使用 Specification 構建動態查詢條件
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            // 建立條件列表，用於存放所有篩選條件
            List<Predicate> predicates = new ArrayList<>();

            // 1. 預設條件：isAvailable 為 true 且 stock 大於 0
            predicates.add(criteriaBuilder.isTrue(root.get("isAvailable")));
            predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));

            // 2. 關鍵字篩選條件
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
            }

            // 3. 價格範圍篩選條件
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (categoryId != null) {
                // 如果是 @ManyToOne 關聯，使用 category.categoryId
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("categoryId"),
                        categoryId
                ));

                // 或者如果資料庫欄位名稱是 category_id，可以使用 Join
                // Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                // predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), categoryId));
            }

            // 將所有條件用 AND 連結起來
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 執行最終查詢
        return productRepository.findAll(specification, pageable);
    }



    private Pageable createPageable(int page, int size, @Nullable String sortBy, @Nullable String direction) {
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(sortDirection, sortBy);
            return PageRequest.of(page, size, sort);
        } else {
            return PageRequest.of(page, size);
        }
    }



}
