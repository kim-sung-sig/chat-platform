package com.example.chat.websocket.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.example.chat.websocket.presentation.handler.CustomRequestLoggingFilter;

import jakarta.servlet.Filter;

/**
 * WebSocket server 전용 필터 등록. HTTP 요청(예: REST health check 등)에 대해 MDC 주입을 보장합니다.
 */
@Configuration
public class LoggingConfig {

	@Bean
	public FilterRegistrationBean<Filter> requestLoggingFilter() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new CustomRequestLoggingFilter());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.addUrlPatterns("/*");
		return registration;
	}
}