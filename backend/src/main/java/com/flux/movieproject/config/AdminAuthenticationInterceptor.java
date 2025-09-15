package com.flux.movieproject.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.flux.movieproject.utils.AdminJwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminAuthenticationInterceptor implements HandlerInterceptor {
	@Autowired
	private AdminJwtUtil adminJwtUtil;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// 解決 CORS preflight 問題的關鍵程式碼 START
		// 在執行任何驗證之前，先判斷是不是 OPTIONS 預檢請求，若是則直接放行
		if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
			System.out.println("✅ 收到 OPTIONS 預檢請求，直接放行");
			response.setStatus(HttpServletResponse.SC_OK);
			return true;
		}

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				// 使用新的工具類來解析 Token
				JWTClaimsSet claims = adminJwtUtil.getClaimsFromToken(token);

				// 檢查 Token 是否過期
				if (claims.getExpirationTime().before(new Date())) {
					System.out.println("❌ 後台 Token 已過期");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("後台 Token 已過期，請重新登入！");
					return false;
				}

				// 檢查 Token 是否包含 "admin" 角色
				if ("admin".equals(claims.getStringClaim("role"))) {
					System.out.println("✅ 後台 Token 驗證成功，使用者：" + claims.getSubject());
					return true;
				} else {
					System.out.println("❌ Token 無效，角色錯誤");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write("您沒有後台操作權限！");
					return false;
				}
			} catch (Exception e) {
				System.out.println("❌ 後台 Token 無效或簽名錯誤：" + e.getMessage());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("後台 Token 無效！");
				return false;
			}
		}

		System.out.println("❌ 後台請求未經授權，缺少 Token");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write("未經授權，請提供有效的後台 Token！");
		return false;
	}
}
