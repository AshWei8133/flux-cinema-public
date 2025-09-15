package com.flux.movieproject.model.dto.movie;

import lombok.Data;

@Data
public class AddActorToCastRequestDTO {

	private Integer actorId;
	
	private String characterName;
}
