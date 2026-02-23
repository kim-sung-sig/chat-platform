package com.example.chat.message.config;

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
 * - 접근 URL: http://localhost:8081/swagger-ui.html
 * - API Docs: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI messageServerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("Chat Message Server API")
                .description("""
                        채팅 메시지 발송 서버 API
                        
                        ## 주요 기능
                        - 메시지 발송 (텍스트, 이미지, 파일, 동영상, 오디오)
                        - 실시간 메시지 발송
                        - Redis Pub/Sub 이벤트 발행
                        - Kafka를 통한 Push Service 연동
                        
                        ## 인증
                        JWT Bearer Token 사용 (Authorization 헤더)
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Chat Platform Team")
                        .email("support@chatplatform.com")
                        .url("https://chatplatform.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private List<Server> servers() {
        return List.of(
                new Server().url("http://localhost:8081").description("Local Development Server"),
                new Server().url("https://api-dev.chatplatform.com").description("Development Server"),
                new Server().url("https://api.chatplatform.com").description("Production Server"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)"));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearer-jwt");
    }
}
