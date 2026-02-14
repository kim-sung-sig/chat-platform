package com.example.chat.message.logging;

import com.example.chat.common.logging.MdcUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Message-server 전용 요청 로깅 필터. MDC에 traceId를 주입하고 간단한 요청 로그를 남깁니다.
 * TODO: X-Trace-Id 헤더 우선 사용, 샘플링 및 민감정보 마스킹 추가
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private static final String HEADER_TRACE_ID = "X-Trace-Id";

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
		String traceId = request.getHeader(HEADER_TRACE_ID);
		boolean generated = false;
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
			generated = true;
		}

		try {
			MdcUtil.putTraceId(traceId);
			log.debug("[MSG] incoming request method={} uri={} traceId={} generated={}", request.getMethod(), request.getRequestURI(), traceId, generated);
			filterChain.doFilter(request, response);
		} finally {
			MdcUtil.removeTraceId();
		}
	}
}
