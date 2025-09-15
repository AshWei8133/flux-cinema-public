package com.flux.movieproject.model.dto.admin;

import lombok.Data;

/**
 * 封裝登入後回傳的token
 */
@Data
public class AdminLoginResponseDTO {
	private String token;
}
