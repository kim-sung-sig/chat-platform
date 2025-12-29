package com.example.chat.modules.auth;

import com.example.chat.modules.auth.jwt.JwtTokenProvider;
import com.example.chat.modules.auth.jwt.TokenBlacklistService;
import com.example.chat.modules.auth.security.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan
public class AuthModuleConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenBlacklistService tokenBlacklistService() {
        return token -> false; // Default: Allow all
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider,
            TokenBlacklistService blacklistService) {
        return new JwtAuthenticationFilter(provider, blacklistService);
    }
}
