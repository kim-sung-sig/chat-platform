package com.example.chat.gateway;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 *
 * 담당 기능:
 * - 라우팅 (서비스 디스커버리 기반 lb://)
 * - 로깅
 * - 인증
 * - Rate Limiting
 */
@EnableTracingLogging
@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
