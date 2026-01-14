package com.example.chat.auth.server.config.security;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.auth.server.domain.repository.UserRepository;
import com.example.chat.auth.server.domain.service.JwtTokenProvider;
import com.example.chat.auth.server.dto.request.LoginRequest;
import com.example.chat.auth.server.dto.response.TokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Transactional
	public TokenResponse login(LoginRequest request) {
		Authentication authentication;
		try {
			UsernamePasswordAuthenticationToken authToken =
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
			authentication = authenticationManager.authenticate(authToken);
		} catch (BadCredentialsException e) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		} catch (LockedException e) {
			throw new IllegalStateException("계정이 잠겨 있습니다. 관리자에게 문의하세요.");
		} catch (DisabledException e) {
			throw new IllegalStateException("비활성화된 계정입니다.");
		}

		AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
		Long userId = principal.getUserId();
		String role = principal.getRole();

		// 로그인 성공: 마지막 로그인 시간 갱신 (필요한 최소 필드만 로드)
		userRepository.findById(userId).ifPresent(user -> {
			user.setLastLoginAt(Instant.now());
			userRepository.save(user);
		});

		String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(userId), role);
		String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(userId));

		return TokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.expiresIn(3600L)
				.build();
	}
}
