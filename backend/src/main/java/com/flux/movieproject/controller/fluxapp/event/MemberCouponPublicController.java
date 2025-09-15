package com.flux.movieproject.controller.fluxapp.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.member.MemberCouponListDTO;
import com.flux.movieproject.service.member.MemberCouponService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class MemberCouponPublicController {
	@Autowired
	private MemberCouponService memberCouponService;

	// 1) 市集（領取用）
	@GetMapping("/get")
	@Operation(summary = "可領取優惠券清單（status 僅『已領取/未領取』）")
	public ResponseEntity<Page<com.flux.movieproject.model.dto.event.CouponListItemDTO>> market(
			@AuthenticationPrincipal(expression = "memberId") Integer memberId, // 未登入可為 null
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "12") @Min(1) int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("couponId")));
		return ResponseEntity.ok(memberCouponService.getMarket(memberId, keyword, pageable));
	}

	// 2) 我的優惠券（會員中心）
	@GetMapping("/memberCoupons")
	@Operation(summary = "我的優惠券列表（status: ALL/未使用/已使用）")
	public ResponseEntity<Page<MemberCouponListDTO>> myCoupons(
			@AuthenticationPrincipal(expression = "memberId") Integer memberId,
			@RequestParam(defaultValue = "ALL") String status, @RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("acquisitionTime")));
		return ResponseEntity.ok(memberCouponService.getMyCoupons(memberId, status, keyword, pageable));
	}

	// 3) 領取
	@PostMapping("/memberCoupons/{couponId}")
	@Operation(summary = "領取指定優惠券（建立 member_coupon，status='未使用'）")
	public ResponseEntity<MemberCouponListDTO> claim(@AuthenticationPrincipal(expression = "memberId") Integer memberId,
			@PathVariable Integer couponId) {
		return ResponseEntity.ok(memberCouponService.claim(memberId, couponId));
	}
}
