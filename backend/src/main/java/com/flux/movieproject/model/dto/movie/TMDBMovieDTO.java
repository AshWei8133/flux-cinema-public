package com.flux.movieproject.model.dto.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TMDBMovieDTO {
	
	    private Integer id;
	    private String title;

	    @JsonProperty("original_title")
	    private String originalTitle;

	    @JsonProperty("release_date")
	    private LocalDate releaseDate;

	    @JsonProperty("tagline")
	    private String tagline;

	    private String overview;

	    @JsonProperty("poster_path")
	    private String posterPath;

	    @JsonProperty("backdrop_path")
	    private String backdropPath;

	    @JsonProperty("original_language")
	    private String originalLanguage;

	    @JsonProperty("popularity")
	    private BigDecimal popularity;
	    
	    @JsonProperty("trailer_url")
	    private String trailerUrl; 

	    @JsonProperty("vote_average")
	    private BigDecimal voteAverage;

	    @JsonProperty("vote_count")
	    private Integer voteCount;

	    @JsonProperty("runtime")
	    private Integer runtimeMinutes;

	    private List<TMDBGenreDto> genres;

	    @Data
	    public static class TMDBGenreDto {
	        private Integer id;
	        private String name;
	    }

}
