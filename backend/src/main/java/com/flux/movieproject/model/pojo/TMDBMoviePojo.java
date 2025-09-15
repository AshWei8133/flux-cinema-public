package com.flux.movieproject.model.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * POJO (Plain Old Java Object) - 一個單純的資料容器。 把它想像成一個 "購物籃"，專門用來裝
 * FetchTMDBDataUtil 從網路上抓回來的原始資料。 它與資料庫沒有直接關係。
 */
@Data
public class TMDBMoviePojo {

    private Integer tmdbMovieId;
    private String titleLocal;
    private String titleEnglish;
    private LocalDate releaseDate;
    private String certification;
    
    private List<Integer> genreIds;
    private List<TMDBDirectorPojo> directors;
    private List<TMDBActorPojo> actors;
    
    private String overview;
    private String trailerUrl;
    private Integer durationMinutes;
    private byte[] posterImage;
    private String originalLanguage;
    private BigDecimal popularity;
    private BigDecimal voteAverage;
    private Integer voteCount;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String posterPath;
}