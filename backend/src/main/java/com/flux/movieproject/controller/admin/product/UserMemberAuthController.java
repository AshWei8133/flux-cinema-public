package com.flux.movieproject.controller.admin.product;

import com.flux.movieproject.model.dto.member.MemberLoginRequestDTO;
import com.flux.movieproject.model.dto.product.cart.MemberLoginResponseDTO;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.service.member.MemberService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/member")
public class UserMemberAuthController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberJwtUtil memberJwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> memberLogin(@RequestBody MemberLoginRequestDTO loginRequest) {
        // 使用 DTO 呼叫 Service 登入
        System.out.println("UserMemberAuthController.memberLogin 被呼叫了");
        Member loggedInMember = memberService.login(loginRequest);

        if (loggedInMember != null) {
            try {
                // 呼叫雙參數版本生成 Token
                String token = memberJwtUtil.generateToken(loggedInMember.getEmail(), loggedInMember.getMemberId());

                MemberLoginResponseDTO response = new MemberLoginResponseDTO();
                response.setToken(token);
                response.setMemberId(loggedInMember.getMemberId());
                response.setEmail(loggedInMember.getEmail());

                return ResponseEntity.ok(response);
            } catch (JOSEException e) {
                // Token 生成失敗，返回 500
                System.out.println("JWT 生成失敗：" + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("JWT Token 生成失敗，請聯繫系統管理員");
            }
        } else {
            // 登入失敗，返回 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("帳號或密碼錯誤");
        }
    }
}
