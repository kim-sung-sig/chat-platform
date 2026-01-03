package com.example.chat.auth.server.domain.service;

import com.example.chat.auth.server.domain.repository.UserRepository;
import com.example.chat.auth.server.dto.request.LoginRequest;
import com.example.chat.auth.server.dto.request.SignupRequest;
import com.example.chat.auth.server.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		throw new UnsupportedOperationException("Not supported yet.");
		//if (userRepository.findByEmail(request.getEmail()).isPresent()) {
		//	throw new IllegalArgumentException("Email already exists");
		//}
		//
		//String encodedPassword = passwordEncoder.encode(request.getPassword());
		//User user = User..create(request.getNickname(), request.getEmail(), encodedPassword);
		//
		//userRepository.save(user);
	}

	/**
	 * 로그인
	 */
	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		throw new UnsupportedOperationException("Not supported yet.");
		//User user = userRepository.findByEmail(request.getEmail())
		//		.orElseThrow(() -> new IllegalArgumentException("User not found"));
		//
		//if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
		//	throw new IllegalArgumentException("Invalid password");
		//}
		//
		//String accessToken = jwtTokenProvider.createAccessToken(user.getId().getValue(), "USER");
		//String refreshToken = jwtTokenProvider.createRefreshToken(user.getId().getValue());
		//
		//return TokenResponse.builder()
		//		.accessToken(accessToken)
		//		.refreshToken(refreshToken)
		//		.tokenType("Bearer")
		//		.expiresIn(3600L)
		//		.build();
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
