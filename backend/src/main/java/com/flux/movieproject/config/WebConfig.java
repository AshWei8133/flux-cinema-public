package com.flux.movieproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private AdminAuthenticationInterceptor adminInterceptor;

	@Autowired
	private MemberAuthenticationInterceptor memberInterceptor;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**") // 對所有 /api/ 下的路徑啟用 CORS
				.allowedOrigins("http://localhost:5173", "http://localhost:5174","https://payment-stage.ecpay.com.tw","http://www.fluxcinema.com:5173") // 允許您的前端應用程式的來源
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 允許的 HTTP 方法
				.allowedHeaders("*") // 允許所有請求頭
				.allowCredentials(true) // 允許發送 cookies (如果使用 Session 或 JWT 存在 cookie 中)
				.maxAge(3600); // 預檢請求的緩存時間 (秒)
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 註冊後台攔截器，只應用於 /api/admin 路徑
		registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**")
				.excludePathPatterns("/api/admin/login"); // 排除後台登入 API

		// --- 前台會員攔截器設定 ---
		registry.addInterceptor(memberInterceptor)
				// 假設需要登入才能訪問的路徑
				.addPathPatterns("/api/**")
				.addPathPatterns("/api/public/cart/**")
				.excludePathPatterns("/api/admin/**") // 會員攔截器不攔截後台 API
				// 從 ApiWhiteList 動態讀取要排除的路徑
				.excludePathPatterns(ApiWhiteList.PUBLIC_API_LIST);
	}
}
