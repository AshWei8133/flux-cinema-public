package com.flux.movieproject.model.dto.theater;

import java.util.Map;

import lombok.Data;

@Data
public class TheaterTypeUpdateDto {
	private Integer theaterTypeId;
    private Map<String, Object> changes;
}
