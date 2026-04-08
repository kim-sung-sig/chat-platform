# apps/auth-server - CLAUDE.md

Nested configuration for the **Authentication Server** microservice.

## Overview

**Purpose**: Centralized authentication & authorization service (OAuth2/JWT, 2FA, session management)  
**Stack**: Java 21, Spring Boot 3.x, Spring Security, JWT, TOTP, PostgreSQL, Redis  
**Key Deps**: Spring Cloud (Eureka, Config), Micrometer (Tracing/Brave), SpringDoc OpenAPI

## Module Dependencies
```
auth-server → common:core, common:logging, common:web
```

## Layered Architecture

See `.claude/rules/12-ddd-layered-architecture.md` for standard structure.

**auth-server specifics**:
- `domain/model/`: User, UserCredential, RefreshToken (JPA entities allowed)
- `domain/event/`: UserCreatedEvent, UserLoginEvent, PasswordResetEvent
- `application/service/`: UserApplicationService, TokenApplicationService, AuthenticationService
- `application/command/`: CreateUserCommand, ValidateCredentialCommand
- `application/query/`: GetUserByIdQuery, SearchUsersQuery
- `infrastructure/oauth2/`: OAuth2 client config (Google, GitHub, etc.)
- `infrastructure/cache/`: Redis session, token caching
- `api/controller/`: AuthController, TokenController, UserController

## PDCA Workflow

| Phase | Command | Checklist |
|-------|---------|-----------|
| **plan** | `/pdca plan auth-feature` | Spec with API endpoints, domain models, events, exceptions |
| **design** | `/sdd-requirements` → `/pdca design` | SDD document (docs/specs/SDD_*.md) |
| **do** | `/spec-to-skeleton` → `/skeleton-to-tests` → impl | Tests Green + compile pass |
| **analyze** | `./gradlew :apps:auth-server:compile*` → `/pdca analyze` | Gap rate ≥ 90% |
| **report** | `/pdca report` | Completion report |

## Build & Test

```bash
# Compile
./gradlew :apps:auth-server:compileJava compileTestJava --no-daemon

# Run tests
./gradlew :apps:auth-server:test

# Full build
./gradlew :apps:auth-server:clean build

# Local run
./gradlew :apps:auth-server:bootRun
```

## Testing Strategy

- **Unit**: Domain models, services (src/test/.../unit/)
- **Integration**: Repository, event publishing (src/test/.../integration/ + TestContainers)
- **API**: REST endpoints, security (src/test/.../api/)

## Configuration

**Files**: `src/main/resources/`
- `application.yml` — common
- `application-dev.yml` — local
- `application-prod.yml` — production

Override via environment variables (Spring property resolution).

## References

**Global Rules**:
- [DDD Layered Architecture](./../.claude/rules/12-ddd-layered-architecture.md)
- [Parent CLAUDE.md](../CLAUDE.md)
- [Conventions](../../docs/conventions/CONVENTIONS.md)

**Related Modules**:
- [apps/chat](../chat/CLAUDE.md)
- [apps/push-service](../push-service/CLAUDE.md)
- [common/core](../../common/core/CLAUDE.md)

---
**Last Updated**: 2026-04-08 | **Scope**: auth-server microservice
