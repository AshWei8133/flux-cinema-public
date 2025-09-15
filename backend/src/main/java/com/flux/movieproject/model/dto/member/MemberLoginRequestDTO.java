package com.flux.movieproject.model.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginRequestDTO {
    private String email;
    private String password;
}
