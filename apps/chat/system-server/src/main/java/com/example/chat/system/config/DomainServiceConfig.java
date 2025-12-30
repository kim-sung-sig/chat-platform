package com.example.chat.system.config;

import com.example.chat.domain.service.ChannelDomainService;
import com.example.chat.domain.service.MessageDomainService;
import com.example.chat.domain.service.ScheduleDomainService;
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

    @Bean
    public ScheduleDomainService scheduleDomainService() {
        return new ScheduleDomainService();
    }

    @Bean
    public ChannelDomainService channelDomainService() {
        return new ChannelDomainService();
    }
}
