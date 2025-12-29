package com.example.chat.auth.server.service;

import com.example.chat.auth.server.domain.LoginRequest;
import com.example.chat.auth.server.domain.SignupRequest;
import com.example.chat.auth.server.domain.TokenResponse;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.modules.auth.jwt.JwtTokenProvider;
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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(request.getNickname(), request.getEmail(), encodedPassword);

        userRepository.save(user);
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId().getValue(), "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId().getValue());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // Example expiration
                .build();
    }

    /**
     * 토큰 갱신
     */
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateAndParseToken(refreshToken).getPayload().getSubject().isEmpty()) {
            String userId = jwtTokenProvider.getUserId(refreshToken);
            // Verify user exists and status

            String newAccessToken = jwtTokenProvider.createAccessToken(userId, "USER");
            // Rotate refresh token if needed (optional)

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Return same or new
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }
}
