# apps/push-service - CLAUDE.md

Nested configuration for the **Push Notification Service** microservice.

## Overview

**Purpose**: Handle push notifications for chat (FCM, APNs, WebPush)  
**Stack**: Java 21, Spring Boot 3.x, Spring Cloud Stream, PostgreSQL, Redis  
**Key Deps**: Firebase Cloud Messaging (FCM), Kafka/RabbitMQ (event streaming)

## Module Dependencies
```
push-service → auth-server (S2S auth), common:core, common:logging, common:web
push-service ← chat (on MessageCreatedEvent, UserOnlineEvent)
```

## Layered Architecture

See `../.claude/rules/12-ddd-layered-architecture.md`.

**push-service specifics**:
- `domain/model/`: DeviceToken, PushSubscription (JPA entities)
- `domain/event/`: PushSentEvent, PushFailedEvent
- `application/service/`: NotificationApplicationService
- `application/command/`: SendPushCommand, RegisterDeviceCommand
- `infrastructure/provider/`: FCMProvider, APNsProvider, WebPushProvider
- `infrastructure/stream/`: Kafka/RabbitMQ consumers (MessageCreatedEvent, etc.)
- `api/controller/`: DeviceTokenController, PushController

## Event Streaming

- **Consume**: MessageCreatedEvent, UserOnlineEvent from chat service
- **Pattern**: Spring Cloud Stream + Kafka binder
- **Handler**: `infrastructure/stream/MessageEventStreamListener.java`
- **Logic**: Convert events → build push payload → send via providers

## PDCA Workflow

| Phase | Focus | Check |
|-------|-------|-------|
| **plan** | `/pdca plan` | API, domain models, event handlers |
| **design** | `/sdd-requirements` | SDD (docs/specs/SDD_*.md) |
| **do** | `/spec-to-skeleton` → tests → impl | Green tests + compile |
| **analyze** | `./gradlew :apps:push-service:compile*` → `/pdca analyze` | Gap ≥ 90% |
| **report** | `/pdca report` | Completion report |

## Build & Test

```bash
./gradlew :apps:push-service:compileJava compileTestJava --no-daemon
./gradlew :apps:push-service:test
./gradlew :apps:push-service:clean build
./gradlew :apps:push-service:bootRun
```

## S2S Authentication

- **Pattern**: JWT with service account credentials
- **Flow**: Push service → auth-server (validate S2S token) → proceed
- **Config**: Service account JWT stored in environment

## Configuration

**Files**: `src/main/resources/`
- `application.yml`, `application-dev.yml`, `application-prod.yml`
- FCM key, APNs certificate stored in environment or secrets manager

## References

**Global Rules**: `../.claude/rules/12-ddd-layered-architecture.md`  
**Parent**: `../CLAUDE.md`  
**Related**: `../chat/CLAUDE.md`, `../auth-server/CLAUDE.md`

---
**Last Updated**: 2026-04-08 | **Scope**: push notification microservice
