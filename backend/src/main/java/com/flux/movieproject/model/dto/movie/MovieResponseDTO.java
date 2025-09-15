package com.flux.movieproject.model.dto.movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 確保此註解存在，它會自動生成 getter 和 setter
@NoArgsConstructor // 確保此註解存在，提供無參建構子
@AllArgsConstructor // 確保此註解存在，提供所有參數的建構子
public class MovieResponseDTO {
    private Integer id;
    private Integer tmdbMovieId;
    private String titleLocal;
    private String titleEnglish;
    private LocalDate releaseDate;
    private LocalDate offShelfDate;
    private String certification;
    private String overview;
    private String trailerUrl;
    private Integer durationMinutes;
    // private byte[] posterImage; // 通常不直接在 JSON 中回傳二進位圖片，而是回傳圖片 URL
    private String originalLanguage;
    private Double popularity;
    private Double voteAverage;
    private Integer voteCount;
    private Boolean status;
    // private byte[] previewImage1; // 同上
    // ... 其他預覽圖片

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 關聯資料：電影的類型列表 (使用簡化的 GenreResponseDto)
    private List<GenreResponseDTO> genres; // 確保這裡的類型是 GenreResponseDto
}
