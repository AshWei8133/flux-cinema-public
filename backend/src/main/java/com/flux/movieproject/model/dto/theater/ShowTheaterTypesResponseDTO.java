package com.flux.movieproject.model.dto.theater;

import lombok.Data;

@Data
public class ShowTheaterTypesResponseDTO {
	private Integer theaterTypeId;

	private String theaterTypeName;

	private String description;
}
