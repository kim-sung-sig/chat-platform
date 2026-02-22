package com.example.chat.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.chat.domain.service.FriendshipDomainService;

@Configuration
public class DomainServiceConfig {

    @Bean
    public FriendshipDomainService friendshipDomainService() {
        return new FriendshipDomainService();
    }
}
