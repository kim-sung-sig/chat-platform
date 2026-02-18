package com.example.chat.system.config

import com.example.chat.domain.service.FriendshipDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServiceConfig {

	@Bean
	fun friendshipDomainService() = FriendshipDomainService()
}
