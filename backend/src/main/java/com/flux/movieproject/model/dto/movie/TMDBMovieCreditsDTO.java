package com.flux.movieproject.model.dto.movie;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TMDBMovieCreditsDTO {

	private Integer id; // 電影ID
    private List<TMDBCastDto> cast; // 演員列表
    private List<TMDBCrewDto> crew; // 劇組人員列表 (包含導演)

    @Data
    public static class TMDBCastDto {
        private Integer id; // 演員ID
        private String name; // 演員姓名
        private String character; // 飾演角色名
        @JsonProperty("order")
        private Integer orderNum; // 出場順序
        private String biography; // 演員簡介 (TMDB API /person/{person_id} 才有，這裡先預留)
    }

    @Data
    public static class TMDBCrewDto {
        private Integer id; // 人員ID
        private String name; // 人員姓名
        private String job; // 職位 (例如 "Director")
        private String department; // 部門
        private String biography; // 人員簡介 (TMDB API /person/{person_id} 才有，這裡先預留)
    }
}
