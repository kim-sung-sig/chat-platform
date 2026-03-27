package com.example.chat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI chatServerOpenAPI() {
		return new OpenAPI().info(new Info().title("Chat Server API").version("v1").description("채팅 서버 API (message + system 통합)"));
	}
}
