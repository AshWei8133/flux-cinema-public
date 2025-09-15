package com.flux.movieproject.model.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeRequestDTO {

	private String oldPassword;

	private String newPassword;
}
