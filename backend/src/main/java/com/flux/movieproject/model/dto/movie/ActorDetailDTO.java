package com.flux.movieproject.model.dto.movie;

import java.util.List;

import lombok.Data;

@Data
public class ActorDetailDTO {
	private Integer actorId;
    private Integer tmdbActorId;
    private String name;
    private String biography;
    private List<MovieActorRoleDTO> associatedMovies; // 關聯的電影及角色列表
}
