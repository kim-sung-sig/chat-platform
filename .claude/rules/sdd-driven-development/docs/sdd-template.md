# SDD Template — Full Example

Copy to: `docs/specs/SDD_<feature-name>.md`

---

## 1. Overview

**What**: 한 줄로 기능 설명

**Why**: 해결하는 사용자 문제, 비즈니스 가치

**Scope**:
- ✅ In: 구현 범위
- ❌ Out: 제외 범위

**Timeline**: Design 1-2d / Impl 3-4d / QA 1d

---

## 2. Domain Model

### Aggregates

**<AggregateRoot>** (Aggregate Root)
| Field | Type | Constraint |
|-------|------|-----------|
| `id` | `<AggregateId>` | PK |
| `status` | `<StatusEnum>` | NOT NULL |
| `createdAt` | `Instant` | NOT NULL |

### Invariants
1. 불변식 1 (예: scheduledFor > now())
2. 불변식 2 (예: 송신자만 수정 가능)

### Ubiquitous Language

| Term | Definition | Code |
|------|-----------|------|
| Schedule | 미래 시간에 메시지 예약 | `.schedule()` |
| Send | SCHEDULED → SENT 전환 | `.markSent()` |
| Cancel | 사용자가 예약 취소 | `.cancel()` |

---

## 3. API Contracts

### POST /api/<resource>

**Request**
```json
{
  "fieldName": "value"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "status": "ACTIVE"
}
```

**Errors**
- `400` — Validation failed
- `401` — Unauthorized
- `404` — Resource not found

---

## 4. Data Model

```sql
CREATE TABLE <table_name> (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_name  VARCHAR(255) NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    version     BIGINT DEFAULT 0,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

## 5. Layered Architecture

**Package**: `com.example.chat.<module>`

```
domain/
  model/        <Aggregate>.java
  repository/   <Aggregate>Repository.java
  event/        <Aggregate>CreatedEvent.java

application/
  service/      <Aggregate>CommandService.java
                <Aggregate>QueryService.java
  listener/     <Aggregate>CreatedEventHandler.java

infrastructure/
  persistence/
    entity/     <Aggregate>Entity.java
    repository/ Jpa<Aggregate>Repository.java
    mapper/     <Aggregate>Mapper.java

api/
  controller/   <Aggregate>Controller.java
  request/      Create<Aggregate>Request.java
  response/     <Aggregate>Response.java
```

---

## 6. Test Plan

### Unit Tests
- [ ] `<Aggregate>.<method>()` succeeds
- [ ] `<Aggregate>.<method>()` rejects invalid input
- [ ] `<Aggregate>Response.from()` converts correctly

### Integration Tests
- [ ] `<Aggregate>CommandService` persists to DB
- [ ] Events published after save
- [ ] `<Aggregate>QueryService` returns paginated results

### API Tests
- [ ] `POST /api/<resource>` returns 201
- [ ] `POST /api/<resource>` with invalid input returns 400
- [ ] `GET /api/<resource>/{id}` returns 200
- [ ] `GET /api/<resource>/{id}` not found returns 404
- [ ] Unauthenticated request returns 401

---

## 7. Event Flows

### Happy Path
1. Client `POST /api/<resource>`
2. CommandService creates domain aggregate
3. Repository saves to DB
4. EventPublisher fires `<Aggregate>CreatedEvent`
5. Handler 1: Update search index
6. Handler 2: Notify relevant users

### Error Cases
- Validation failure → 400, no event
- DB failure → 500, no event (transaction rolled back)
- Handler failure → logged, command still succeeds

---

## 8. Implementation Checklist

### Skeleton (/spec-to-skeleton)
- [ ] Domain aggregate created
- [ ] Repository interface defined
- [ ] CommandService stub
- [ ] QueryService stub
- [ ] Controller stub
- [ ] DTOs (request/response records)

### Tests (/skeleton-to-tests)
- [ ] Unit tests (domain, service)
- [ ] Integration tests
- [ ] API tests

### Implementation
- [ ] Domain aggregate logic
- [ ] Repository implementation
- [ ] CommandService logic
- [ ] QueryService logic
- [ ] Event handlers
- [ ] Error handling

### Verification (/pdca analyze)
- [ ] `./gradlew compileJava compileTestJava` passes
- [ ] All tests green
- [ ] Gap analysis ≥ 90%