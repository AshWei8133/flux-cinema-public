package com.flux.movieproject.model.dto.movie;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 修正點：確保此註解存在，提供無參建構子
@AllArgsConstructor // 修正點：確保此註解存在，提供所有參數的建構子
public class MovieSimpleDTO {

	    private Integer id;
	    private String titleLocal;
	    private String titleEnglish;
	    private LocalDate releaseDate;
	    
	    
	
}
