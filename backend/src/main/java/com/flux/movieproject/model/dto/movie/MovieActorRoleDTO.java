package com.flux.movieproject.model.dto.movie;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorRoleDTO {
	private Integer movieId;
    private String movieTitleLocal;
    private String movieTitleEnglish;
    private LocalDate movieReleaseDate;
    private String characterName; // 飾演角色名
    private Integer orderNum; // 出場順序
    private Integer actorId; // 修正點：新增 actorId 欄位
    private String actorName; // 修正點：新增 actorName 欄位

}
