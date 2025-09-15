package com.flux.movieproject.model.dto.product.cart;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginResponseDTO {
    private String token;
    private Integer memberId;
    private String email;
}
