package com.flux.movieproject.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderNumberUtils {
	/**
     * 品牌前綴
     */
    private static final String PREFIX = "FX";

    /**
     * 日期格式化 (yyMMdd - 兩位數年份)
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * 加鹽：一個固定的、隨機選擇的大質數，用來混淆真實的 ID。
     * 選擇一個數字即可，例如 112358。
     */
    private static final long SALT = 1140916L;

    /**
     * 將資料庫的整數 ID "加密" 成專業的訂單編號字串。
     *
     * @param id 資料庫中的 ticket_order_id (例如: 123)
     * @return 專業的訂單編號 (例如: "FX250826-2BS5")
     */
    public static String encode(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID 必須是正整數");
        }

        // 1. 加鹽
        long saltedId = id + SALT;

        // 2. 轉換為 36 進制並轉為大寫 (0-9, A-Z)
        String encodedPart = Long.toString(saltedId, 36).toUpperCase();

        // 3. 取得日期部分
        String datePart = LocalDate.now().format(DATE_FORMATTER);

        // 4. 組合
        return PREFIX + datePart + "-" + encodedPart;
    }

    /**
     * 將專業的訂單編號字串 "解密" 回原始的資料庫整數 ID。
     *
     * @param orderNumber 專業的訂單編號 (例如: "FX250826-2BS5")
     * @return 原始的 ticket_order_id (例如: 123)
     */
    public static Integer decode(String orderNumber) {
        if (orderNumber == null || !orderNumber.startsWith(PREFIX) || !orderNumber.contains("-")) {
            throw new IllegalArgumentException("無效的訂單編號格式");
        }

        try {
            // 1. 找到 '-' 的位置，並取出後面的加密部分
            String encodedPart = orderNumber.substring(orderNumber.lastIndexOf("-") + 1);

            // 2. 將 36 進制的字串解析回 long 型別的數字
            long saltedId = Long.parseLong(encodedPart, 36);

            // 3. 減去鹽，還原成原始 ID
            long originalId = saltedId - SALT;
            
            if (originalId <= 0) {
                 throw new IllegalArgumentException("解碼後的 ID 無效");
            }

            return (int) originalId;

        } catch (NumberFormatException e) {
            // 如果加密部分不是有效的 36 進制字串，會拋出例外
            throw new IllegalArgumentException("訂單編號的加密部分格式錯誤", e);
        }
    }
}