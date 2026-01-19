package com.example.chat.auth.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.chat.auth.server.core.domain.policy.AuthPolicy;
import com.example.chat.auth.server.core.domain.policy.DefaultAuthPolicy;

/**
 * Auth 서버 설정
 */
@Configuration
public class AuthConfiguration {

    /**
     * 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 기본 인증 정책
     */
    @Bean
    public AuthPolicy authPolicy() {
        return new DefaultAuthPolicy();
    }
}
