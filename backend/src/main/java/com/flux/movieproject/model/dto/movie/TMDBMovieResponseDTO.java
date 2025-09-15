package com.flux.movieproject.model.dto.movie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class TMDBMovieResponseDTO {
	private Integer tmdbMovieId;
	private String titleLocal;
	private String titleEnglish;
	private LocalDate releaseDate;
	private String certification;
	private String overview;
	private String trailerUrl;
	private Integer durationMinutes;
	// posterImage 是 byte[]，如果前端需要 URL 而不是 byte 陣列，
	// 你可以考慮建立一個專門的端點來提供圖片，這裡只回傳一個標識或 URL。
	// 為了簡單起見，我們先不包含它，或者你可以將其轉為 Base64 字串。
	private String originalLanguage;
	private BigDecimal popularity;
	private BigDecimal voteAverage;
	private Integer voteCount;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
	// 【新增】用來存放 Base64 格式的圖片字串
	private String posterImageBase64;

	// 關聯的資料也使用 DTO
	private Set<DirectorDTO> directors;
	private Set<GenreDTO> genres;
	private Set<ActorDTO> actors;
}
