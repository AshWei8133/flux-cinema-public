package com.flux.movieproject.utils;

import java.util.Base64;

public class PictureConverter {
	/**
	 * 輔助工具：將base64圖片轉換為byte[]格式
	 * @param pictureBase64 Base64 字串
	 * @return 圖片的 byte[] 陣列
	 */
	public static byte[] convertBase64ToBytes(String pictureBase64) {
		if (pictureBase64 == null || pictureBase64.isEmpty()) {
            return null;
        }
        try {
            // 使用 Base64.getDecoder() 將 Base64 字串解碼成 byte 陣列
            return Base64.getDecoder().decode(pictureBase64);
        } catch (IllegalArgumentException e) {
            // 如果 Base64 字串格式不正確，可以進行錯誤處理或回傳 null
            System.err.println("無效的 Base64 字串: " + e.getMessage());
            return null;
        }
	}
}
