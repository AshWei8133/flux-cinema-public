package com.flux.movieproject.model.dto.movie;

import lombok.Data;

@Data
public class ActorDTO {
	private Integer tmdbActorId;
    private String name;
    private String character;
    private Integer orderNum;
}
