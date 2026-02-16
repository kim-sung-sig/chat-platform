package com.example.chat.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Global Logging Filter
 * 모든 요청/응답을 로깅합니다.
 */
@Component
class LoggingFilter : GlobalFilter, Ordered {

	private val logger = LoggerFactory.getLogger(javaClass)

	override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
		val request = exchange.request
		val startTime = System.currentTimeMillis()

		logger.info(
			"[Gateway Request] {} {} from {}",
			request.method,
			request.uri.path,
			request.remoteAddress?.address?.hostAddress ?: "unknown"
		)

		return chain.filter(exchange).then(
			Mono.fromRunnable {
				val duration = System.currentTimeMillis() - startTime
				logger.info(
					"[Gateway Response] {} {} - Status: {} - Duration: {}ms",
					request.method,
					request.uri.path,
					exchange.response.statusCode,
					duration
				)
			}
		)
	}

	override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
