# Auth Server - JWT 발급 서버

## 개요
OAuth2 Resource Server가 사용할 수 있는 JWT 토큰을 발급하는 인증 서버입니다.

## 주요 기능
- ✅ **회원 가입 / 로그인**: 사용자 인증
- ✅ **JWT 토큰 발급**: Access Token + Refresh Token
- ✅ **JWK Set 엔드포인트**: `/.well-known/jwks.json` 제공
- ✅ **RSA 서명**: Nimbus JOSE JWT 라이브러리 사용

## 서버 정보
- **포트**: 18080
- **Issuer**: http://localhost:18080
- **JWK Set URL**: http://localhost:18080/.well-known/jwks.json

## API 엔드포인트

### 1. 회원 가입
```http
POST /auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "User"
}
```

### 2. 로그인
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 3. 토큰 갱신
```http
POST /auth/refresh
Content-Type: application/json

"<refresh_token>"
```

### 4. JWK Set (공개키)
```http
GET /.well-known/jwks.json
```

**응답:**
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "kid": "uuid-key-id",
      "n": "..."
    }
  ]
}
```

## 아키텍처

### JWT 발급 흐름
```
1. 사용자 로그인
   ↓
2. 비밀번호 검증 (BCrypt)
   ↓
3. JWT 생성 (RSA 개인키로 서명)
   - Access Token (1시간)
   - Refresh Token (24시간)
   ↓
4. 클라이언트에 토큰 반환
```

### Resource Server 검증 흐름
```
1. Resource Server가 JWT 수신
   ↓
2. /.well-known/jwks.json에서 공개키 가져오기
   ↓
3. RSA 공개키로 JWT 서명 검증
   ↓
4. 토큰이 유효하면 요청 처리
```

## 기술 스택

### JWT 구현
- **Spring Security OAuth2 JOSE**: JWT 인코딩/디코딩
- **Nimbus JOSE + JWT**: EC 키쌍 및 JWK Set 관리
- **알고리즘**: ES256 (ECDSA with P-256 and SHA-256)
- **키 크기**: 256-bit (RSA 2048-bit 대비 8배 작음)
- **성능**: RSA 대비 서명 5~10배 빠름

### JWT Claims
```json
{
  "alg": "ES256",
  "kid": "generated-key-id"
}
```
```json
{
  "iss": "http://localhost:18080",
  "sub": "user-id",
  "iat": 1234567890,
  "exp": 1234571490,
  "scope": "USER",
  "type": "access"
}
```

## 설정

### application.properties
```properties
server.port=18080

jwt.issuer=http://localhost:18080
jwt.access-token-expiration=3600
jwt.refresh-token-expiration=86400
```

### 환경변수로 오버라이드
```bash
JWT_ISSUER=https://auth.example.com
JWT_ACCESS_TOKEN_EXPIRATION=7200
JWT_REFRESH_TOKEN_EXPIRATION=172800
```

## 보안 고려사항

### 현재 구현 (개발용)
- ⚠️ EC 키쌍이 애플리케이션 시작 시 생성됨
- ⚠️ 재시작 시 키가 변경되어 기존 토큰이 무효화됨

### 운영 환경 권장사항
1. **키 영속성**: 데이터베이스 또는 파일 시스템에 키 저장
2. **키 로테이션**: 주기적으로 키 교체
3. **HTTPS 사용**: 토큰 전송 시 암호화
4. **Refresh Token 저장**: Redis에 저장하여 무효화 가능하도록 구성

### 왜 ECDSA (ES256)를 선택했나?

| 비교 항목 | RSA-2048 (RS256) | ECDSA P-256 (ES256) |
|----------|------------------|---------------------|
| 키 크기 | 2048-bit | **256-bit** |
| 보안 수준 | 112-bit | **128-bit** |
| 서명 속도 | 기준 | **5~10배 빠름** |
| 검증 속도 | 빠름 | **RSA와 유사** |
| JWT 크기 | 큼 | **작음 (네트워크 절약)** |
| 표준 지원 | 광범위 | 광범위 (RFC 7518) |

**결론**: ECDSA가 더 작고, 빠르고, 안전합니다!

## 연동 방법

### Message Server 설정
```properties
security.jwt.issuer-uri=http://localhost:18080
security.jwt.jwk-set-uri=http://localhost:18080/.well-known/jwks.json
```

### Message Server에서 JWT 사용
```java
@Configuration
@EnableJwtSecurity
public class SecurityConfig {
    // JWT 보안이 자동으로 활성화됩니다
}
```

### API 호출
```bash
# 1. 로그인하여 토큰 받기
TOKEN=$(curl -X POST http://localhost:18080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | jq -r '.accessToken')

# 2. Message Server API 호출
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:20001/api/messages
```

## 실행 방법

### 1. 데이터베이스 시작
```bash
docker-compose up -d postgres redis
```

### 2. Auth Server 실행
```bash
./gradlew :apps:auth-server:bootRun
```

### 3. 확인
```bash
# Health Check
curl http://localhost:18080/health

# JWK Set 확인
curl http://localhost:18080/.well-known/jwks.json
```

## 트러블슈팅

### "EC key generation failed"
- Java Cryptography Extension (JCE) 확인
- JDK 버전 확인 (Java 21 사용 중)
- P-256 곡선 지원 확인

### "Public key not found"
- /.well-known/jwks.json 엔드포인트 확인
- Resource Server의 issuer-uri 설정 확인

### "Token validation failed"
- 토큰 만료 시간 확인
- Issuer URL이 일치하는지 확인
- 알고리즘이 ES256인지 확인 (RS256 아님)

