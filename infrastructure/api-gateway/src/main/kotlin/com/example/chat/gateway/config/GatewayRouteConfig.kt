package com.example.chat.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// API Gateway Route Configuration
//
// [Auth Server]   /api/auth/**       -> auth-server /api/v1/auth/**   (버전 없으면 v1 주입)
//                 /api/v1/auth/**    -> auth-server /api/v1/auth/**   (그대로 전달)
//                 /api/{ver}/auth/** -> auth-server /api/{ver}/auth/** (그대로 전달)
//
// [Message]       /api/messages/**      -> chat-message-server /api/v1/messages/**
//                 /api/{ver}/messages/** -> 그대로 전달
//
// [System]        /api/channels|schedules/** 동일 방식
@Configuration
class GatewayRouteConfig {

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator =
        builder.routes()

            // Auth Server - 버전 없는 경우: /api/auth/** -> /api/v1/auth/**
            .route("auth-server-no-version") { r ->
                r.path("/api/auth/**")
                    .filters { f ->
                        f.rewritePath(
                            "/api/auth/(?<segment>.*)",
                            "/api/v1/auth/\${segment}"
                        )
                    }
                    .uri("lb://AUTH-SERVER")
            }

            // Auth Server - 버전 있는 경우: /api/v1/auth/** -> 그대로 전달
            .route("auth-server-versioned") { r ->
                r.path("/api/*/auth/**")
                    .uri("lb://AUTH-SERVER")
            }

            // Message Server - 버전 없는 경우
            .route("chat-message-server-no-version") { r ->
                r.path("/api/messages/**")
                    .filters { f ->
                        f.rewritePath(
                            "/api/messages/(?<segment>.*)",
                            "/api/v1/messages/\${segment}"
                        )
                    }
                    .uri("lb://CHAT-MESSAGE-SERVER")
            }

            // Message Server - 버전 있는 경우
            .route("chat-message-server-versioned") { r ->
                r.path("/api/*/messages/**")
                    .uri("lb://CHAT-MESSAGE-SERVER")
            }

            // System Server - channels (버전 없는 경우)
            .route("chat-system-server-channels-no-version") { r ->
                r.path("/api/channels/**")
                    .filters { f ->
                        f.rewritePath(
                            "/api/channels/(?<segment>.*)",
                            "/api/v1/channels/\${segment}"
                        )
                    }
                    .uri("lb://CHAT-SYSTEM-SERVER")
            }

            // System Server - channels (버전 있는 경우)
            .route("chat-system-server-channels-versioned") { r ->
                r.path("/api/*/channels/**")
                    .uri("lb://CHAT-SYSTEM-SERVER")
            }

            // System Server - schedules (버전 없는 경우)
            .route("chat-system-server-schedules-no-version") { r ->
                r.path("/api/schedules/**")
                    .filters { f ->
                        f.rewritePath(
                            "/api/schedules/(?<segment>.*)",
                            "/api/v1/schedules/\${segment}"
                        )
                    }
                    .uri("lb://CHAT-SYSTEM-SERVER")
            }

            // System Server - schedules (버전 있는 경우)
            .route("chat-system-server-schedules-versioned") { r ->
                r.path("/api/*/schedules/**")
                    .uri("lb://CHAT-SYSTEM-SERVER")
            }

            // WebSocket Server
            .route("chat-websocket-server") { r ->
                r.path("/ws/**")
                    .uri("lb:ws://CHAT-WEBSOCKET-SERVER")
            }

            .build()
}
