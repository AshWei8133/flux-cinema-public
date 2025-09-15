package com.flux.movieproject.model.dto.member;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberProfileUpdateRequestDTO {

	private String username;

	private String gender;

	private LocalDate birthDate;

	private String phone;

}
