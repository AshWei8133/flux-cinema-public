package com.flux.movieproject.model.dto.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 確保此註解存在，它會自動生成 getter 和 setter
@NoArgsConstructor // 確保此註解存在，提供無參建構子
@AllArgsConstructor // 確保此註解存在，提供所有參數的建構子
public class GenreResponseDTO {
    private Integer genreId;
    private Integer tmdbGenreId;
    private String name;
}
