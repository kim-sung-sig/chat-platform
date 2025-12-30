package com.example.chat.websocket.logging;

import com.example.chat.common.logging.MdcUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * WebSocket server 전용 요청 로깅 필터. MDC에 traceId를 주입하고 간단한 요청 로그를 남깁니다.
 * TODO: WebSocket 연결 핸들(Handshake)에 MDC 주입을 추가할 것
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER_TRACE_ID);
        boolean generated = false;
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            generated = true;
        }

        try {
            MdcUtil.putTraceId(traceId);
            logger.debug("[WS] incoming request method={} uri={} traceId={} generated={}", request.getMethod(), request.getRequestURI(), traceId, generated);
            filterChain.doFilter(request, response);
        } finally {
            MdcUtil.removeTraceId();
        }
    }
}