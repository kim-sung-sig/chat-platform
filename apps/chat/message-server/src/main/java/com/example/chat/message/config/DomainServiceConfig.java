package com.example.chat.message.config;

import com.example.chat.domain.service.MessageDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Service 빈 설정
 *
 * Domain Service는 상태가 없으므로 싱글톤으로 관리
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public MessageDomainService messageDomainService() {
        return new MessageDomainService();
    }
}
