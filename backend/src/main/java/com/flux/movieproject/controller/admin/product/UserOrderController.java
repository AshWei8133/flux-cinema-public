package com.flux.movieproject.controller.admin.product;


import com.flux.movieproject.model.dto.member.MemberCouponListDTO;
import com.flux.movieproject.model.dto.member.MemberCouponListDTOForProduct;
import com.flux.movieproject.model.dto.product.order.CreateOrderDTO;
import com.flux.movieproject.model.dto.product.order.CreateOrderResponseDTO;
import com.flux.movieproject.model.entity.member.MemberCoupon;
import com.flux.movieproject.service.product.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public/order")
public class UserOrderController {
    private final ProductOrderService productOrderService;

    @Autowired
    public UserOrderController(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;}

    @PostMapping("/create")
    public ResponseEntity<CreateOrderResponseDTO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        try {
            System.out.println("接收到的訂單資料: " + createOrderDTO);
            CreateOrderResponseDTO response = productOrderService.createOrder(createOrderDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new CreateOrderResponseDTO(null, null,null,"訂單建立失敗：" + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new CreateOrderResponseDTO(null, null,null, "系統錯誤：" + e.getMessage())
            );
        }
    }
    //TODO 前端要展示優惠券
    @GetMapping("/coupon")
    public List<MemberCouponListDTOForProduct> unusedCoupons() {
        try {
            // 確保返回的是有效的 List，即使是空的
            List<MemberCouponListDTOForProduct> coupons = productOrderService.unusedCoupons();

            // 確保不返回 null
            return coupons != null ? coupons : new ArrayList<>();
        } catch (Exception e) {
            // 記錄錯誤
            // 返回空列表而不是拋出異常
            return new ArrayList<>();
        }
    }
}
