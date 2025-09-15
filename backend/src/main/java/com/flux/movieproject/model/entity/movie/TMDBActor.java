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
@Table(name = "tmdb_actor")
public class TMDBActor {
	
	@Id
	private Integer tmdbActorId;
	
	private String name;



}
