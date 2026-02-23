package com.example.chat.message.logging;

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
 * Message-server 전용 요청 로깅 필터
 *
 * 책임:
 * - MDC에 traceId 주입
 * - 요청 로그 기록
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // TraceId 조회 또는 생성
        String traceId = request.getHeader(HEADER_TRACE_ID);
        boolean generated = (traceId == null || traceId.isBlank());

        if (generated) {
            traceId = UUID.randomUUID().toString();
        }

        try {
            MdcUtil.putTraceId(traceId);
            log.debug("[MSG] incoming request method={} uri={} traceId={} generated={}",
                    request.getMethod(), request.getRequestURI(), traceId, generated);
            filterChain.doFilter(request, response);
        } finally {
            MdcUtil.removeTraceId();
        }
    }
}
