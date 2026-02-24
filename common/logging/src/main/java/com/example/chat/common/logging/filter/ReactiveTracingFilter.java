package com.example.chat.common.logging.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFlux(Reactive) 기반 서비스 공통 Tracing 필터.
 * <p>
 * Gateway 또는 WebFlux 기반 서비스에서 사용한다.
 * Micrometer Tracing 이 Reactor Context 를 통해 traceId / spanId 를 관리하며,
 * 이 필터는 응답 헤더(X-Trace-Id) 노출과 요청/응답 로깅을 담당한다.
 * </p>
 */
public class ReactiveTracingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveTracingFilter.class);
    private static final String TRACE_ID_RESPONSE_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public ReactiveTracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        log.debug("[Request ] {} {}", method, path);

        return chain.filter(exchange).doFinally(signal -> {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                exchange.getResponse().getHeaders().set(TRACE_ID_RESPONSE_HEADER, traceId);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Response] {} {} - status={} duration={}ms",
                    method, path,
                    exchange.getResponse().getStatusCode(),
                    duration);
        });
    }
}
