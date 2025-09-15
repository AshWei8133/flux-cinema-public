package com.flux.movieproject.controller.fluxapp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/api/payment/ecpay")
public class EcpayRedirectController {

	// 注入我們在 properties 中設定的前端網址
	@Value("${app.frontend.url}")
	private String frontendUrl;

	/**
	 * 處理綠界付款完成後，使用者瀏覽器的跳轉 (Client-side) 這個方法會接收到綠界的回傳參數，然後決定要重導向到前端的成功或失敗頁面
	 */
	@PostMapping("/result")
	public RedirectView handleEcpayResult(@RequestParam Map<String, String> callbackData) {

		// 簡單驗證 RtnCode 是否為 1 (成功)
		String rtnCode = callbackData.get("RtnCode");
		String orderNumber = callbackData.get("MerchantTradeNo");

		String redirectUrl;

		if ("1".equals(rtnCode)) {
			// 付款成功，準備跳轉到前端的成功頁面
			// 使用 UriComponentsBuilder 來安全地建構 URL
			redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                    .path("/payment-success") // 前端成功頁面的路由
					.queryParam("orderId", orderNumber) // 帶上訂單編號給前端顯示
					.toUriString();
		} else {
			// 付款失敗，準備跳轉到前端的失敗頁面
			String rtnMsg = callbackData.get("RtnMsg");
			redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                    .path("/payment-failure") // 前端失敗頁面的路由
					.queryParam("orderId", orderNumber).queryParam("message", rtnMsg) // 帶上失敗訊息
					.toUriString();
		}

		// 使用 RedirectView 物件來執行重導向
		return new RedirectView(redirectUrl);
	}

}
