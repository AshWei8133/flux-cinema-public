package com.flux.movieproject.repository.product;

import java.util.List;
import java.util.Optional;

import com.flux.movieproject.model.entity.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.product.Product;

public interface ProductRepository extends JpaRepository<Product,Integer>, JpaSpecificationExecutor<Product> {


    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);//根據價格區間查詢

    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE c.categoryName = :categoryName")
    List<Product> findByCategoryCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE c.categoryId = :categoryId")
    List<Product> findByCategoryCategoryId(@Param("categoryId") Integer categoryId);

    List<Product> findByIsAvailableFalse();

    Optional<Product> findByProductNameAndPrice(String productName, Integer price);





    @Query("SELECT p FROM Product p WHERE p.isAvailable = true AND " +
            "(p.category.categoryName = '餐點' AND p.stock <= 50) OR " +
            "(p.category.categoryName = '飲料' AND p.stock <= 25)")
    List<Product> findProductsNeedingRestock();



}


