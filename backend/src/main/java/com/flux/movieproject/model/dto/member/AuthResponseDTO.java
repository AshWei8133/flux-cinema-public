package com.flux.movieproject.model.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // 建立一個包含所有參數的建構子
public class AuthResponseDTO {
    private String token;
    private MemberDTO memberInfo;
}
