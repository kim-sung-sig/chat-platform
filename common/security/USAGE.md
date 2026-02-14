# Auth-JWT 모듈 사용 예시 (Kotlin)

## 1. 다른 서버 모듈에서 사용하기

### build.gradle

```gradle
dependencies {
    implementation project(':common:security')
}
```

### application.yml

```yaml
security:
  jwt:
    issuer-uri: http://localhost:9000
    jwk-set-uri: http://localhost:9000/.well-known/jwks.json
```

### Application.kt (@EnableJwtSecurity 필수!)

```kotlin
@SpringBootApplication
@EnableJwtSecurity // 필수! 이 어노테이션이 트리거 역할
class MessageServerApplication

fun main(args: Array<String>) {
    runApplication<MessageServerApplication>(*args)
}
```

## 2. 커스터마이징 예시

### 경로별 권한 설정

```kotlin
@Configuration
@EnableJwtSecurity
class SecurityConfig {
    private val swaggerPaths = arrayOf(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    )

    private val healthCheckPaths = arrayOf(
        "/actuator/health",
        "/actuator/info"
    )

    @Bean
    fun securityRequestCustomizer(): SecurityRequestCustomizer {
        return SecurityRequestCustomizer { auth ->
            auth.requestMatchers(*swaggerPaths).permitAll()
            auth.requestMatchers(*healthCheckPaths).permitAll()
            auth.requestMatchers("/api/messages/health").permitAll()
        }
    }
}
```

## 3. Controller에서 인증 정보 사용

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {
    // 현재 인증된 사용자 정보 가져오기
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserInfo> {
        val userId = jwt.subject
        val roles = jwt.getClaimAsStringList("roles") ?: emptyList()

        return ResponseEntity.ok(UserInfo(userId, roles))
    }

    // SecurityContext에서 가져오기
    @GetMapping("/profile")
    fun getProfile(): ResponseEntity<String> {
        val auth = SecurityContextHolder.getContext().authentication
        val username = auth.name

        return ResponseEntity.ok("Profile of $username")
    }

    // 특정 권한 체크
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        // Admin만 접근 가능
        return ResponseEntity.noContent().build()
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

# 2. 발급받은 토큰으로 API 호출
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 5. 에러 응답 예시

### 토큰 없음/유효하지 않음 (401)
```json
{
  "code": "AUTH_001",
  "message": "유효하지 않은 토큰",
  "status": 401
}
```

### 만료된 토큰 (401)
```json
{
  "code": "AUTH_002",
  "message": "만료된 토큰",
  "status": 401
}
```

### 권한 부족 (403)
```json
{
  "code": "AUTH_003",
  "message": "권한이 부족합니다",
  "status": 403
}
```
