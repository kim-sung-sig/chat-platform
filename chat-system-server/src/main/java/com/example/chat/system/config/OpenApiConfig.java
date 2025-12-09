package com.example.chat.system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 *
 * API 문서 자동 생성을 위한 설정
 * - 접근 URL: http://localhost:8082/swagger-ui.html
 * - API Docs: http://localhost:8082/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI systemServerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * API 기본 정보
     */
    private Info apiInfo() {
        return new Info()
                .title("Chat System Server API")
                .description("""
                        채팅 시스템 관리 서버 API
                        
                        ## 주요 기능
                        - 채널 관리 (생성, 조회, 수정, 삭제)
                        - 메시지 관리 (CRUD, 상태 관리)
                        - 예약 메시지 (단발성, 주기적)
                        - 스케줄 관리 (일시중지, 재개, 취소)
                        
                        ## 인증
                        JWT Bearer Token 필요 (Authorization 헤더)
                        
                        ## Quartz Scheduler
                        - 단발성 스케줄: 특정 시간에 1회 실행
                        - 주기적 스케줄: Cron 표현식 기반 반복 실행
                        """)
                .version("v1.0.0")
                .contact(contact())
                .license(license());
    }

    /**
     * 연락처 정보
     */
    private Contact contact() {
        return new Contact()
                .name("Chat Platform Team")
                .email("support@chatplatform.com")
                .url("https://chatplatform.com");
    }

    /**
     * 라이선스 정보
     */
    private License license() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    /**
     * 서버 정보
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8082")
                        .description("Local Development Server"),
                new Server()
                        .url("https://api-dev-system.chatplatform.com")
                        .description("Development Server"),
                new Server()
                        .url("https://api-system.chatplatform.com")
                        .description("Production Server")
        );
    }

    /**
     * 보안 스키마 설정 (JWT)
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)")
                );
    }

    /**
     * 보안 요구사항
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("bearer-jwt");
    }
}
