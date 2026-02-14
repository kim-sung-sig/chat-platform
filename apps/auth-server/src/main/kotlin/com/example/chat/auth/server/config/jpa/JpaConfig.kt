
package com.example.chat.auth.server.config.jpa

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal
import java.util.*

@Configuration
@EnableJpaAuditing
class JpaConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware<String> {
            Optional.ofNullable(SecurityContextHolder.getContext().authentication)
                .filter { it.isAuthenticated }
                .map { it.principal }
                .filter { it is Principal }
                .map { (it as Principal).name }
        }
    }
}
