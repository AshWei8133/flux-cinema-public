package com.flux.movieproject.model.entity.movie;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tmdb_genre")
public class TMDBGenre {

	@Id
	private Integer tmdbGenreId;
	
	private String name;
	

}
