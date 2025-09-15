package com.flux.movieproject.model.dto.theater;

import java.util.List;

import lombok.Data;

@Data
public class TheaterTypeChangesRequest {
	private List<TheaterTypeCreateDto> added;
    private List<TheaterTypeUpdateDto> updated;
    private List<Integer> deleted; // 假設 ID 是 Long 型態

}
