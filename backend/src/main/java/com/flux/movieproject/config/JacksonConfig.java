package com.flux.movieproject.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.StreamReadConstraints;

@Configuration
public class JacksonConfig {

	 /**
     * 建立一個 Bean 來修改 Jackson 的預設設定
     * @return 一個自訂器物件
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customStreamReadConstraints() {
        return builder -> {
            // builder.postConfigurer 的意思是：「等 Jackson 的主要設定都完成後，我再做一些額外的微調。」
            builder.postConfigurer(objectMapper -> {
                // 我們從 objectMapper 拿到它底層的工廠 (Factory)
                // 然後設定它的「讀取限制」(StreamReadConstraints)
                objectMapper.getFactory()
                    .setStreamReadConstraints(
                        // 我們建立一個新的限制規則，並只修改最大字串長度
                        StreamReadConstraints.builder()
                            .maxStringLength(20_000_000) // 設定為 20MB (底線是為了方便閱讀)
                            .build()
                    );
            });
        };
    }
}
