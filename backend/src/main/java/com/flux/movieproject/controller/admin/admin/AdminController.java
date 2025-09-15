package com.flux.movieproject.controller.admin.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.admin.AdminLoginResponseDTO;
import com.flux.movieproject.model.entity.admin.Admin;
import com.flux.movieproject.service.admin.AdminService;
import com.flux.movieproject.utils.AdminJwtUtil;
import com.nimbusds.jose.JOSEException;

/**
 * 管理員後台操作controller api： 1. post /api/admin/login 2. post /api/admin/logout
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@Autowired
	AdminJwtUtil adminJwtUtil;

	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(@RequestBody Admin admin) {
		// 呼叫service驗證帳號密碼
		Admin loggedInAdmin = adminService.adminLogin(admin);
		// 2. 判斷驗證是否成功
		if (loggedInAdmin != null) {
			// 3. 如果驗證成功，生成一個專屬於後台的 JWT Token
			try {
				String token = adminJwtUtil.generateToken(loggedInAdmin.getAdminName());
				
				// 4. 將 Token 放入回應物件中
				AdminLoginResponseDTO response = new AdminLoginResponseDTO();
				response.setToken(token);
				System.out.println("🎉 後台管理員 " + loggedInAdmin.getAdminName() + " 登入成功，Token 已生成！");
				
				// 5. 返回 200 OK 狀態碼和 Token
				return ResponseEntity.ok(response);
			} catch (JOSEException e) {
				System.out.println("❌ 生成 Token 失敗: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token 生成失敗，請聯繫系統管理員。");
			}

		} else {
			// 6. 如果驗證失敗，返回 401 Unauthorized 狀態碼
			System.out.println("❌ 管理員登入失敗，帳號或密碼錯誤。");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("帳號或密碼錯誤");
		}
	}
}
