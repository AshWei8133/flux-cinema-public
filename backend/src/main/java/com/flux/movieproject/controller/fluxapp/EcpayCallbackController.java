package com.flux.movieproject.controller.fluxapp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.service.ticket.EcpayService;
import com.flux.movieproject.service.ticket.TicketOrderService;

@RestController
@RequestMapping("/api/payment/ecpay")
public class EcpayCallbackController {

	@Autowired
	private EcpayService ecpayService; // 讓它能呼叫 EcpayService 的驗證方法

	@Autowired
	private TicketOrderService ticketOrderService;

	/**
	 * 處理綠界金流回傳的交易結果
	 *
	 * @param callbackData 綠界回傳的 Key-Value 參數，包含交易狀態、訂單編號等
	 * @return 必須回傳固定的字串 "1|OK" 給綠界，代表接收成功
	 */
	@PostMapping("/notify")
	public String handleEcpayCallback(@RequestParam Map<String, String> callbackData) {
		System.out.println("收到綠界回傳的通知： " + callbackData);

		// 1. 【核心】驗證資料的 CheckMacValue，確保資料未被竄改
		if (!ecpayService.isValidCheckMacValue(callbackData)) {
			System.err.println("CheckMacValue 驗證失敗，回傳資料有誤！");
			return "0|ErrorMessage";
		}

		// 2. 根據回傳的訂單狀態，處理業務邏輯
		String rtnCode = callbackData.get("RtnCode");
		String orderNumber = callbackData.get("MerchantTradeNo");

		// 綠界的回傳代碼 1 代表交易成功
		if ("1".equals(rtnCode)) {
			// 處理付款成功
			String transactionId = callbackData.get("TradeNo"); // 綠界交易編號
			try {
				ticketOrderService.handlePaymentSuccess(orderNumber, transactionId);
				System.out.println("訂單 " + orderNumber + " 已成功更新為已付款。");
			} catch (Exception e) {
				System.err.println("更新訂單狀態時發生錯誤：" + e.getMessage());
				return "0|ErrorUpdatingOrder";
			}
		} else {
			// 處理付款失敗或取消
			try {
				ticketOrderService.handlePaymentFailure(orderNumber);
				System.out.println("訂單 " + orderNumber + " 已處理付款失敗/取消。");
			} catch (Exception e) {
				System.err.println("處理訂單失敗狀態時發生錯誤：" + e.getMessage());
				return "0|ErrorHandlingFailure";
			}
		}

		// 3. 【重要】成功處理後，必須回傳固定的 "1|OK" 字串給綠界
		return "1|OK";
	}


}
