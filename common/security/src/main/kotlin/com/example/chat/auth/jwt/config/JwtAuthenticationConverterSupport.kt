package com.example.chat.auth.jwt.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.jwt.Jwt

class JwtAuthenticationConverterSupport : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val roles = jwt.getClaimAsStringList("roles") ?: emptyList()
        return AuthorityUtils.createAuthorityList(roles)
    }
}
