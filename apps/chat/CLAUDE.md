# apps/chat - CLAUDE.md

Nested configuration for the **Chat Service** microservice.

## Overview

**Purpose**: Real-time messaging service (chat rooms, direct messages, message history)  
**Stack**: Java 21, Spring Boot 3.x, WebSocket, PostgreSQL, Redis  
**Key Deps**: Spring Cloud (Eureka, Config), Spring Data JPA, Micrometer Tracing

## Module Dependencies
```
chat → auth-server (validate tokens), common:core, common:logging, common:web
chat ← push-service (send notifications)
```

## Layered Architecture

See `../.claude/rules/12-ddd-layered-architecture.md` for standard structure.

**chat specifics**:
- `domain/model/`: ChatRoom, Message, User (JPA entities)
- `domain/event/`: MessageCreatedEvent, RoomCreatedEvent, UserJoinedEvent
- `application/service/`: MessageApplicationService, ChatRoomApplicationService
- `application/command/`: SendMessageCommand, CreateChatRoomCommand, AddUserCommand
- `application/query/`: GetMessagesQuery, GetChatRoomQuery
- `infrastructure/websocket/`: WebSocket handlers, message broadcast
- `infrastructure/cache/`: Redis message queue, room state cache
- `api/controller/`: ChatController, MessageController

## PDCA Workflow

| Phase | Command | Output |
|-------|---------|--------|
| **plan** | `/pdca plan` | Spec: API endpoints, domain models, events |
| **design** | `/sdd-requirements` | SDD doc (docs/specs/SDD_*.md) |
| **do** | `/spec-to-skeleton` → tests → impl | Green tests + compile |
| **analyze** | `./gradlew :apps:chat:compile*` → `/pdca analyze` | Gap ≥ 90% |
| **report** | `/pdca report` | Completion report |

## Build & Test

```bash
# Compile & test
./gradlew :apps:chat:compileJava compileTestJava --no-daemon
./gradlew :apps:chat:test

# Full build
./gradlew :apps:chat:clean build

# Local run
./gradlew :apps:chat:bootRun
```

## WebSocket & Real-time

- **Handler**: `infrastructure/websocket/ChatWebSocketHandler.java`
- **Broker**: Redis Pub/Sub (for multi-instance scaling)
- **Events**: Message published → Redis → broadcast to subscribers

## Configuration

**Files**: `src/main/resources/`
- `application.yml`, `application-dev.yml`, `application-prod.yml`
- Override via environment variables

## References

**Global Rules**: `../.claude/rules/12-ddd-layered-architecture.md`  
**Parent**: `../CLAUDE.md`  
**Related**: `../auth-server/CLAUDE.md`, `../push-service/CLAUDE.md`

---
**Last Updated**: 2026-04-08 | **Scope**: chat microservice
