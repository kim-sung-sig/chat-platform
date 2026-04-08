---
name: SDD-Driven Development (Software Design Document)
description: All features require SDD before implementation. Code must match SDD.
scope: All microservices
applies-to: Feature design, implementation
version: 1.0
triggers: Design phase start, before code skeleton
---

# Rule: SDD-Driven Development

## Core Principle

**SDD is the single source of truth. Code must match SDD exactly. Domain language consistent.**

## SDD Location

```
docs/specs/SDD_<feature-name>.md
```

Example: `SDD_scheduled-message.md`

## 8-Section SDD Structure

| Section | Content | Purpose |
|---------|---------|---------|
| **1. Overview** | What, why, scope, timeline | Requirements clarity |
| **2. Domain Model** | Aggregates, invariants, ubiquitous language | Business concepts |
| **3. API Contracts** | REST endpoints with JSON examples | HTTP contract |
| **4. Data Model** | Tables, indexes, constraints | Database schema |
| **5. Layered Architecture** | Package structure, class stubs | Code organization |
| **6. Test Plan** | Unit/integration/API test scenarios | Test coverage |
| **7. Event Flows** | Happy path & error cases | Business workflows |
| **8. Implementation Checklist** | Skeleton → Tests → Impl → Verify | Completion criteria |

## Section 2: Ubiquitous Language (Critical)

**Define domain terms once, use consistently everywhere:**

```markdown
### Ubiquitous Language

| Term | Definition | Code Class |
|------|-----------|-----------|
| Schedule | Set message for future send | ScheduledMessage.schedule() |
| Send | Transition SCHEDULED → SENT | ScheduledMessage.markSent() |
| Cancel | User cancels scheduled msg | ScheduledMessage.cancel() |
```

**During implementation**: Use terms exactly.

## Section 5: Architecture (Maps to Code)

```markdown
### Layered Architecture

**Domain Layer**
- domain/model/ScheduledMessage
- domain/repository/ScheduledMessageRepository
- domain/event/ScheduledMessageCreatedEvent

**Application Layer**
- application/service/ScheduleCommandService
- application/service/ScheduleQueryService
- application/listener/ScheduledMessageCreatedHandler

**Infrastructure Layer**
- infrastructure/persistence/JpaScheduledMessageRepository
- infrastructure/event/SendScheduledMessagesJob

**API Layer**
- api/controller/ScheduledMessageController
- api/request/ScheduleMessageRequest
- api/response/ScheduledMessageResponse
```

Each class stub created in `/spec-to-skeleton`.

## Section 6: Test Plan (Becomes Test Names)

```markdown
### Unit Tests
- [ ] ScheduledMessage.schedule() succeeds
- [ ] ScheduledMessage.schedule() rejects past timestamp
- [ ] ScheduledMessage.cancel() only for SCHEDULED
- [ ] ScheduledMessageResponse.from() converts correctly

### Integration Tests
- [ ] ScheduleCommandService persists to DB
- [ ] SendScheduledMessagesJob finds due messages
- [ ] Events published after save

### API Tests
- [ ] POST /api/messages/schedule returns 201
- [ ] POST with past timestamp returns 400
```

During implementation: Create test classes with these names.

## SDD ↔ Code Sync

**When implementing**:
1. Section 2 → Domain model class names, invariants
2. Section 3 → Controller methods, DTOs
3. Section 4 → Entity fields, indexes
4. Section 5 → Package structure, stubs
5. Section 6 → Test class names

**If code deviates**:
- ❌ Change code to match SDD first
- ✅ If change needed, update SDD + get approval

Never leave SDD & code out of sync.

## Creation Workflow

1. Copy `docs/specs/SDD_TEMPLATE.md`
2. Fill 8 sections (each section 1-2 pages)
3. Get stakeholder approval
4. Run `/spec-to-skeleton` (creates stubs from Section 5)
5. Run `/skeleton-to-tests` (creates tests from Section 6)
6. Implement matching Section 2 domain language

## Rules

| Rule | Detail |
|------|--------|
| **8 sections** | All required, no sections skipped |
| **Ubiquitous language** | Defined in Section 2, used everywhere |
| **API specs complete** | All status codes, error responses |
| **Database schema** | Reviewed before coding |
| **Architecture maps** | Section 5 = actual package structure |
| **Test plan concrete** | Section 6 test items = actual test names |
| **Code matches SDD** | Exactly, no deviations |

## Checklist

- [ ] SDD 8 sections complete
- [ ] Domain language defined (Section 2)
- [ ] API specs have all status codes
- [ ] Database schema includes indexes
- [ ] Architecture section matches code structure
- [ ] Test plan names = actual test names
- [ ] Code matches SDD exactly
- [ ] If deviating, SDD updated first

---

Examples: `sdd-driven-development/docs/`
