package com.flux.movieproject.controller.fluxapp.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.flux.movieproject.model.dto.event.CouponListItemDTO;
import com.flux.movieproject.model.dto.member.MemberCouponListDTO;
import com.flux.movieproject.service.member.MemberCouponService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponListPublicController {

	private final MemberCouponService memberCouponService;

	// ★ 4. 像 TicketOrderController 一樣，注入 MemberJwtUtil
	@Autowired
	private MemberJwtUtil memberJwtUtil;

	/**
	 * 前台優惠券清單（未登入可看；登入顯示是否已領取） ★ 5. 修改此方法以手動解析 Token
	 */
	@GetMapping("/List")
	public ResponseEntity<?> getMarket(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int size, @RequestParam(required = false) String keyword,
			@RequestHeader(value = "Authorization", required = false) String authHeader // 改為非必要
	) {
		Integer memberId = null; // 預設 memberId 為 null (未登入狀態)

		try {
			// 如果請求頭存在且格式正確，才嘗試解析 memberId
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				// 這裡的解析如果失敗 (例如 Token 過期)，會拋出異常，直接進入 catch 區塊
				JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
				memberId = claims.getIntegerClaim("memberId");
			}
		} catch (Exception e) {
			// 對於這個公開的列表 API，Token 無效或過期不是一個致命錯誤
			// 我們只需在控制台打印一個日誌，然後繼續以未登入狀態 (memberId = null) 查詢即可
			System.err.println(
					"GET /api/coupons/List: Invalid Token provided. Proceeding as guest. Error: " + e.getMessage());
		}

		// 無論是否成功解析出 memberId，都繼續執行查詢
		Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "couponId"));
		Page<CouponListItemDTO> marketPage = memberCouponService.getMarket(memberId, keyword, pageable);
		return ResponseEntity.ok(marketPage);
	}

	/** 領取（需登入，未登入由攔截器回 401） */
	@PostMapping("/{couponId}/claim")
	public ResponseEntity<MemberCouponListDTO> claim(@PathVariable Integer couponId,
			@RequestAttribute("memberId") Integer memberId) {
		return ResponseEntity.ok(memberCouponService.claim(memberId, couponId));
	}

	/** 會員中心：我的優惠券 */
	@GetMapping("/me")
	public ResponseEntity<Page<MemberCouponListDTO>> myCoupons(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "ALL") String status,
			@RequestParam(required = false) String keyword,
			@RequestAttribute(value = "memberId", required = false) Integer memberId) {
		if (memberId == null) {
			// 攔截器若沒塞到 request attribute，就直接回 401，避免進 Service 後 500
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
		}
		Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "memberCouponId"));
		return ResponseEntity.ok(memberCouponService.getMyCoupons(memberId, status, keyword, pageable));
	}
}
