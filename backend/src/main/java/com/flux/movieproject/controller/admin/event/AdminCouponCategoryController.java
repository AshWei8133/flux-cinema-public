package com.flux.movieproject.controller.admin.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.entity.event.CouponCategory;
import com.flux.movieproject.service.event.CouponCategoryService;

@RestController
@RequestMapping("/api/admin/coupon-categories")
public class AdminCouponCategoryController {
	
	@Autowired
    private CouponCategoryService couponCategoryService;

    

    /**
     * 獲取所有優惠券類別
     * @return 包含所有優惠券類別的 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<CouponCategory>> getAllCouponCategories() {
        List<CouponCategory> categories = couponCategoryService.getAllCouponCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 根據 ID 獲取單一優惠券類別
     * @param id 優惠券類別 ID
     * @return 包含單一優惠券類別的 ResponseEntity
     */
    @GetMapping("/{couponCategoryId}")
    public ResponseEntity<CouponCategory> getCouponCategoryById(@PathVariable Integer couponCategoryId) {
        return couponCategoryService.getCouponCategoryById(couponCategoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增一個新的優惠券類別
     * @param category 待新增的優惠券類別物件
     * @return 新增後的優惠券類別物件與 HTTP 201 Created 狀態碼
     */
    @PostMapping
    public ResponseEntity<CouponCategory> createCouponCategory(@RequestBody CouponCategory category) {
        CouponCategory newCategory = couponCategoryService.createCouponCategory(category);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    /**
     * 更新現有的優惠券類別
     * @param id 待更新的優惠券類別 ID
     * @param category 包含更新後資料的物件
     * @return 更新後的優惠券類別物件
     */
    @PutMapping("/{couponCategoryId}")
    public ResponseEntity<CouponCategory> updateCouponCategory(
            @PathVariable Integer couponCategoryId,
            @RequestBody CouponCategory category) {
        try {
            CouponCategory updatedCategory = couponCategoryService.updateCouponCategory(couponCategoryId, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根據 ID 刪除優惠券類別
     * @param id 待刪除的優惠券類別 ID
     * @return HTTP 204 No Content 狀態碼
     */
    @DeleteMapping("/{couponCategoryId}")
    public ResponseEntity<Void> deleteCouponCategory(@PathVariable Integer couponCategoryId) {
        try {
            couponCategoryService.deleteCouponCategory(couponCategoryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}