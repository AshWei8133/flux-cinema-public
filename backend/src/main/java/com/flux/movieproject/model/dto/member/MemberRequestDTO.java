package com.flux.movieproject.model.dto.member;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MemberRequestDTO { // 用於前端傳來的資料

	private String username;

	private String password; // 密碼只在新增/更新時傳入

	private String gender;

	private LocalDate birthDate;

	private String email;

	private String phone;

	private Integer memberPoints;

	private String loginMethod;

	private String thirdPartyLoginId;
	
	/**
     * 用於接收前端傳來的 Base64 編碼的頭像圖片字串
     * 前端傳送的格式可能包含 Data URI scheme (例如 "data:image/png;base64,iVBORw0KGgo...")
     */
	private String profilePhotoBase64;

}
