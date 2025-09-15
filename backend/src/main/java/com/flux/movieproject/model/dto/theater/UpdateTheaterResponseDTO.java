package com.flux.movieproject.model.dto.theater;

import lombok.Data;
/**
 * 描述是否新增、刪除、修改成功
 */
@Data
public class UpdateTheaterResponseDTO {
	private Boolean success;

	private String message;
}
