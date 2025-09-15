package com.flux.movieproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	@Bean // 標記為 Bean，讓 Spring 容器管理這個物件
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
