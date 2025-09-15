package com.flux.movieproject.service.event;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.entity.event.CouponCategory;
import com.flux.movieproject.repository.event.CouponCategoryRepository;

/**
 * 優惠券類別的核心服務，負責優惠券類別的 CRUD 操作。
 */
@Service
@Transactional(readOnly = true) // 將讀取操作設為唯讀，提升效能
public class CouponCategoryService {

    private final CouponCategoryRepository couponCategoryRepository;

    public CouponCategoryService(CouponCategoryRepository couponCategoryRepository) {
        this.couponCategoryRepository = couponCategoryRepository;
    }

    /**
     * 獲取所有優惠券類別
     * @return 所有優惠券類別的列表
     */
    public List<CouponCategory> getAllCouponCategories() {
        return couponCategoryRepository.findAll();
    }

    /**
     * 根據ID獲取單一優惠券類別
     * @param id 優惠券類別ID
     * @return 包含優惠券類別物件的 Optional
     */
    public Optional<CouponCategory> getCouponCategoryById(Integer id) {
        return couponCategoryRepository.findById(id);
    }

    /**
     * 新增一個新的優惠券類別
     * @param category 待新增的優惠券類別物件
     * @return 新增後的優惠券類別物件
     */
    @Transactional // 標註寫入操作
    public CouponCategory createCouponCategory(CouponCategory category) {
        // 可以在這裡加入額外驗證，例如檢查名稱是否已存在
        return couponCategoryRepository.save(category);
    }

    /**
     * 更新現有的優惠券類別
     * @param id              待更新的優惠券類別ID
     * @param updatedCategory 包含更新後資料的物件
     * @return 更新後的優惠券類別物件
     */
    @Transactional // 標註寫入操作
    public CouponCategory updateCouponCategory(Integer id, CouponCategory updatedCategory) {
        return couponCategoryRepository.findById(id).map(category -> {
            category.setCouponCategoryName(updatedCategory.getCouponCategoryName());
            category.setDescription(updatedCategory.getDescription());
            return couponCategoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("優惠券類別未找到，ID: " + id));
    }

    /**
     * 根據ID刪除優惠券類別
     * @param id 待刪除的優惠券類別ID
     */
    @Transactional // 標註寫入操作
    public void deleteCouponCategory(Integer id) {
        if (!couponCategoryRepository.existsById(id)) {
            throw new RuntimeException("優惠券類別未找到，ID: " + id);
        }
        couponCategoryRepository.deleteById(id);
    }
}