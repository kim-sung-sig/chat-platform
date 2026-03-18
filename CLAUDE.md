# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build everything
./gradlew clean build

# Build a specific service
./gradlew :apps:chat:chat-server:build
./gradlew :apps:chat:websocket-server:build
./gradlew :infrastructure:api-gateway:build

# Run a service
./gradlew :apps:chat:chat-server:bootRun
./gradlew :apps:chat:websocket-server:bootRun

# Run all tests
./gradlew test

# Run tests for a single module
./gradlew :apps:chat:chat-server:test

# Run a single test class
./gradlew :apps:chat:chat-server:test --tests "com.example.chat.channel.ChannelCommandServiceTest"
```

## Infrastructure (Docker)

Start all external dependencies before running services:
```bash
cd docker && docker compose up -d
```

Services: PostgreSQL 17.6 source (15432) + replica (15433), Redis 7.4.1 (16379), Kafka 3.9 (29092), Zipkin (9411).

Start order for microservices: config-server → eureka-server → api-gateway → apps.

## Architecture Overview

**Gradle multi-module monorepo** with Java 21 + Kotlin, Spring Boot 3.4.4, Spring Cloud 2024.0.2.

### Services

| Service | Port | Role |
|---------|------|------|
| `infrastructure:config-server` | 8888 | Centralized config |
| `infrastructure:eureka-server` | 8761 | Service discovery |
| `infrastructure:api-gateway` | 8000 | Routing |
| `apps:chat:chat-server` | 20001 | Core business logic |
| `apps:chat:websocket-server` | 20002 | WebSocket/STOMP connections |
| `apps:auth-server` | — | JWT, OAuth2, MFA (TOTP) |
| `apps:push-service` | — | Kafka-based push notifications |

### Shared Libraries

- `common:core` — exceptions, enums, constants
- `common:security` — JWT authentication filter
- `common:web` — web utilities
- `common:logging` — Micrometer tracing (Brave), Logback config
- `apps:chat:libs:chat-storage` — JPA entities and repositories shared between chat-server and websocket-server

### Bounded Contexts in chat-server

Each bounded context (`message`, `channel`, `friendship`, `approval`, `voice`) follows this layered structure:

```
<context>/
├── domain/
│   ├── model/        # Aggregates, Value Objects
│   ├── service/      # Domain Services
│   └── repository/   # Port interfaces
├── application/
│   └── service/      # Command/Query use cases (CQRS)
├── infrastructure/
│   ├── kafka/        # Kafka consumers/producers
│   ├── redis/        # Cache and Pub/Sub adapters
│   └── datasource/   # JPA repository adapters
└── rest/
    ├── controller/   # REST endpoints
    └── dto/          # Request/Response DTOs
```

### Event Flow

```
HTTP Client → API Gateway (8000) → chat-server (20001)
                                        ↓
                              PostgreSQL (source: writes, replica: reads)
                              Redis (unread count cache, Pub/Sub events)
                              Kafka (read receipts, member-left, push notifications)

WebSocket Client → websocket-server (20002)
                        ↓
                   Redis Pub/Sub ← chat-server publishes domain events
```

### Key Design Decisions

- **chat-server consolidates** the former message-server and system-server to reduce operational overhead.
- **websocket-server is separate** to scale independently based on WebSocket connection count.
- **CQRS**: Commands go to `XxxCommandService`, queries go to `XxxQueryService`. Read queries use cursor-based pagination.
- **Hexagonal architecture**: domain and application layers have no framework dependencies; infrastructure adapters implement domain port interfaces.
- **Virtual threads** are enabled (`spring.threads.virtual.enabled=true`) — avoid thread-local patterns.
- **PostgreSQL read/write splitting**: write operations use `source` datasource, reads use `replica` datasource.

## Development Principles

Full conventions in [docs/conventions/CONVENTIONS.md](docs/conventions/CONVENTIONS.md). Key rules:

- **DDD layering**: `domain` has zero framework dependencies → `application` → `infrastructure`/`rest`.
- **CQRS**: writes → `XxxCommandService`, reads → `XxxQueryService` with cursor pagination.
- **No magic constants**: use enums or domain types.
- **Early return**: no nested if-else.
- **TDD**: tests first for domain logic and aggregates.
- **Constructor injection** for all dependencies.

## Harness (`.claude/`)

| 위치 | 역할 |
|------|------|
| `.claude/settings.json` | 팀 공유 하네스 — 훅 + 자동승인 권한 |
| `.claude/settings.local.json` | 개인 오버라이드 (git 미추적) |
| `.claude/agents/` | 에이전트 팀: `planner`, `developer`, `reviewer`, `qa` |
| `.claude/commands/` | 슬래시 커맨드 정의 |
| `.claude/hooks/` | 훅 스크립트 |

### 훅 동작

| 이벤트 | 스크립트 | 동작 |
|--------|---------|------|
| `PreToolUse(Bash)` | `bash-guard.sh` | `rm -rf`, `git push --force` 등 위험 명령 차단 (exit 2) |
| `PreToolUse(Edit\|Write)` | `file-guard.sh` | `settings.json`, `compose.yml` 등 보호 파일 편집 차단 |
| `SessionStart(compact)` | `on-compact.sh` | 컨텍스트 압축 후 핵심 아키텍처 규칙 재주입 |
| `Notification` | `notify.sh` | 승인 대기·유휴 시 터미널 벨 + 메시지 |

## Agent Workflow & Slash Commands

Multi-agent SDLC — see [AGENTS.md](AGENTS.md) for the feedback loop and roles.
All commands are in [`.claude/commands/`](.claude/commands/) — full reference in [AGENT_COMMANDS.md](AGENT_COMMANDS.md).

**SDLC phases**: `/plan` → `/develop` → `/review` (score >80 to pass) → `/qa`

**SDD skill chain**: `/sdd-requirements` → `/sdd-read` → `/spec-to-skeleton` → `/skeleton-to-tests` → `/sdd-review`

**Utilities**: `/explain`, `/patch`, `/refactor`, `/perf`, `/security`, `/docs`
