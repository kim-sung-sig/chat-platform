# common/web - CLAUDE.md

Shared library for **REST API configuration & security filters**.

## Overview

**Purpose**: Common web setup (error handlers, security filters, CORS, validation)  
**Used by**: auth-server, chat, push-service  
**Stack**: Spring Web MVC, Spring Security, validation annotations

## Package Structure

```
common:web/src/main/java/com/example/chat/common/
├── web/
│   ├── config/
│   │   ├── WebConfiguration.java    # CORS, content negotiation
│   │   ├── SecurityConfiguration.java # JWT auth, security filters
│   │   └── ValidationConfiguration.java
│   ├── error/
│   │   ├── GlobalExceptionHandler.java (@ControllerAdvice)
│   │   ├── ErrorResponse.java        # Standardized error DTO
│   │   └── ErrorMapper.java          # Domain → HTTP status
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CorsFilter.java
│   │   └── SecurityHeaderFilter.java
│   ├── dto/
│   │   └── CommonDtos.java           # PaginationRequest, etc.
│   └── validation/
│       └── CustomValidators.java
└── config/
    └── WebAutoConfiguration.java
```

## Key Components

### 1. Global Exception Handler
- Catches all exceptions, maps to HTTP responses
- Domain exceptions → 400/401/403/422
- Technical exceptions → 500
- Logs full stack trace (except validation errors)

### 2. JWT Authentication Filter
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates signature, expiration
- Sets `SecurityContext` for downstream use
- Applied to protected endpoints only

### 3. CORS Configuration
- Allowed origins: configured per environment
- Allowed methods: GET, POST, PUT, DELETE
- Credentials: allowed for same-site requests

### 4. Security Headers
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000` (HTTPS only)

### 5. Validation
- `@Valid` on controller parameters triggers validation
- Custom validators for domain constraints
- Returns 400 with detailed field errors

## Configuration

**application.yml** (in each service):
```yaml
spring:
  web:
    cors:
      allowed-origins: http://localhost:3000, https://app.example.com
      allowed-methods: GET, POST, PUT, DELETE
      allowed-headers: "*"
      allow-credentials: true
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://auth-server:8080
          jwk-set-uri: http://auth-server:8080/.well-known/jwks.json
```

## Build & Test

```bash
./gradlew :common:web:clean build
```

## Usage in Other Modules

```java
// In auth-server, automatically inherited
@SpringBootApplication
public class AuthServerApplication {
  // @EnableWebSecurity, @EnableGlobalMethodSecurity applied via auto-config
}

// Error handling automatic
@RestController
public class UserController {
  @GetMapping("/{id}")
  public UserResponse getUser(@PathVariable String id) {
    // If UserNotFoundException thrown → 404 (mapped by GlobalExceptionHandler)
    return userService.getUser(id);
  }
}
```

## References

**Used by**: auth-server, chat, push-service  
**Parent**: `../../CLAUDE.md`  
**Depends on**: common:core, common:logging

---
**Last Updated**: 2026-04-08 | **Scope**: shared web configuration
