package com.flux.movieproject.model.dto.movie;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 修正點：確保此註解存在，提供無參建構子
@AllArgsConstructor // 修正點：確保此註解存在，提供所有參數的建構子
public class DirectorListDTO {
	
	    private Integer directorId;
	    private Integer tmdbDirectorId;
	    private String name;
	    private String directorSummary;
	    private List<MovieSimpleDTO> associatedMovies; // 關聯的電影簡化資訊列表
	

}
