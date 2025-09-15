package com.flux.movieproject.model.dto.movie;

import java.util.List;

import com.flux.movieproject.model.entity.movie.Actor;
import com.flux.movieproject.model.entity.movie.Director;
import com.flux.movieproject.model.entity.movie.Genre;
import com.flux.movieproject.model.entity.movie.Movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviePublicDetailDTO {
     /**
     * 完整的電影實體物件
     */
    private Movie movie;

    /**
     * 該電影所屬的類型列表
     */
    private List<Genre> genres;
    
    /**
     * 【已新增】該電影的導演列表
     */
    private List<Director> directors;
    
    /**
     * 【已新增】該電影的演員列表
     */
    private List<Actor> actors;
}
