package com.flux.movieproject.model.dto.movie;

import java.util.List;

import com.flux.movieproject.model.entity.movie.Genre;
import com.flux.movieproject.model.entity.movie.Movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviesAllDataDTO {
	private Movie movie;
	private List<Genre> genres;
	

}
