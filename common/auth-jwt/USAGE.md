# Auth-JWT 모듈 사용 예시

## 1. 다른 서버 모듈에서 사용하기

### build.gradle

```groovy
dependencies {
	implementation project(':common:auth-jwt')
}
```

### application.properties

```properties
# Authorization Server 주소
security.jwt.issuer-uri=http://localhost:9000
security.jwt.jwk-set-uri=http://localhost:9000/.well-known/jwks.json
# 또는 환경변수
# JWT_ISSUER_URI=http://localhost:9000
# JWT_JWK_SET_URI=http://localhost:9000/.well-known/jwks.json
```

### Application.java (@EnableJwtSecurity 필수!)

```java
@SpringBootApplication
@EnableJwtSecurity  // 필수! 이 어노테이션이 트리거 역할
public class MessageServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageServerApplication.class, args);
    }
}
```

## 2. 커스터마이징 예시

### 경로별 권한 설정

```java

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain jwtSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(auth -> auth
						// Public 경로
						.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll()

						// Admin 전용
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// User 권한 필요
						.requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")

						// 그 외 인증 필요
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtAuthenticationConverter(jwtAuthenticationConverter)
						)
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				);

		return http.build();
	}
}
```

### 커스텀 권한 변환 로직

```java

@Configuration
public class CustomJwtConfig {

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		// 커스텀 권한 변환 로직
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			List<String> roles = jwt.getClaimAsStringList("authorities");

			if (roles == null) {
				return Collections.emptyList();
			}

			return roles.stream()
					.map(role -> "ROLE_" + role.toUpperCase())
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
		});

		// Principal 이름을 user_id claim으로 설정
		converter.setPrincipalClaimName("user_id");

		return converter;
	}
}
```

## 3. Controller에서 인증 정보 사용

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // 현재 인증된 사용자 정보 가져오기
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        
        UserInfo userInfo = new UserInfo(userId, roles);
        return ResponseEntity.ok(userInfo);
    }
    
    // SecurityContext에서 가져오기
    @GetMapping("/profile")
    public ResponseEntity<String> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return ResponseEntity.ok("Profile of " + username);
    }
    
    // 특정 권한 체크
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Admin만 접근 가능
        return ResponseEntity.noContent().build();
    }
}
```

## 4. 테스트

### JWT 토큰으로 API 호출

```bash
# 1. Authorization Server에서 토큰 발급
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=client&client_secret=secret"

# 응답
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}

# 2. 발급받은 토큰으로 API 호출
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# 성공 응답
{
  "userId": "user123",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}

# 3. 토큰 없이 호출 (실패)
curl http://localhost:8080/api/users/me

# 에러 응답
{
  "code": "AUTH_001",
  "message": "유효하지 않은 토큰",
  "path": "/api/users/me"
}
```

## 5. 에러 응답 예시

### 토큰 없음

```json
{
  "code": "AUTH_001",
  "message": "유효하지 않은 토큰",
  "path": "/api/protected"
}
```

### 만료된 토큰

```json
{
  "code": "AUTH_002",
  "message": "만료된 토큰",
  "path": "/api/protected"
}
```

### 권한 부족

```json
{
  "code": "AUTH_003",
  "message": "권한이 부족합니다",
  "path": "/api/admin/users"
}
```

## 6. 문제 해결

### HttpSecurity 빈을 찾을 수 없다는 에러

→ Spring Security 의존성이 정상적으로 추가되었는지 확인
→ `@EnableWebSecurity` 어노테이션 충돌 확인

### JwtDecoder 빈이 여러 개라는 에러

→ `@ConditionalOnMissingBean`이 동작하지 않는 경우
→ 커스텀 JwtDecoder를 정의했다면 Auto-Configuration의 빈 이름을 변경

### Issuer URI 연결 실패

→ Authorization Server가 실행 중인지 확인
→ `security.jwt.issuer-uri` 설정이 올바른지 확인
→ `/.well-known/openid-configuration` 엔드포인트가 정상인지 확인

```bash
curl http://localhost:9000/.well-known/openid-configuration
```

