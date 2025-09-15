package com.flux.movieproject.model.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MemberDTO { // 用於傳送給前端的資料

	private Integer memberId;

	private String username;

	private String gender;

	private LocalDate birthDate;

	private String email;

	private String phone;

	private LocalDateTime registerDate;

	private String loginMethod;

	private LocalDateTime lastLoginTime;

	private Integer memberPoints;
	
	 private String profilePhoto;

}
