package com.example.chat.auth.jwt.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer

fun interface SecurityRequestCustomizer {
    fun customize(
            auth:
                    AuthorizeHttpRequestsConfigurer<
                            HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
    )
}
