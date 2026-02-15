package com.example.chat.message.config

import com.example.chat.domain.service.MessageDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Domain Service 빈 설정
 *
 * Domain Service는 상태를 갖지 않으므로 싱글톤으로 관리
 */
@Configuration
class DomainServiceConfig {

	@Bean
	fun messageDomainService(): MessageDomainService {
		return MessageDomainService()
	}
}
