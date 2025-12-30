package com.example.chat.message.config;

import com.example.chat.message.logging.RequestLoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * RequestLoggingFilter를 전역으로 등록합니다.
 * TODO: 운영환경에서는 샘플링 로직(예: 1%만 상세로그 남김) 또는 민감 데이터 마스킹을 추가하세요.
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