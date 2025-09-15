package com.flux.movieproject.model.dto.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TMDBMovieSearchResponseDTO {
    private Integer page;
    private List<TMDBMovieDTO> results; // 這裡的 TMDBMovieDto 欄位較少，但可用來取得電影ID
    @JsonProperty("total_pages")
    private Integer totalPages;
    @JsonProperty("total_results")
    private Integer totalResults;
}
