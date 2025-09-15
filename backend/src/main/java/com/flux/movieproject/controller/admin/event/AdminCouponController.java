package com.flux.movieproject.controller.admin.event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.event.coupon.CouponDTO;
import com.flux.movieproject.model.dto.event.coupon.CouponMapper;
import com.flux.movieproject.model.dto.event.coupon.CouponResponse;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.service.event.CouponService;

import jakarta.validation.Valid;

/**
 * 後台優惠券管理控制器，處理優惠券的 CRUD 相關 API 請求。
 */
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {


    @Autowired
    private CouponService couponService;



    /**
     * 取得所有優惠券的列表，並支援分頁。
     *
     * @param page 查詢的頁碼，預設為 0。
     * @param size 每頁顯示的數量，預設為 10。
     * @return 包含優惠券列表的分頁物件，並回傳 HTTP 200 OK。
     */
    @GetMapping
    public ResponseEntity<Page<CouponDTO>> getAllCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CouponDTO> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 根據ID取得單一優惠券的詳細資訊。
     *
     * @param couponId 欲查詢優惠券的唯一識別碼。
     * @return 匹配指定 ID 的優惠券物件，並回傳 HTTP 200 OK。
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Integer couponId) { 
        CouponDTO coupon = couponService.getCouponById(couponId); 
        return ResponseEntity.ok(coupon);
    }

    /**
     * 新增一張優惠券。
     *
     * @param coupon 要新增的優惠券物件。
     * @return 成功新增並包含生成序號的優惠券物件，回傳 HTTP 201 Created。
     */
    @PostMapping
    
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
    	System.out.println(couponDTO);
        Coupon createdCoupon = couponService.createCoupon(couponDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);
    }

    /**
     * 更新指定ID的優惠券資訊。
     *
     * @param couponId 欲更新優惠券的唯一識別碼。
     * @param couponDetails 包含新資訊的優惠券物件。
     * @return 更新後完整的優惠券物件，並回傳 HTTP 200 OK。
     */
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> update(@PathVariable Integer couponId, @RequestBody Coupon coupon) {
        Coupon updated = couponService.updateCoupon(couponId, coupon);
        return ResponseEntity.ok(CouponMapper.toResponse(updated));
    }
    
    



    
    

    /**
     * 根據ID刪除指定的優惠券。
     *
     * @param couponId 欲刪除優惠券的唯一識別碼。
     * @return 回傳無內容的 HTTP 204 No Content，表示刪除成功。
     */
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Integer couponId) { 
    	couponService.deleteCoupon(couponId); 
        return ResponseEntity.noContent().build();
    }
    


}