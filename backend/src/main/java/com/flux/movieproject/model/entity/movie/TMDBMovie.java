package com.flux.movieproject.model.entity.movie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tmdb_movie")
public class TMDBMovie {

	@Id
	private Integer tmdbMovieId; // TMDB電影ID（PK） 

	private String titleLocal; // 中文片名 

	private String titleEnglish; // 原始片名 

	private LocalDate releaseDate; // 上映日期 

	private String certification; // 分級 

	private String overview; // 劇情簡介 

	private String trailerUrl; // 預告片連結 

	private Integer durationMinutes; // 片長（分鐘） 

	private byte[] posterImage; // 封面圖（圖片二進位資料） 

	private String originalLanguage; // 原始語言 

	private BigDecimal popularity; // 熱門指數 

	private BigDecimal voteAverage; // 平均評分 

	private Integer voteCount; // 評分人數 

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//前端對應的時間格式，要搭配雙層大括號 
	@Temporal(TemporalType.TIMESTAMP) 
	private LocalDateTime createTime; // 建立時間(後續興建到資料庫再加) 

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//前端對應的時間格式，要搭配雙層大括號 
	@Temporal(TemporalType.TIMESTAMP) 
	private LocalDateTime updateTime; // 更新時間(後續興建到資料庫再考慮加) 

	  // --- TMDB 關聯 ---
	@OneToMany(mappedBy = "tmdbMovie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TMDBMovieDirector> tmdbMovieDirectors = new HashSet<>();

    @OneToMany(mappedBy = "tmdbMovie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TMDBMovieGenre> tmdbMovieGenres = new HashSet<>();
    
    @OneToMany(mappedBy = "tmdbMovie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TMDBMovieActor> tmdbMovieActors = new HashSet<>();

}
