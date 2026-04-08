---
name: DDD Layered Architecture (Database-Driven)
description: |
  Organize code into horizontal layers: api → application → domain → infrastructure.
  JPA entities allowed in domain (Layered Architecture, not Clean).
  Each layer has specific responsibilities and boundaries.
scope: All microservices (auth-server, chat, push-service)
paths:
  - "apps/*/src/main/java/com/example/chat/{module}/"
  - "common/*/src/main/java/com/example/chat/common/"
applies-to: |
  - Designing package structure for new module/service
  - Understanding layer responsibilities
  - Code review for architectural compliance
version: 1.0
triggers:
  - Creating new microservice or bounded context
  - Reviewing code organization
  - Planning new feature architecture
---

# Rule: DDD Layered Architecture (Layered, Database-Driven)

## Architecture Overview

```
HTTP Request
    ↓
┌─────────────────────────────────────────┐
│ api/             (Presentation Layer)    │
│ ├─ controller/   (REST endpoints)        │
│ ├─ request/      (record, validation)    │
│ └─ response/     (record, factory)       │
└─────────────────────┬───────────────────┘
                      ↓ (domain language)
┌─────────────────────────────────────────┐
│ application/     (Use Case Layer)        │
│ ├─ service/      (CommandService,...)    │
│ ├─ command/      (CQRS write)            │
│ ├─ query/        (CQRS read)             │
│ └─ listener/     (Event handlers)        │
└─────────────────────┬───────────────────┘
                      ↓ (business rules)
┌─────────────────────────────────────────┐
│ domain/          (Domain Layer)          │
│ ├─ model/        (JPA entities OK)       │
│ ├─ service/      (Pure logic, no I/O)    │
│ ├─ repository/   (Port interfaces)       │
│ ├─ event/        (Domain events)         │
│ └─ exception/    (Domain exceptions)     │
└─────────────────────┬───────────────────┘
                      ↓ (technical impl)
┌─────────────────────────────────────────┐
│ infrastructure/  (Technical Layer)       │
│ ├─ persistence/  (JPA Repository impl)   │
│ ├─ cache/        (Redis adapters)        │
│ ├─ kafka/        (Event publishing)      │
│ └─ event/        (Event handlers)        │
└─────────────────────────────────────────┘
        ↓
    Database
```

## Layer Responsibilities

### 1. API Layer (api/)
**Responsibility**: REST contract, input validation, response serialization

```
api/
├─ controller/     → @RestController (thin, delegation only)
├─ request/        → record + @Valid (HTTP input)
└─ response/       → record + from() factory (HTTP output)
```

**Rules**:
- ✅ Receive HTTP request, parse DTO
- ✅ Call application service
- ✅ Convert response via factory method
- ❌ No business logic, database access, validation (beyond HTTP)

### 2. Application Layer (application/)
**Responsibility**: Use case orchestration, CQRS separation, event handling

```
application/
├─ service/        → CommandService, QueryService (@Service)
├─ command/        → CQRS write commands
├─ query/          → CQRS read queries
└─ listener/       → @EventListener (side effects)
```

**Rules**:
- ✅ Orchestrate repositories, domain services, events
- ✅ `@Transactional` on commands, `@Transactional(readOnly=true)` on queries
- ✅ Publish domain events after successful mutation
- ❌ No direct database access (use repositories)
- ❌ No business rule validation (domain owns that)

### 3. Domain Layer (domain/)
**Responsibility**: Business logic, models, invariants, ports

```
domain/
├─ model/          → JPA @Entity, Aggregate Root, Value Objects
├─ service/        → Domain services (pure logic, no I/O)
├─ repository/     → Port interfaces (what, not how)
├─ event/          → Domain events (immutable record)
└─ exception/      → Domain-specific exceptions
```

**Rules**:
- ✅ JPA entities allowed (Layered Architecture)
- ✅ Pure business logic (no HTTP, database, Kafka)
- ✅ Define contracts as repository interfaces
- ❌ No Spring annotations except @Entity
- ❌ No I/O or external service calls

### 4. Infrastructure Layer (infrastructure/)
**Responsibility**: Technical implementation of ports, external integrations

```
infrastructure/
├─ persistence/
│  ├─ entity/      → JPA @Entity (if separate from domain)
│  ├─ repository/  → Implements domain/repository/ (JPA-based)
│  └─ mapper/      → Entity ↔ Domain model conversion
├─ cache/          → Redis, caching logic
├─ kafka/          → Kafka producers/consumers
└─ event/          → DomainEventPublisher impl, handlers
```

**Rules**:
- ✅ Implement domain port interfaces
- ✅ Adapt external libraries (JPA, Redis, Kafka)
- ✅ Spring annotations allowed (@Repository, @Component)
- ❌ No business logic (keep it in domain)
- ❌ No direct controller access

## Dependency Rules

**Allowed flows**:
```
api/ → application/ → domain/
api/ → application/ → infrastructure/
application/ → domain/
application/ → infrastructure/
infrastructure/ → domain/ (only via interfaces)
```

**Forbidden**:
```
❌ api/ → domain/ (bypass application)
❌ domain/ → application/ (upward)
❌ domain/ → infrastructure/ (concrete impl)
❌ api/ → infrastructure/ (bypass application)
```

## Key Distinction: Layered vs Clean Architecture

| Aspect | Layered (This Project) | Clean Architecture |
|--------|------------------------|-------------------|
| **JPA in domain** | ✅ Allowed | ❌ Forbidden |
| **Layer count** | 4 (api, app, domain, infra) | 4+ (entity, use case, gateway, controller) |
| **Focus** | Horizontal layers | Concentric circles |
| **Entity location** | domain/model/ | outermost (entity/ layer) |
| **I/O dependency** | Infrastructure imports domain | Domain center, no imports |

**This project uses Layered Architecture** (database-driven, pragmatic for microservices).

## Naming Convention

| Element | Location | Pattern |
|---------|----------|---------|
| Entity | `domain/model/` | `User`, `Message`, `ChatRoom` |
| Repository Interface | `domain/repository/` | `UserRepository`, `MessageRepository` |
| Repository Impl | `infrastructure/persistence/repository/` | `JpaUserRepository` |
| CommandService | `application/service/` | `UserCommandService`, `MessageCommandService` |
| QueryService | `application/service/` | `UserQueryService`, `MessageQueryService` |
| Controller | `api/controller/` | `UserController`, `MessageController` |

## Rules Checklist

- [ ] HTTP entry point is `api/controller/`
- [ ] Controllers are thin (delegate to application service)
- [ ] Request/response are `record` (immutable)
- [ ] Application service orchestrates repos + domain
- [ ] Domain service is pure logic (no I/O)
- [ ] Domain owns port interfaces (`domain/repository/`)
- [ ] Infrastructure implements ports (JPA, Redis, Kafka)
- [ ] No upward dependencies (domain → application)
- [ ] No business logic in infrastructure
- [ ] Events published after domain state change

## See Also

- [Rule 13: Record DTO Pattern](../13-record-dto-pattern/13-record-dto-pattern.md)
- [Rule 14: CQRS Pattern](../14-cqrs-pattern/14-cqrs-pattern.md)
- [Rule 17: Repository Pattern](../17-repository-pattern/17-repository-pattern.md)

---

**Expanded docs available**: `12-ddd-layered-architecture/docs/`