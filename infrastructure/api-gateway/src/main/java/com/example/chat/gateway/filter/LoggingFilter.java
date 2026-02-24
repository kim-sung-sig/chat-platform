package com.example.chat.gateway.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway 전역 Tracing 로깅 필터.
 * <p>
 * Micrometer Tracing 이 Reactor 컨텍스트를 통해 traceId / spanId 를 관리하며,
 * 이 필터는 요청/응답 로깅과 하위 서비스로의 traceId 헤더 전파를 담당한다.
 * </p>
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String TRACE_ID_RESPONSE_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public LoggingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String remoteAddress = (request.getRemoteAddress() != null)
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("[Gateway Request] {} {} from {}", request.getMethod(), request.getURI().getPath(), remoteAddress);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                exchange.getResponse().getHeaders().set(TRACE_ID_RESPONSE_HEADER, traceId);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Gateway Response] {} {} - status={} duration={}ms",
                    request.getMethod(),
                    request.getURI().getPath(),
                    exchange.getResponse().getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
