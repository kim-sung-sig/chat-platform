package com.example.chat.auth.server.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

	// audit의 createBy, lastModifiedBy 자동 설정을 위한 빈 등록
	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getPrincipal)
				.filter(Principal.class::isInstance)
				.map(Principal.class::cast)
				.map(Principal::getName);
	}
}