package com.example.chat.auth.server.controller;

import com.example.chat.auth.server.domain.service.AuthService;
import com.example.chat.auth.server.domain.service.JwtTokenProvider;
import com.example.chat.auth.server.dto.request.LoginRequest;
import com.example.chat.auth.server.dto.request.SignupRequest;
import com.example.chat.auth.server.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthApi {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/signup")
	public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
		authService.signup(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(@RequestBody String refreshToken) {
		return ResponseEntity.ok(authService.refreshToken(refreshToken));
	}

	@GetMapping("/test")
	public ResponseEntity<TokenResponse> test() {
		String accessToken = jwtTokenProvider.createAccessToken("TEST", "USER");
		String refreshToken = jwtTokenProvider.createRefreshToken("TEST");

		var token = TokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.expiresIn(3600L)
				.build();

		return ResponseEntity.ok(token);
	}

}
