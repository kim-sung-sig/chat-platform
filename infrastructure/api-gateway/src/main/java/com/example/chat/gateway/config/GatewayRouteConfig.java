package com.example.chat.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Gateway 라우트 설정
 *
 * [Auth Server]   /api/auth/**          -> auth-server
 * [Chat Server]   /api/messages/**      -> chat-server (message-server + system-server 통합)
 *                 /api/channels/**      -> chat-server
 *                 /api/schedules/**     -> chat-server
 *                 /api/friendships/**   -> chat-server
 * [WebSocket]     /ws/**               -> chat-websocket-server
 *
 * Eureka 서비스 ID: CHAT-SERVER (spring.application.name=chat-server)
 */
@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // =============================================
                // Auth Server
                // =============================================
                .route("auth-server-no-version", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://AUTH-SERVER"))

                .route("auth-server-versioned", r -> r
                        .path("/api/*/auth/**")
                        .uri("lb://AUTH-SERVER"))

                // =============================================
                // Chat Server - Messages
                // =============================================
                .route("chat-server-messages-no-version", r -> r
                        .path("/api/messages/**")
                        .filters(f -> f.rewritePath("/api/messages/(?<segment>.*)", "/api/messages/${segment}"))
                        .uri("lb://CHAT-SERVER"))

                .route("chat-server-messages-versioned", r -> r
                        .path("/api/*/messages/**")
                        .uri("lb://CHAT-SERVER"))

                // =============================================
                // Chat Server - Channels
                // =============================================
                .route("chat-server-channels-no-version", r -> r
                        .path("/api/channels/**")
                        .filters(f -> f.rewritePath("/api/channels/(?<segment>.*)", "/api/v1/channels/${segment}"))
                        .uri("lb://CHAT-SERVER"))

                .route("chat-server-channels-versioned", r -> r
                        .path("/api/*/channels/**")
                        .uri("lb://CHAT-SERVER"))

                // =============================================
                // Chat Server - Friendships
                // =============================================
                .route("chat-server-friendships-no-version", r -> r
                        .path("/api/friendships/**")
                        .filters(f -> f.rewritePath("/api/friendships/(?<segment>.*)", "/api/v1/friendships/${segment}"))
                        .uri("lb://CHAT-SERVER"))

                .route("chat-server-friendships-versioned", r -> r
                        .path("/api/*/friendships/**")
                        .uri("lb://CHAT-SERVER"))

                // =============================================
                // Chat Server - Schedules
                // =============================================
                .route("chat-server-schedules-no-version", r -> r
                        .path("/api/schedules/**")
                        .filters(f -> f.rewritePath("/api/schedules/(?<segment>.*)", "/api/v1/schedules/${segment}"))
                        .uri("lb://CHAT-SERVER"))

                .route("chat-server-schedules-versioned", r -> r
                        .path("/api/*/schedules/**")
                        .uri("lb://CHAT-SERVER"))

                // =============================================
                // WebSocket Server
                // =============================================
                .route("chat-websocket-server", r -> r
                        .path("/ws/**")
                        .uri("lb:ws://CHAT-WEBSOCKET-SERVER"))

                .build();
    }
}
