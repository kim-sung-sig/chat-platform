package com.example.chat.message.config;

import com.example.chat.domain.service.MessageDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Service 빈 설정
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public MessageDomainService messageDomainService() {
        return new MessageDomainService();
    }
}
