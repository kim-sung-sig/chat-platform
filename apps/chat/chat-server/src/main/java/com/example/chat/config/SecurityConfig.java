package com.example.chat.config;
import com.example.chat.auth.jwt.annotation.EnableJwtSecurity;
import com.example.chat.auth.jwt.config.SecurityRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@EnableJwtSecurity
public class SecurityConfig {
    @Bean
    public SecurityRequestCustomizer securityRequestCustomizer() {
        return auth -> {
            auth.requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll();
            auth.requestMatchers("/actuator/health","/actuator/info","/api/messages/health").permitAll();
        };
    }
}