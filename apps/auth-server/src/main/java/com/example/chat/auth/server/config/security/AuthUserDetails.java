package com.example.chat.auth.server.config.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.chat.auth.server.domain.model.User;
import com.example.chat.auth.server.domain.model.UserStatus;

public class AuthUserDetails implements UserDetails {

	private final Long userId;
	private final String username;
	private final String password;
	private final String role;
	private final UserStatus status;

	public AuthUserDetails(User user) {
		this.userId = user.getId();
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.role = user.getRole().name();
		this.status = user.getStatus();
	}

	public Long getUserId() {
		return userId;
	}

	public String getRole() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return status != UserStatus.EXPIRED;
	}

	@Override
	public boolean isAccountNonLocked() {
		return status != UserStatus.LOCKED;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return status == UserStatus.ENABLED;
	}
}
