package com.flux.movieproject.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MemberAuthenticationInterceptor implements HandlerInterceptor {
	@Autowired
	private MemberJwtUtil memberJwtUtil;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = request.getRequestURI();
		System.out.println("MemberAuthenticationInterceptor called for URI: " + uri);
        // 對於 OPTIONS 請求，直接放行，以處理 CORS 預檢請求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }



		if (uri.startsWith("/api/public/product")){
			return true;
		}

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				// 使用會員的 JWT 工具類來解析 Token
				JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);

				// 檢查 Token 是否過期
				if (claims.getExpirationTime().before(new Date())) {
					System.out.println("❌ 前台 Token 已過期");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("憑證已過期，請重新登入！");
					return false;
				}

				// 檢查 Token 是否包含 "member" 角色
				if ("member".equals(claims.getStringClaim("role"))) {
					System.out.println("✅ 前台 Token 驗證成功，使用者：" + claims.getSubject());
					System.out.println("memberId=" + claims.getIntegerClaim("memberId"));
					// 你可以在這裡將使用者資訊存入 request attribute，方便後續 Controller 使用
					request.setAttribute("memberId", claims.getIntegerClaim("memberId"));
					// request.setAttribute("memberEmail", claims.getSubject());
					return true; // 驗證成功，放行
				} else {
					System.out.println("❌ Token 無效，角色錯誤");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write("您沒有權限訪問此資源！");
					return false;
				}
			} catch (Exception e) {
				System.out.println("❌ 前台 Token 無效或簽名錯誤：" + e.getMessage());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("憑證無效，請重新登入！");
				return false;
			}
		}

		System.out.println("❌ 前台請求未經授權，缺少 Token");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write("未經授權，請先登入！");
		return false;
	}
}