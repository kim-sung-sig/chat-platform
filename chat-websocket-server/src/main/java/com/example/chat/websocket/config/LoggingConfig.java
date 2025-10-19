package com.example.chat.websocket.config;

import com.example.chat.websocket.logging.RequestLoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * WebSocket server 전용 필터 등록. HTTP 요청(예: REST health check 등)에 대해 MDC 주입을 보장합니다.
 */
@Configuration
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<Filter> requestLoggingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}