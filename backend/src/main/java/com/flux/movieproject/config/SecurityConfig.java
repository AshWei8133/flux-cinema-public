package com.flux.movieproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 演算法來加密密碼
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 為了讓我們的 JWT 攔截器能夠正常運作，
        // 我們需要停用 Spring Security 預設的 httpBasic 和 formLogin 驗證機制。
        // 同時也停用 CSRF 保護，因為在前後端分離的 RESTful API 中，通常使用 JWT 來驗證，CSRF 的必要性較低。
        http
            .csrf(csrf -> csrf.disable()) // 停用 CSRF 保護
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // 暫時允許所有請求，因為我們將使用自訂的 Interceptor 來控制權限
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // 停用 HTTP Basic 認證
            .formLogin(formLogin -> formLogin.disable()); // 停用表單登入

        return http.build();
    }
}
