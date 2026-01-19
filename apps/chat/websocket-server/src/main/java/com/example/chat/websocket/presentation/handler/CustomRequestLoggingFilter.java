package com.example.chat.websocket.presentation.handler;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.filter.OncePerRequestFilter;

import com.example.chat.common.logging.MdcUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket server 전용 요청 로깅 필터. MDC에 traceId를 주입하고 간단한 요청 로그를 남깁니다.
 */
@Slf4j
public class CustomRequestLoggingFilter extends OncePerRequestFilter {
	private static final String HEADER_TRACE_ID = "X-Trace-Id";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String traceId = request.getHeader(HEADER_TRACE_ID);
		boolean generated = false;
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
			generated = true;
		}

		try {
			MdcUtil.putTraceId(traceId);
			log.debug("[WS] incoming request method={} uri={} traceId={} generated={}", request.getMethod(),
					request.getRequestURI(), traceId, generated);
			filterChain.doFilter(request, response);
		} finally {
			MdcUtil.removeTraceId();
		}
	}
}
