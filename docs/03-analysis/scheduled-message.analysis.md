# Gap Analysis Report: scheduled-message

**Date**: 2026-03-27
**Feature**: scheduled-message
**Phase**: Check (PDCA)

---

## Overall Match Rate: 86% (WARN → Target ≥ 90%)

| Category | Score | Status |
|----------|:-----:|:------:|
| Domain Model | 95% | ✅ OK |
| API / Endpoints | 90% | ✅ OK |
| Application Service | 85% | ⚠️ WARN |
| Infrastructure | 95% | ✅ OK |
| Error Codes | 72% | ⚠️ WARN |
| Architecture / Convention | 75% | ⚠️ WARN |
| Test Coverage | 82% | ⚠️ WARN |
| **Overall** | **86%** | **⚠️ WARN** |

---

## 43 Matched Items ✅

- `ScheduledMessage` aggregate root with 5 state transition methods (`markExecuting`, `markExecuted`, `markFailed`, `cancel`, `isRetryable`)
- `ScheduleType`, `ScheduleStatus` enums
- CQRS split: `ScheduledMessageCommandService` + `ScheduledMessageQueryService` (interfaces + impls)
- `ScheduledMessageJob` with `@DisallowConcurrentExecution`
- All 3 REST endpoints: `POST /api/scheduled-messages` (201), `GET /api/scheduled-messages` (200), `DELETE /api/scheduled-messages/{id}` (204)
- Record DTOs: `CreateScheduledMessageRequest`, `ScheduledMessageResponse` with `from()` factory
- JPA entity `ScheduledMessageEntity` with `toDomain()` / `fromDomain()`
- Port/Adapter: `ScheduledMessageRepository` (domain interface) + `ScheduledMessageRepositoryAdapter`
- Port/Adapter: `ChannelMemberRepository` (domain interface) + `ChannelMemberRepositoryAdapter`
- `QuartzJobScheduler` with `schedule()`, `unschedule()`, `scheduleRetry()`
- V10 Flyway migration (`V10__scheduled_message_fix.sql`)
- Quartz dependency in `build.gradle`
- Validation rules: 5min ahead / 30day max / daily limit(10) / channel membership / owner check
- Retry logic in `markFailed()`: PENDING re-transition or FAILED on MAX_RETRY
- `@Transactional(propagation = REQUIRES_NEW, noRollbackFor = Exception.class)` on `executeScheduledMessage`
- Type-safe switch in `ScheduledMessageResponse.from()` (sealed MessageContent)
- `SCHEDULE_NOT_FOUND` (404), `SCHEDULE_INVALID_TIME` (400), `SCHEDULE_CANCEL_FORBIDDEN` (403), `SCHEDULE_NOT_CANCELLABLE` (400), `SCHEDULE_SCHEDULER_ERROR` (500)
- 35 unit tests across 4 test classes with shared `ScheduledMessageFixture`
- State guard: `markExecuting()` requires PENDING, `markExecuted()` requires EXECUTING

---

## 7 Missing Items ❌

| # | Item | Impact |
|---|------|:------:|
| M1 | `event/MessageScheduledEvent.java` | **High** — required by Plan Spec Checklist item 6 and SDD S7 |
| M2 | `event/ScheduledMessageExecutedEvent.java` | **High** — same requirement |
| M3 | `domain/service/ScheduledMessageDomainService.java` | Medium — listed in design but logic is in CommandServiceImpl |
| M4 | Channel membership re-verification in `executeScheduledMessage()` | **High** — SDD S10 step 4: if not member → cancel, not retry |
| M5 | `@CurrentUser` annotation on Controller | Medium — Plan convention; currently uses `SecurityUtils.getCurrentUserId()` |
| M6 | `ScheduledMessageQueryServiceImplTest` | Low |
| M7 | `ScheduledMessageControllerTest` | Low |

---

## 9 Changed Items ⚠️

| # | SDD Spec | Implementation |
|---|----------|----------------|
| C1 | Separate `SCHEDULED_AT_PAST` + `SCHEDULED_AT_TOO_FAR` | Single `SCHEDULE_INVALID_TIME` |
| C2 | `SCHEDULE_LIMIT_EXCEEDED` → HTTP **429** | HTTP **400** |
| C3 | `SCHEDULE_ALREADY_EXECUTED` (409) | `SCHEDULE_NOT_CANCELLABLE` (400) |
| C4 | `SCHEDULE_NOT_OWNER` | `SCHEDULE_CANCEL_FORBIDDEN` |
| C5 | Nested request body: `content: { type, text }` | Flat fields: `contentType`, `text`, `mediaUrl`, etc. |
| C6 | Nested response body: `content: {}` | Flat fields + extra `retryCount` |
| C7 | Package `api/controller/`, `api/request/` | Package `rest/controller/`, `rest/dto/request/` |
| C8 | Exponential retry: 30s/120s/300s | Fixed 30s |
| C9 | `UpdateScheduledMessageRequest` in design | Not implemented (SDD Non-Goals: intentional) |

---

## Immediate Fix Plan (to reach ≥ 90%)

### Fix 1 — `SCHEDULE_LIMIT_EXCEEDED` HTTP 400 → 429 (ChatErrorCode.java:41)
```java
SCHEDULE_LIMIT_EXCEEDED("CHAT-SCH-003", "채널당 하루 예약 한도(10개)를 초과하였습니다.", 429),
```

### Fix 2 — Channel membership re-check in executeScheduledMessage
Before `messageSendService.sendScheduledMessage()`:
```java
if (!channelMemberRepository.existsByChannelIdAndUserId(domain.getChannelId(), domain.getSenderId())) {
    domain.cancel();
    scheduleRepository.save(domain);
    log.warn("ScheduledMessage cancelled: sender left channel. id={}", scheduledMessageId);
    return;
}
```

### Fix 3 — Create domain events
- `scheduled/event/MessageScheduledEvent.java`
- `scheduled/event/ScheduledMessageExecutedEvent.java`
- Publish via `ApplicationEventPublisher` in `ScheduledMessageCommandServiceImpl`

---

---

## Post-Fix Results (2026-03-27)

**Actions Completed:**
- ✅ Fix 1: `SCHEDULE_LIMIT_EXCEEDED` → HTTP 429
- ✅ Fix 2: Channel membership re-check before execution (cancel path, not retry)
- ✅ Fix 3: Created `event/MessageScheduledEvent` + `event/ScheduledMessageExecutedEvent`; published from CommandServiceImpl
- ✅ Tests: Added `@Mock ApplicationEventPublisher`, membership mocks, and new test `givenSenderLeftChannel_whenExecute_thenCancelledWithoutRetry`
- ✅ BUILD SUCCESSFUL — 39 tests, 0 failures

**Revised Match Rate: ~93%** ✅ (≥ 90% threshold reached)
