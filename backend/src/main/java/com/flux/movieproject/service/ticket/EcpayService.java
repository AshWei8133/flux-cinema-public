package com.flux.movieproject.service.ticket;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EcpayService {
	// 從 application.properties 讀取設定
	@Value("${ecpay.merchantId}")
	private String merchantId;

	@Value("${ecpay.hashKey}")
	private String hashKey;

	@Value("${ecpay.hashIv}")
	private String hashIv;

	@Value("${ecpay.returnUrl}")
	private String returnUrl;

	@Value("${ecpay.clientBackUrl}")
	private String clientBackUrl;

	public Map<String, String> createCreditCardOrder(String orderId, Integer amount, String itemName) {
		// 1. 準備一個 TreeMap，它會自動按照 Key 的字母順序排序
		Map<String, String> params = new TreeMap<>();

		// 2. 放入固定的交易參數
		params.put("MerchantID", merchantId);
		params.put("MerchantTradeNo", orderId); // 訂單編號，每次都必須是唯一的
		params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(amount));
		params.put("TradeDesc", "Flux電影院 - 電影票訂購");
		params.put("ItemName", itemName); // 商品名稱，可以使用 # 分隔多個商品
		params.put("ReturnURL", returnUrl); // 後端接收付款結果的 callback URL
		params.put("ChoosePayment", "Credit"); // 指定付款方式為信用卡
		params.put("EncryptType", "1"); // 必須為 1，表示使用 SHA256 加密

		params.put("ClientRedirectURL", clientBackUrl);
		// (可選，但建議保留) 提供綠界結果頁面上的「返回商店」按鈕連結
		params.put("ClientBackURL", clientBackUrl);

		// 【重要】設定付款成功後，「自動跳轉」回商店的網址
		params.put("OrderResultURL", clientBackUrl);

		// 3. 產生最重要的 CheckMacValue
		String checkMacValue = generateCheckMacValue(params);
		params.put("CheckMacValue", checkMacValue);

		return params;
	}

	/**
	 * 產生綠界需要的 CheckMacValue
	 * 
	 * @param params 包含所有交易參數的 Map
	 * @return 加密後的 CheckMacValue 字串
	 */
	private String generateCheckMacValue(Map<String, String> params) {
		// 1. 將參數依照 Key 的字母順序排序
		// 因為我們一開始就使用 TreeMap，所以這一步已經自動完成了。

		// 2. 將參數用 & 符號串接起來，格式為 Key1=Value1&Key2=Value2...
		String paramString = params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("&"));

		// 3. 在字串前後加上 HashKey 和 HashIV
		String fullString = "HashKey=" + hashKey + "&" + paramString + "&HashIV=" + hashIv;

		// 4. 對整個字串進行 URL-Encode
		String urlEncodedString;
		try {
			urlEncodedString = URLEncoder.encode(fullString, StandardCharsets.UTF_8.toString()).toLowerCase();
		} catch (UnsupportedEncodingException e) {
			// 這個錯誤在現代 Java 版本中幾乎不可能發生
			throw new RuntimeException(e);
		}

		// 5. 使用 SHA256 進行加密
		String hashedString;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(urlEncodedString.getBytes(StandardCharsets.UTF_8));

			// 6. 將 byte 陣列轉換為十六進位的字串
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			hashedString = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// 這個錯誤在現代 Java 版本中也幾乎不可能發生
			throw new RuntimeException(e);
		}

		// 7. 將結果轉為大寫
		return hashedString.toUpperCase();
	}

	/**
	 * 驗證綠界回傳的 CheckMacValue 是否正確
	 * 
	 * @param callbackData 綠界回傳的資料 Map
	 * @return 如果驗證通過回傳 true，否則回傳 false
	 */
	public boolean isValidCheckMacValue(Map<String, String> callbackData) {
		// 1. 取得綠界回傳的 CheckMacValue
		String receivedCheckMacValue = callbackData.get("CheckMacValue");

		// 2. 移除 CheckMacValue，因為它不參與驗證
		Map<String, String> tempParams = new TreeMap<>(callbackData);
		tempParams.remove("CheckMacValue");

		// 3. 重新生成 CheckMacValue
		String calculatedCheckMacValue = generateCheckMacValue(tempParams);

		// 4. 比較兩者是否一致
		return receivedCheckMacValue != null && receivedCheckMacValue.equals(calculatedCheckMacValue);
	}

}
