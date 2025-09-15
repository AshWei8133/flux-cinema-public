package com.flux.movieproject.controller.fluxapp.event.ticketorder;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.entity.event.TicketOrderApplicableCouponDTO;
import com.flux.movieproject.service.ticket.TicketOrderService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

@RestController
@RequestMapping("/api/coupons/ticketorder")
public class CouponForTicketOrderController {
	@Autowired
	private TicketOrderService ticketOrderCouponService;

	@Autowired
	private MemberJwtUtil memberJwtUtil;

	/**
	 * 根據訂單的上下文資訊（場次、小計金額），查詢當前會員可用的優惠券。
	 *
	 * @param sessionId  當前訂單的電影場次 ID
	 * @param subtotal   當前訂單的小計金額
	 * @param authHeader HTTP 請求標頭中的 Authorization，用於驗證會員身份
	 * @return 一個包含適用優惠券列表的 ResponseEntity
	 */
	@GetMapping("/applicable")
	public ResponseEntity<?> getApplicableCoupons(@RequestParam Integer sessionId, @RequestParam BigDecimal subtotal,
			@RequestHeader("Authorization") String authHeader) {

		try {
			// 1. 驗證標頭格式並提取 Token 字串 (與 TicketOrderController 完全相同的邏輯)
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "無效的認證標頭"));
			}
			String token = authHeader.substring(7);

			// 2. 使用 JwtUtil 解析 Token 並獲取 Claims
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);

			// 3. 從 Claims 中安全地獲取 memberId
			Integer memberId = claims.getIntegerClaim("memberId");
			if (memberId == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Token 中缺少 memberId 資訊"));
			}

			// 4. 將解析出的、可信的 memberId 與請求參數一起傳遞給 Service 執行業務邏輯
			List<TicketOrderApplicableCouponDTO> coupons = ticketOrderCouponService.findApplicableCoupons(memberId, sessionId,
					subtotal);
			
			// 5. 返回成功的回應，直接回傳優惠券列表
			return ResponseEntity.ok(coupons);

		} catch (ParseException | JOSEException e) {
			// 如果 Token 格式錯誤、或簽名驗證失敗
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token 無效或已過期: " + e.getMessage()));
		} catch (RuntimeException e) {
			// 處理 Service 層可能拋出的業務邏輯例外 (如找不到場次)
			// 使用 HttpStatus.BAD_REQUEST (400) 表示客戶端傳送的參數有問題（例如 sessionId 不存在）
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}
}
