package com.example.chat.common.logging.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 기반 서비스 공통 Tracing 필터.
 * <p>
 * Micrometer Tracing 이 traceId / spanId 를 MDC 에 자동 주입한다.
 * 이 필터는 요청/응답 로깅과 응답 헤더(X-Trace-Id) 노출을 담당한다.
 * </p>
 */
public class TracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);
    private static final String TRACE_ID_RESPONSE_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public TracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.debug("[Request ] {} {}", method, uri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                response.setHeader(TRACE_ID_RESPONSE_HEADER, traceId);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Response] {} {} - status={} duration={}ms",
                    method, uri, response.getStatus(), duration);
        }
    }
}
