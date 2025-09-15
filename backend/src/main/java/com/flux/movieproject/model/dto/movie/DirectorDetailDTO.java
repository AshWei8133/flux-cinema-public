package com.flux.movieproject.model.dto.movie;

import java.util.List;

import lombok.Data;

@Data
public class DirectorDetailDTO {

	    private Integer directorId;
	    private Integer tmdbDirectorId;
	    private String name;
	    private String directorSummary;
	    private List<MovieSimpleDTO> associatedMovies; // 關聯的電影簡化資訊列表
}
