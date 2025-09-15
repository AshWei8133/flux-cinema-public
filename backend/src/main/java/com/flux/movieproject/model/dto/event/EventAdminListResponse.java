package com.flux.movieproject.model.dto.event;

import java.util.List;

public record EventAdminListResponse(
		Long count, // 活動總數
		List<EventAdminListDTO> list // EventDTO 列表
) {

}
