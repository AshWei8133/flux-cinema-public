package com.flux.movieproject.controller.fluxapp.member;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.member.AuthResponseDTO;
import com.flux.movieproject.model.dto.member.GoogleLoginRequestDTO;
import com.flux.movieproject.model.dto.member.MemberDTO;
import com.flux.movieproject.model.dto.member.MemberLoginRequestDTO;
import com.flux.movieproject.model.dto.member.MemberRequestDTO;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.service.member.MemberService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jose.JOSEException;

@RestController
@RequestMapping("/api/member")
public class MemberController {
	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberJwtUtil memberJwtUtil;

	/**
	 * 會員註冊 API
	 */
	@PostMapping(value = "/register")
	public ResponseEntity<?> registerMember(@RequestBody MemberRequestDTO request) {
		try {
			// 呼叫 service 時，file 參數直接傳入 null
			MemberDTO newMember = memberService.createMember(request, null);
			return ResponseEntity.status(HttpStatus.CREATED).body(newMember);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("註冊失敗，發生未知錯誤。");
		}
	}

	/**
	 * 會員登入 API
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody MemberLoginRequestDTO loginRequest) {
		// 1. 呼叫 Service 進行帳號密碼驗證
		Member authenticatedMember = memberService.login(loginRequest);

		// 2. 判斷驗證是否成功
		if (authenticatedMember != null) {
			try {
				// 3. 如果驗證成功，使用 MemberJwtUtil 生成 JWT Token
				// 使用 email 當作 Token 的 subject (主體)
				String token = memberJwtUtil.generateToken(authenticatedMember.getEmail(),
						authenticatedMember.getMemberId());

				// 4. 準備要回傳給前端的會員資訊 (不包含密碼)
				MemberDTO memberInfo = new MemberDTO();
				BeanUtils.copyProperties(authenticatedMember, memberInfo, "password", "profilePhoto");

				// 5. 將 Token 和會員資訊包裝起來回傳
				AuthResponseDTO response = new AuthResponseDTO(token, memberInfo);
				System.out.println("前台會員 " + authenticatedMember.getEmail() + " 登入成功，Token 已生成！");

				return ResponseEntity.ok(response);

			} catch (JOSEException e) {
				System.out.println("❌ 生成 Member Token 失敗: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token 生成失敗，請聯繫系統管理員。");
			}
		} else {
			// 6. 驗證失敗，返回 401 Unauthorized
			System.out.println("❌ 會員登入失敗，Email 或密碼錯誤。");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email 或密碼錯誤");
		}
	}

	/**
	 * Google登入
	 */
	@PostMapping("/google-login")
	public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequestDTO loginRequest) {
		try {
			// 1. 呼叫 Service 進行 Google 登入驗證
			Member authenticatedMember = memberService.googleLogin(loginRequest);
			// 2. 判斷驗證是否成功
			if (authenticatedMember != null) {
				// 3. 如果驗證成功，使用 MemberJwtUtil 生成 JWT Token
				String token = memberJwtUtil.generateToken(authenticatedMember.getEmail(),
						authenticatedMember.getMemberId());

				// 4. 準備要回傳給前端的會員資訊
				MemberDTO memberInfo = new MemberDTO();
				BeanUtils.copyProperties(authenticatedMember, memberInfo, "password", "profilePhot");

				// 5. 將 Token 和會員資訊包裝起來回傳
				AuthResponseDTO response = new AuthResponseDTO(token, memberInfo);
				System.out.println("前台會員" + authenticatedMember.getEmail() + "(Google)登入成功，Token 已生成！");

				return ResponseEntity.ok(response);
			} else {
				// 6. 驗證失敗 (理論上 token 無效時 service 會直接拋出例外)
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("無效的 Google 憑證");
			}
		} catch (Exception e) {
			// 捕捉 Service 層可能拋出的所有例外
			System.out.println("❌ Google 登入失敗:" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Google 登入失敗:" + e.getMessage());
		}
	}

	/**
	 * 檢查電子郵件和電話是否存在 (用於忘記密碼流程)
	 * 
	 * @param payload
	 * @return
	 */
	@PostMapping("/verify-identity")
	public ResponseEntity<?> checkIdentity(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		String phone = payload.get("phone");
		if (email == null || email.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email and phone are required."));
		}
		boolean exists = memberService.verifyEmailAndPhone(email, phone);

		if (exists) {
			return ResponseEntity.ok().body(Map.of("message", "Identity verified."));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Email or Phone not found."));
		}
	}

	/**
	 * 重設密碼
	 */
	@PostMapping("/reset-password-insecure")
	public ResponseEntity<?> insecureResetPassword(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");
			String newPassword = payload.get("newPassword");
			if (email == null || newPassword == null) {
				return ResponseEntity.badRequest().body("Email and newPassword are required.");
			}
			memberService.insecureResetPassword(email, newPassword);
			return ResponseEntity.ok().body(Map.of("message", "Password reset successfully for testing."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

}