# Auth-JWT 모듈

JWT 인증 필터를 제공하는 Spring Boot Auto-Configuration 모듈입니다.

## 특징

- 🔐 **OAuth2 Resource Server 기반 JWT 인증**
- 🚀 **Spring Boot Auto-Configuration 지원**
- 🛡️ **자동 에러 처리 및 응답 변환**
- ⚙️ **간편한 설정 및 커스터마이징**

## 사용법

### 1. 의존성 추가

다른 모듈의 `build.gradle`에 추가:

```groovy
dependencies {
	implementation project(':common:auth-jwt')
}
```

### 2. @EnableJwtSecurity 어노테이션 추가 (필수!)

**중요**: 이 모듈은 `@EnableJwtSecurity` 어노테이션을 추가해야만 활성화됩니다.

```java
@SpringBootApplication
@EnableJwtSecurity  // 필수! 이 어노테이션이 트리거 역할
public class YourApplication {
	public static void main(String[] args) {
		SpringApplication.run(YourApplication.class, args);
	}
}
```

### 3. 설정

`application.properties` 또는 `application.yml`에 JWT 설정 추가:

```properties
# JWT Issuer URI (Authorization Server 주소)
security.jwt.issuer-uri=http://localhost:8080
# JWK Set URI (공개키 엔드포인트)
security.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json
```

환경변수로 설정할 수도 있습니다:

```bash
JWT_ISSUER_URI=http://localhost:8080
JWT_JWK_SET_URI=http://localhost:8080/.well-known/jwks.json
```

## 제공 기능

### 1. 자동 JWT 인증 필터

- `/auth/**`, `/health`, `/actuator/**` 경로는 인증 없이 접근 가능
- 나머지 모든 경로는 JWT 인증 필요
- Stateless 세션 관리

### 2. JWT 권한 변환

JWT의 `roles` claim을 Spring Security의 `GrantedAuthority`로 자동 변환

```json
{
  "sub": "user123",
  "roles": [
    "ROLE_USER",
    "ROLE_ADMIN"
  ],
  "exp": 1234567890
}
```

### 3. 에러 처리

JWT 인증 실패 시 자동으로 JSON 에러 응답 반환:

```json
{
  "code": "AUTH_001",
  "message": "유효하지 않은 토큰",
  "path": "/api/users"
}
```

**에러 코드:**

- `AUTH_001`: 유효하지 않은 토큰
- `AUTH_002`: 만료된 토큰
- `AUTH_003`: 권한이 부족합니다

## 커스터마이징

### 1. SecurityFilterChain 커스터마이징

기본 설정을 오버라이드하려면:

```java

@Configuration
public class CustomSecurityConfig {

	@Bean
	public SecurityFilterChain jwtSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/public/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtAuthenticationConverter(jwtAuthenticationConverter)
						)
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				);

		return http.build();
	}
}
```

### 2. JwtAuthenticationConverter 커스터마이징

```java

@Configuration
public class CustomJwtConfig {

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		// 커스텀 권한 변환기
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			// 커스텀 로직
			return AuthorityUtils.createAuthorityList("ROLE_CUSTOM");
		});

		// Principal name 설정
		converter.setPrincipalClaimName("user_id");

		return converter;
	}
}
```

### 3. 에러 응답 커스터마이징

`JwtExceptionHandler`를 상속하여 커스터마이징:

```java

@RestControllerAdvice
public class CustomJwtExceptionHandler extends JwtExceptionHandler {

	@Override
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
		// 커스텀 에러 응답
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("error", ex.getErrorCode().getCode());
		errorResponse.put("description", ex.getErrorCode().getMessage());
		errorResponse.put("timestamp", System.currentTimeMillis());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}
}
```

## 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                     Client Request                       │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         JwtSecurityFilterChain (자동 구성됨)            │
│  - CSRF 비활성화                                         │
│  - Stateless 세션                                        │
│  - OAuth2 Resource Server (JWT)                          │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│           JwtDecoder (NimbusJwtDecoder)                  │
│  - Issuer URI로부터 JWT 검증                             │
│  - JWK Set 자동 갱신                                     │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│      JwtAuthenticationConverter                          │
│  - JWT claims → GrantedAuthority 변환                   │
│  - roles claim 처리                                      │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         인증 성공 → SecurityContext 저장                 │
└─────────────────────────────────────────────────────────┘

                 (인증 실패 시)
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│      JwtAuthenticationEntryPoint                         │
│  - 401 Unauthorized 응답                                 │
│  - JSON 에러 응답 생성                                   │
└─────────────────────────────────────────────────────────┘
```

## 의존성

- Spring Boot 3.5.6
- Spring Security 6.x
- Spring Security OAuth2 Resource Server
- Jackson (JSON 처리)

## 주의사항

1. **Authorization Server 필수**: 이 모듈은 Resource Server이므로, 별도의 Authorization Server가 필요합니다.
2. **Issuer URI 설정**: `security.jwt.issuer-uri`는 반드시 실제 Authorization Server 주소로 설정해야 합니다.
3. **JWK Set 엔드포인트**: Authorization Server는 `/.well-known/jwks.json` 엔드포인트를 제공해야 합니다.

## 라이선스

이 프로젝트는 내부 모듈입니다.

