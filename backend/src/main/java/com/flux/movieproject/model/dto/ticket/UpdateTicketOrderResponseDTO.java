package com.flux.movieproject.model.dto.ticket;

import lombok.Data;

/**
 * 描述訂單是否新增、修改、刪除成功
 */
@Data
public class UpdateTicketOrderResponseDTO {
	private Boolean success;

	private String message;
}
