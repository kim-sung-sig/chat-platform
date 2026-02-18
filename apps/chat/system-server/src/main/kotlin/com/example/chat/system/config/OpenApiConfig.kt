package com.example.chat.system.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

	@Bean
	fun openAPI(): OpenAPI {
		return OpenAPI()
			.info(
				Info()
					.title("Chat System API")
					.version("1.0")
					.description("채팅 시스템 관리 API")
			)
	}
}
