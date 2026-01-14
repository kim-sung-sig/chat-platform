# Auth 서버 인증/인가 기능 확장 계획

## 1. 회원가입 기능 명확화 및 보완
- User, SignupRequest, UserRepository, AuthService 점검 및 보완
- 이메일/비밀번호 정책, 중복 체크, 예외 처리

## 2. Username/Password 로그인 강화
- LoginRequest, AuthService 내 인증 처리 강화
- 실패/잠금 정책, 예외 처리

## 3. JWT 발급/재발급 구조 개선
- JwtTokenProvider, TokenResponse 구조 개선
- RefreshToken 관리 및 재발급 API

## 4. OAuth2 로그인 도입
- 외부 인증 연동 구조 설계
- OAuth2 config/controller/service 추가

## 5. 보안 정책 및 테스트
- SecurityConfig, JwtConfig 재점검
- 단위/통합 테스트 작성

---
- 2026-01-14 작성
- 담당: (작성자 이름)
