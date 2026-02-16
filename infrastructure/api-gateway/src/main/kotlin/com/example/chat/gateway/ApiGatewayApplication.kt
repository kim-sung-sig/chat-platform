package com.example.chat.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Spring Cloud API Gateway
 *
 * 모든 마이크로서비스의 단일 진입점
 * - 라우팅
 * - 로드 밸런싱
 * - 인증/인가
 * - Rate Limiting
 */
@EnableDiscoveryClient
@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
	runApplication<ApiGatewayApplication>(*args)
}
