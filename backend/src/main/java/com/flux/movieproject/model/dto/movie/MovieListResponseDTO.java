package com.flux.movieproject.model.dto.movie;

import java.util.Base64;

import lombok.Data;

/**
 * 用於在電影列表頁面（例如排片頁的側邊欄）回傳給前端的資料傳輸物件 (DTO)。
 * 對應前端 MovieCard.vue 元件所需要的資料結構。
 */
@Data 
public class MovieListResponseDTO {

    /**
     * 電影的唯一ID。
     * 對應前端 v-for 的 :key 和拖曳時的識別 ID。
     */
    private Integer id;

    /**
     * 電影的本地語系標題 (例如：中文標題)。
     */
    private String titleLocal;

    private String titleEnglish; 
    
    private String certification;
    /**
     * 電影的時長，單位為分鐘。
     */
    private Integer durationMinutes;

    /**
     * 電影的劇情簡介。
     */
    private String overview;

    /**
     * 電影海報的 Base64 Data URL 字串。
     * 對應前端 MovieCard.vue 中的 movie.posterImageUrl。
     */
    private String posterImageUrl;
    
//    預告連結
    private String trailerUrl; 

    /**
     * 輔助方法，由 MovieService 層在轉換 Entity 到 DTO 時調用。
     * 將從資料庫取出的 byte[] 格式的圖片，轉換為前端可以直接使用的 Base64 Data URL。
     *
     * @param posterImage 來自 Movie Entity 的 byte[] 圖片資料
     */
    public void setPosterImage(byte[] posterImage) {
        // 檢查從資料庫來的 byte 陣列是否為 null 或為空
        if (posterImage != null && posterImage.length > 0) {
            // 使用 Java 內建的 Base64 編碼器將 byte[] 轉換為 Base64 字串
            String base64String = Base64.getEncoder().encodeToString(posterImage);
            
            this.posterImageUrl = "data:image/jpeg;base64," + base64String;
        } else {
            this.posterImageUrl = null;
        }
    }
}