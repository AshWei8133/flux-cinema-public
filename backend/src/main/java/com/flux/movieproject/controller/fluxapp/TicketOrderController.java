package com.flux.movieproject.controller.fluxapp;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.ticket.ReservationRequest;
import com.flux.movieproject.model.dto.ticket.ReservationResponse;
import com.flux.movieproject.model.dto.ticket.UpdateOrderRequest;
import com.flux.movieproject.model.dto.ticket.UpdateOrderResponse;
import com.flux.movieproject.service.ticket.EcpayService;
import com.flux.movieproject.service.ticket.TicketOrderService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.flux.movieproject.utils.OrderNumberUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

@RestController
@RequestMapping("/api/ticketOrder")
public class TicketOrderController {
	@Autowired
	private EcpayService ecpayService;

	@Autowired
	private TicketOrderService ticketOrderService;
	@Autowired
	private MemberJwtUtil memberJwtUtil;

	@Value("${ecpay.apiUrl}")
	private String ecpayApiUrl;

	/**
	 * 模擬創建一筆訂單，並取得前往綠界付款需要的參數
	 * 
	 * @return 一個包含 API URL 和所有表單參數的物件
	 */
	@PostMapping("/checkout")
	public ResponseEntity<?> createPayment() {
		// 在實際應用中，您應該從資料庫生成或取得訂單資訊
		String orderId = "FLUX" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
		Integer amount = 500;
		String itemName = "復仇者聯盟：終局之戰 電影票 x2";

		// 呼叫 Service 產生綠界需要的參數
		Map<String, String> ecpayParams = ecpayService.createCreditCardOrder(orderId, amount, itemName);

		// 將 ECPay 的 API URL 和參數一起回傳給前端
		// 這樣前端就可以根據這些資料動態建立 form
		Map<String, Object> response = Map.of("apiUrl", ecpayApiUrl, "params", ecpayParams);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/reserve")
	public ResponseEntity<?> reserveSeats(@RequestBody ReservationRequest request,
			@RequestHeader("Authorization") String authHeader) {

		try {
			// 1. 驗證標頭格式並提取 Token 字串
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("無效的認證標頭");
			}
			String token = authHeader.substring(7);

			// 2. 使用 JwtUtil 解析 Token 並獲取 Claims
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);

			// 3. 從 Claims 中安全地獲取 memberId
			Integer memberId = claims.getIntegerClaim("memberId");
			if (memberId == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token 中缺少 memberId 資訊");
			}

			// 4. 將解析出的、可信的 memberId 傳遞給 Service 執行業務邏輯
			ReservationResponse serviceResponse = ticketOrderService.createReservation(request, memberId);

			// 5. 將 Service 回傳的數字 ID 加密為字串
			String orderNumber = OrderNumberUtils.encode(serviceResponse.getTicketOrderId());
			
			System.out.println("訂單編號: " + orderNumber + " 生成");

			// 6. 【修改】建立新的響應物件，回傳加密後的訂單編號和到期時間
			Map<String, Object> response = Map.of("orderNumber", orderNumber, "reservedExpiredDate",
					serviceResponse.getReservedExpiredDate());

			return ResponseEntity.ok(response);

		} catch (ParseException | JOSEException e) {
			// 如果 Token 格式錯誤、或簽名驗證失敗
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token 無效或已過期: " + e.getMessage());
		} catch (RuntimeException e) {
			// 處理 Service 層拋出的業務邏輯例外 (如座位已被預訂、票數不符等)
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@DeleteMapping("/cancel/{orderNumber}")
	public ResponseEntity<?> cancelReservation(@PathVariable String orderNumber,
			@RequestHeader("Authorization") String authHeader) {

		try {
			// 1. 從 Token 中安全地獲取 memberId
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "無效的認證標頭"));
			}
			String token = authHeader.substring(7);
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			Integer memberId = claims.getIntegerClaim("memberId");
			if (memberId == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Token 中缺少 memberId 資訊"));
			}

