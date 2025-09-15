package com.flux.movieproject.model.dto.event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用於接收更新活動狀態請求的 DTO。
 */
@Data
@Getter
@Setter
public class StatusUpdateRequest {

	private String status;

}