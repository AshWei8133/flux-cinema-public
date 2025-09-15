package com.flux.movieproject.model.dto.movie;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDetailDTO {
    private Integer genreId;
    private Integer tmdbGenreId;
    private String name;
    private List<MovieSimpleDTO> associatedMovies; // 關聯的電影簡化資訊列表
}
