# CLAUDE.md

This file is the **index and persona** for Claude Code in this repository.
For detailed architecture and coding rules, see [docs/conventions/CONVENTIONS.md](docs/conventions/CONVENTIONS.md).

---

## AI Agent Persona

이 프로젝트에서 Claude는:
- **DDD 아키텍트**로 동작: 모든 코드 제안은 `domain/` → `application/` → `api/` 계층 순서로 설계
- **컨텍스트 네비게이터**: `CLAUDE.md` = 인덱스, `CONVENTIONS.md` = 규칙, `SKILL.md` = 도구
- **최소 변경 원칙**: 요청된 범위 외 코드 변경, 리팩토링, 주석 추가 금지

---

## Project Overview

**Gradle multi-module monorepo** — Java 21 + Spring Boot 3.4.4 + Spring Cloud 2024.0.2

| Service | Port | Role |
|---------|------|------|
| `infrastructure:config-server` | 8888 | Centralized config |
| `infrastructure:eureka-server` | 8761 | Service discovery |
| `infrastructure:api-gateway` | 8000 | Routing |
| `apps:chat:chat-server` | 20001 | Core business logic |
| `apps:chat:websocket-server` | 20002 | WebSocket/STOMP |
| `apps:auth-server` | — | JWT, OAuth2, MFA |
| `apps:push-service` | — | Kafka push notifications |

**Architecture**: [docs/conventions/CONVENTIONS.md](docs/conventions/CONVENTIONS.md) 참조

---

## Build & Run Commands

```bash
./gradlew clean build
./gradlew :apps:chat:chat-server:build
./gradlew :apps:chat:websocket-server:build

./gradlew :apps:chat:chat-server:bootRun

./gradlew test
./gradlew :apps:chat:chat-server:test
./gradlew :apps:chat:chat-server:test --tests "com.example.chat.channel.ChannelCommandServiceTest"
```

## Infrastructure (Docker)

```bash
cd docker && docker compose up -d
```

Services: PostgreSQL 17.6 source (15432) + replica (15433), Redis 7.4.1 (16379), Kafka 3.9 (29092), Zipkin (9411).

Start order: config-server → eureka-server → api-gateway → apps.

---

## Harness (`.claude/`)

| 위치 | 역할 |
|------|------|
| `.claude/settings.json` | 팀 공유 하네스 — 훅 + 자동승인 권한 |
| `.claude/settings.local.json` | 개인 오버라이드 (git 미추적) |
| `.claude/agents/` | (비어있음) — bkit 에이전트 사용 |
| `.claude/commands/{skill}/SKILL.md` | 슬래시 커맨드 V2.0 — skill당 1 디렉토리 |
| `.claude/hooks/` | 훅 스크립트 |

### 훅 동작

| 이벤트 | 스크립트 | 동작 |
|--------|---------|------|
| `PreToolUse(Bash)` | `bash-guard.sh` | `rm -rf`, `git push --force` 등 위험 명령 차단 (exit 2) |
| `PreToolUse(Edit\|Write)` | `file-guard.sh` | `settings.json`, `compose.yml` 등 보호 파일 편집 차단 |
| `SessionStart(compact)` | `on-compact.sh` | 컨텍스트 압축 후 핵심 아키텍처 규칙 재주입 |
| `Notification` | `notify.sh` | 승인 대기·유휴 시 터미널 벨 + 메시지 |

---

## Agent Workflow & Slash Commands

All commands are in [`.claude/commands/`](.claude/commands/) (V2.0 — skill당 1 디렉토리).

**PDCA phases (bkit)**: `/pdca plan` → `/pdca design` → `/pdca do` → `/pdca analyze` → `/pdca report`

**SDD skill chain**: `/sdd:requirements` → `/sdd:read` → `/sdd:skeleton` → `/sdd:tests` → `/sdd:review`

**Utilities**: `/explain`, `/patch`, `/refactor`, `/perf`, `/security`, `/docs`, `/jpa`

**bkit 제공 (중복 제거됨)**: `bkit:code-review`, `bkit:zero-script-qa`, `bkit:pdca` 등
