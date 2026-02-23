package com.example.chat.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Gateway 라우트 설정
 *
 * [Auth Server]    /api/auth/**           -> auth-server /api/v1/auth/**
 *                  /api/{ver}/auth/**     -> auth-server (그대로)
 * [Message]        /api/messages/**       -> chat-message-server /api/v1/messages/**
 *                  /api/{ver}/messages/** -> 그대로
 * [System]         /api/channels/**       -> chat-system-server
 *                  /api/schedules/**      -> chat-system-server
 * [WebSocket]      /ws/**                 -> chat-websocket-server
 */
@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth Server - 버전 없음
                .route("auth-server-no-version", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://AUTH-SERVER"))

                // Auth Server - 버전 있음
                .route("auth-server-versioned", r -> r
                        .path("/api/*/auth/**")
                        .uri("lb://AUTH-SERVER"))

                // Message Server - 버전 없음
                .route("chat-message-server-no-version", r -> r
                        .path("/api/messages/**")
                        .filters(f -> f.rewritePath("/api/messages/(?<segment>.*)", "/api/v1/messages/${segment}"))
                        .uri("lb://CHAT-MESSAGE-SERVER"))

                // Message Server - 버전 있음
                .route("chat-message-server-versioned", r -> r
                        .path("/api/*/messages/**")
                        .uri("lb://CHAT-MESSAGE-SERVER"))

                // System Server - channels 버전 없음
                .route("chat-system-server-channels-no-version", r -> r
                        .path("/api/channels/**")
                        .filters(f -> f.rewritePath("/api/channels/(?<segment>.*)", "/api/v1/channels/${segment}"))
                        .uri("lb://CHAT-SYSTEM-SERVER"))

                // System Server - channels 버전 있음
                .route("chat-system-server-channels-versioned", r -> r
                        .path("/api/*/channels/**")
                        .uri("lb://CHAT-SYSTEM-SERVER"))

                // System Server - schedules 버전 없음
                .route("chat-system-server-schedules-no-version", r -> r
                        .path("/api/schedules/**")
                        .filters(f -> f.rewritePath("/api/schedules/(?<segment>.*)", "/api/v1/schedules/${segment}"))
                        .uri("lb://CHAT-SYSTEM-SERVER"))

                // System Server - schedules 버전 있음
                .route("chat-system-server-schedules-versioned", r -> r
                        .path("/api/*/schedules/**")
                        .uri("lb://CHAT-SYSTEM-SERVER"))

                // WebSocket Server
                .route("chat-websocket-server", r -> r
                        .path("/ws/**")
                        .uri("lb:ws://CHAT-WEBSOCKET-SERVER"))

                .build();
    }
}
