package com.example.chat.auth.server.domain.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.auth.server.domain.model.User;
import com.example.chat.auth.server.domain.model.UserRole;
import com.example.chat.auth.server.domain.model.UserStatus;
import com.example.chat.auth.server.domain.repository.UserRepository;
import com.example.chat.auth.server.dto.request.SignupRequest;
import com.example.chat.auth.server.dto.response.TokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 회원 가입
	 */
	@Transactional
	public void signup(SignupRequest request) {
		// username = email로 사용
		if (userRepository.existsByUsername(request.email())) {
			throw new IllegalArgumentException("이미 가입된 이메일입니다.");
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		User user = User.builder()
				.username(request.email())
				.password(encodedPassword)
				.role(UserRole.ROLE_USER)
				.status(UserStatus.ENABLED)
				.name(request.nickname())
				.email(request.email())
				.build();

		userRepository.save(user);
	}

	/**
	 * 토큰 갱신
	 */
	public TokenResponse refreshToken(String refreshToken) {
		throw new UnsupportedOperationException("Not supported yet.");
		//
		//return TokenResponse.builder()
		//		.accessToken("new-access-token")
		//		.refreshToken(refreshToken)
		//		.tokenType("Bearer")
		//		.expiresIn(3600L)
		//		.build();
	}
}
