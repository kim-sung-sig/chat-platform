package com.example.chat.message.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI (Swagger) 설정
 *
 * API 문서 자동 생성을 위한 설정
 * - 접근 URL: http://localhost:8081/swagger-ui.html
 * - API Docs: http://localhost:8081/v3/api-docs
 */
@Configuration
class OpenApiConfig {

	@Bean
	fun messageServerOpenAPI(): OpenAPI {
		return OpenAPI()
			.info(apiInfo())
			.servers(servers())
			.components(securityComponents())
			.addSecurityItem(securityRequirement())
	}

	/**
	 * API 기본 정보
	 */
	private fun apiInfo(): Info {
		return Info()
			.title("Chat Message Server API")
			.description(
				"""
                채팅 메시지 발송 서버 API
                
                ## 주요 기능
                - 메시지 발송 (텍스트, 이미지, 파일, 비디오, 오디오)
                - 답장 메시지 발송
                - Redis Pub/Sub 이벤트 발행
                - Kafka를 통한 Push Service 연동
                
                ## 인증
                JWT Bearer Token 필요 (Authorization 헤더)
                """.trimIndent()
			)
			.version("v1.0.0")
			.contact(contact())
			.license(license())
	}

	/**
	 * 연락처 정보
	 */
	private fun contact(): Contact {
		return Contact()
			.name("Chat Platform Team")
			.email("support@chatplatform.com")
			.url("https://chatplatform.com")
	}

	/**
	 * 라이선스 정보
	 */
	private fun license(): License {
		return License()
			.name("Apache 2.0")
			.url("https://www.apache.org/licenses/LICENSE-2.0.html")
	}

	/**
	 * 서버 정보
	 */
	private fun servers(): List<Server> {
		return listOf(
			Server()
				.url("http://localhost:8081")
				.description("Local Development Server"),
			Server()
				.url("https://api-dev.chatplatform.com")
				.description("Development Server"),
			Server()
				.url("https://api.chatplatform.com")
				.description("Production Server")
		)
	}

	/**
	 * 보안 스키마 설정 (JWT)
	 */
	private fun securityComponents(): Components {
		return Components()
			.addSecuritySchemes(
				"bearer-jwt", SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.`in`(SecurityScheme.In.HEADER)
					.name("Authorization")
					.description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)")
			)
	}

	/**
	 * 보안 요구사항
	 */
	private fun securityRequirement(): SecurityRequirement {
		return SecurityRequirement()
			.addList("bearer-jwt")
	}
}
