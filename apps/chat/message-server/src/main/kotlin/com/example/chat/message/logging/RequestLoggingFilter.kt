package com.example.chat.message.logging

import com.example.chat.common.logging.MdcUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

/**
 * Message-server 전용 요청 로깅 필터
 *
 * 책임:
 * - MDC에 traceId를 주입
 * - 요청 로그 기록
 *
 * TODO: X-Trace-Id 헤더 우선 사용, 샘플링 및 민감정보 마스킹 추가
 */
class RequestLoggingFilter : OncePerRequestFilter() {
	private val log = LoggerFactory.getLogger(javaClass)

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		// TraceId 조회 또는 생성
		var traceId = request.getHeader(HEADER_TRACE_ID)
		val generated = traceId.isNullOrBlank()

		if (generated) {
			traceId = UUID.randomUUID().toString()
		}

		try {
			MdcUtil.putTraceId(traceId!!)
			log.debug(
				"[MSG] incoming request method={} uri={} traceId={} generated={}",
				request.method, request.requestURI, traceId, generated
			)
			filterChain.doFilter(request, response)
		} finally {
			MdcUtil.removeTraceId()
		}
	}

	companion object {
		private const val HEADER_TRACE_ID = "X-Trace-Id"
	}
}