			// 2. 將加密字串解碼為數字 ID，再傳給 Service
			Integer orderId = OrderNumberUtils.decode(orderNumber);

			// 3. 呼叫 Service 執行取消邏輯
			ticketOrderService.cancelTemporaryOrder(orderId, memberId);

			// 4. 返回成功訊息
			return ResponseEntity.ok(Map.of("message", "訂單 " + orderNumber + " 已成功取消。"));

		} catch (ParseException | JOSEException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token 無效或已過期: " + e.getMessage()));
		} catch (IllegalStateException e) {
			// 處理權限不足等業務邏輯錯誤
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (RuntimeException e) {
			// 處理訂單不存在等其他錯誤
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		}
	}
	
	
	
	@PostMapping("/update-and-checkout")
    public ResponseEntity<?> updateOrderForCheckout(@RequestBody UpdateOrderRequest request,
                                                    @RequestHeader("Authorization") String authHeader) {
        try {
            // 1. 從 Token 獲取 memberId
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("無效的認證標頭");
            }
            String token = authHeader.substring(7);
            JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
            Integer memberId = claims.getIntegerClaim("memberId");

            if (memberId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token 中缺少 memberId 資訊");
            }

            // 2. 【核心】將前端傳來的加密訂單編號解密為後端資料庫使用的整數 ID
            Integer orderId = OrderNumberUtils.decode(request.getEncodedOrderId());

            // 3. 呼叫 Service 更新訂單狀態並計算最終金額
            // 這裡傳入的是解碼後的整數 ID
            UpdateOrderResponse response = ticketOrderService.updateOrderAndPrepareForPayment(orderId, request.getCouponId(), memberId);

            // 4. 呼叫 EcpayService 產生綠界需要的參數
            // 這裡傳入的是 Service 回傳的訂單編號字串，綠界用這個字串來追蹤訂單
            Map<String, String> ecpayParams = ecpayService.createCreditCardOrder(
                response.getOrderId(),
                response.getFinalAmount(),
                response.getItemName()
            );

            // 5. 將 ECPay 的 API URL 和參數一起回傳給前端
            Map<String, Object> finalResponse = Map.of("apiUrl", ecpayApiUrl, "params", ecpayParams);

            return ResponseEntity.ok(finalResponse);

        } catch (ParseException | JOSEException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token 無效或已過期: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
	
	
	/**
     * 【全新API】最終確認一筆「臨櫃付款」的訂單
     *
     * @param request      包含加密訂單編號和優惠券ID的請求
     * @param authHeader   JWT 授權標頭
     * @return 成功或失敗的回應
     */
    @PostMapping("/finalize-counter-reservation")
    public ResponseEntity<?> finalizeCounterReservation(@RequestBody UpdateOrderRequest request,
                                                        @RequestHeader("Authorization") String authHeader) {
        try {
            // 1. 從 Token 獲取 memberId (與其他 API 相同)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "無效的認證標頭"));
            }
            String token = authHeader.substring(7);
            JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
            Integer memberId = claims.getIntegerClaim("memberId");

            if (memberId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Token 中缺少 memberId 資訊"));
            }

            // 2. 將前端傳來的加密訂單編號解密為後端資料庫使用的整數 ID
            Integer orderId = OrderNumberUtils.decode(request.getEncodedOrderId());

            // 3. 呼叫 Service 執行臨櫃預訂的確認邏輯
            ticketOrderService.finalizeCounterReservation(orderId, request.getCouponId(), memberId);

            // 4. 回傳成功訊息
            return ResponseEntity.ok(Map.of("message", "臨櫃訂單預訂成功！"));

        } catch (ParseException | JOSEException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token 無效或已過期: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 處理例如訂單編號解碼失敗等問題
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            // 處理例如訂單狀態不符、優惠券無效等業務邏輯錯誤
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            // 處理例如訂單不存在等其他伺服器錯誤
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
