package com.example.chat.auth.jwt.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationConverterSupport implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Collection<String> roles = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
				.orElse(List.of());

		return AuthorityUtils.createAuthorityList(roles);
	}
}

