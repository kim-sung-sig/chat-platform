# SDD: scheduled-message

## 1. Title / Version / Status / Owners

- **Title**: 메시지 예약 발송 (Scheduled Message)
- **Version**: 1.0
- **Status**: Draft
- **Owners**: chat-platform team
- **Related Docs**:
  - Plan: `docs/01-plan/features/chat-improve.plan.md` (BE-P1-1)
  - Design: `docs/02-design/features/chat-improve.design.md`
  - Parent SDD: `docs/specs/SDD_chat-improve.md`
  - Skeleton output: `apps/chat/chat-server/src/main/java/com/example/chat/scheduled/`
  - Tests output: `apps/chat/chat-server/src/test/java/com/example/chat/scheduled/`

---

## 2. Problem Statement

- **What**: 채팅 사용자가 메시지를 미래 특정 시각에 자동 발송하도록 예약할 수 없다. `schedule_rules` 테이블과 Quartz 스키마가 이미 DB에 존재하지만, 도메인 모델·애플리케이션 서비스·Job 구현이 없다.
- **Why now**: chat-improve 고도화의 P1 우선순위 항목으로, 사용자가 시차나 업무 시간 외에 메시지를 보낼 때 필요한 핵심 기능이다.

---

## 3. Goals / Non-Goals

### Goals
- `schedule_rules` 테이블을 사용하는 `ScheduledMessage` Aggregate Root 구현
- Quartz Job으로 `scheduledAt` 시각에 메시지 자동 발송
- CRUD API: 예약 생성·조회·취소
- 발송 실패 시 최대 3회 재시도
- 채널 멤버십 실행 시점 재검증

### Non-Goals
- 반복(Recurring) 예약 (`cron_expression` 컬럼은 존재하나 이번 구현에서는 ONCE 타입만)
- 예약 메시지 수정 (취소 후 재생성으로 대체)
- 관리자 예약 목록 전체 조회

---

## 4. Stakeholders / Target Users

- **채팅 사용자**: 원하는 시각에 메시지를 예약·관리
- **채널 멤버**: 예약된 메시지를 예약 시각에 수신
- **시스템**: Quartz Job이 스케줄러로서 실행

---

## 5. Requirements

### Functional Requirements

| ID | 요구사항 | 검증 방법 |
|----|---------|----------|
| FR-01 | 사용자는 채널에 메시지를 예약할 수 있다 (현재 시각 + 5분 ~ +30일) | POST /api/messages/schedule → 201 |
| FR-02 | 예약 채널당 10개/일 한도 초과 시 거부 | 11번째 요청 → 429 |
| FR-03 | PENDING 상태의 예약만 취소 가능 | DELETE → 204; EXECUTED 후 → 409 |
| FR-04 | Quartz Job이 scheduledAt에 메시지를 발송한다 | Job 실행 후 chat_messages에 행 생성 확인 |
| FR-05 | 발송 시 채널 멤버십을 재검증한다 (탈퇴 시 CANCELLED) | 멤버 제거 후 Job 실행 → status=CANCELLED |
| FR-06 | 발송 실패 시 최대 3회 재시도 (30s 간격) | S3/DB 실패 주입 후 retry count 확인 |
| FR-07 | 발송 성공 시 status=EXECUTED, executedAt 기록 | Job 완료 후 DB 확인 |
| FR-08 | 취소 시 status=CANCELLED, cancelledAt 기록 | DELETE 후 DB 확인 |

### Non-Functional Requirements

- 예약 생성 API 응답: < 200ms (p99)
- Quartz Job 실행 지연: scheduledAt ± 30초 이내
- schedule_rules 행 개수 > 10만 시 인덱스 활용 (idx_schedule_scheduled_at)

---

## 6. Domain Knowledge

### Glossary

| 용어 | 정의 |
|------|------|
| ScheduledMessage | 미래 시각에 발송될 메시지 예약 단위 (Aggregate Root) |
| ScheduleType | `ONCE` — 단순 1회 예약 (현재 지원) |
| ScheduleStatus | `PENDING` / `EXECUTING` / `EXECUTED` / `CANCELLED` / `FAILED` |
| scheduledAt | 발송 예정 UTC 시각 |
| executedAt | 실제 발송 완료 UTC 시각 |
| retryCount | 현재 재시도 횟수 (최대 3회) |

### Domain Invariants

1. `scheduledAt > now() + 5분` (생성 시 검증)
2. `scheduledAt < now() + 30일` (생성 시 검증)
3. 취소는 `status == PENDING` 일 때만 허용
4. 동일 채널·동일 사용자의 `PENDING` 예약은 하루 최대 10개
5. `retryCount <= 3` — 3회 초과 시 `FAILED` 처리

---

## 7. Domain Model & Boundaries

### Bounded Context

```
com.example.chat.scheduled/
├── domain/
│   ├── model/
│   │   ├── ScheduledMessage.java       # Aggregate Root
│   │   ├── ScheduleType.java           # Enum Value Object
│   │   └── ScheduleStatus.java         # Enum Value Object
│   ├── service/
│   │   └── ScheduledMessageDomainService.java
│   └── repository/
│       └── ScheduledMessageRepository.java  # Port (interface)
├── application/
│   ├── service/
│   │   ├── ScheduledMessageCommandService.java
│   │   └── ScheduledMessageQueryService.java
│   └── job/
│       └── ScheduledMessageJob.java         # Quartz Job
├── event/
│   ├── MessageScheduledEvent.java
│   └── ScheduledMessageExecutedEvent.java
├── infrastructure/
│   ├── datasource/
│   │   ├── ScheduledMessageEntity.java      # JPA → schedule_rules
│   │   └── JpaScheduledMessageRepository.java
│   └── quartz/
│       └── QuartzJobScheduler.java
└── api/
    ├── controller/
    │   └── ScheduledMessageController.java
    ├── request/
    │   └── CreateScheduledMessageRequest.java
    └── response/
        └── ScheduledMessageResponse.java
```

### Aggregate Root: ScheduledMessage

```
ScheduledMessage
├── id: String (UUID)
├── channelId: String
├── senderId: String
├── content: MessageContent  (sealed interface 재사용)
├── scheduleType: ScheduleType (ONCE)
├── status: ScheduleStatus
├── scheduledAt: ZonedDateTime
├── createdAt: ZonedDateTime
├── executedAt: ZonedDateTime?
├── cancelledAt: ZonedDateTime?
└── retryCount: int (0~3)

Methods:
+ cancel(): void          // PENDING → CANCELLED
+ markExecuting(): void   // PENDING → EXECUTING
+ markExecuted(): void    // EXECUTING → EXECUTED
+ markFailed(): void      // EXECUTING → FAILED (retryCount 증가)
+ isRetryable(): boolean  // retryCount < 3
```

---

## 8. Interfaces

### Commands

```java
// CreateScheduledMessageCommand
record CreateScheduledMessageCommand(
    String channelId,
    String senderId,
    MessageContent content,
    ZonedDateTime scheduledAt
) {}

// CancelScheduledMessageCommand
record CancelScheduledMessageCommand(
    String scheduleId,
    String requesterId   // 요청자 = 소유자 검증용
) {}
```

### Queries

```java
// GetScheduledMessagesQuery
record GetScheduledMessagesQuery(
    String channelId,
    String requesterId
) {}
```

### HTTP Endpoints

```
POST   /api/messages/schedule
  Request:  CreateScheduledMessageRequest
  Response: 201 ScheduledMessageResponse

GET    /api/messages/schedule/{channelId}
  Response: 200 List<ScheduledMessageResponse>

DELETE /api/messages/schedule/{id}
  Response: 204 No Content
```

### Request / Response Schema

```json
// CreateScheduledMessageRequest
{
  "channelId": "uuid",
  "content": {
    "type": "TEXT",          // TEXT | IMAGE | FILE
    "text": "예약 메시지 내용"  // type=TEXT 시 필수
  },
  "scheduledAt": "2026-03-26T09:00:00+09:00"
}

// ScheduledMessageResponse
{
  "id": "uuid",
  "channelId": "uuid",
  "senderId": "uuid",
  "content": { "type": "TEXT", "text": "..." },
  "status": "PENDING",
  "scheduledAt": "2026-03-26T09:00:00+09:00",
  "createdAt": "2026-03-25T10:00:00+09:00",
  "executedAt": null,
  "cancelledAt": null
}
```

### Error Codes

| Code | HTTP | 발생 조건 |
|------|------|----------|
| `SCHEDULED_AT_PAST` | 400 | scheduledAt ≤ now() + 5분 |
| `SCHEDULED_AT_TOO_FAR` | 400 | scheduledAt > now() + 30일 |
| `SCHEDULE_LIMIT_EXCEEDED` | 429 | 채널당 일일 10개 초과 |
| `SCHEDULE_NOT_FOUND` | 404 | 예약 없음 |
| `SCHEDULE_ALREADY_EXECUTED` | 409 | EXECUTED 상태에서 취소 시도 |
| `SCHEDULE_NOT_OWNER` | 403 | 본인 예약이 아닌 취소 시도 |
| `CHANNEL_NOT_MEMBER` | 403 | 채널 멤버 아님 |

---

## 9. Data Model

### schedule_rules (기존 테이블 — 변경 없음)

```sql
CREATE TABLE schedule_rules (
    id                VARCHAR(36)   PRIMARY KEY,
    schedule_type     VARCHAR(20)   NOT NULL,   -- 'ONCE'
    schedule_status   VARCHAR(20)   NOT NULL,   -- 'PENDING'|'EXECUTING'|'EXECUTED'|'CANCELLED'|'FAILED'
    message_id        VARCHAR(36)   NOT NULL,   -- 발송된 메시지 ID (실행 후 채움)
    channel_id        VARCHAR(36)   NOT NULL,
    sender_id         VARCHAR(36)   NOT NULL,
    message_text      VARCHAR(5000),
    message_media_url VARCHAR(500),
    message_file_name VARCHAR(255),
    message_file_size BIGINT,
    message_mime_type VARCHAR(100),
    cron_expression   VARCHAR(100),            -- ONCE 타입은 NULL
    scheduled_at      TIMESTAMP     NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    executed_at       TIMESTAMP,
    cancelled_at      TIMESTAMP
);
-- 기존 인덱스
-- idx_schedule_type_status ON (schedule_type, schedule_status)
-- idx_schedule_scheduled_at ON (scheduled_at)
```

**추가 컬럼 필요 (Flyway 마이그레이션 V10)**:

```sql
-- 재시도 횟수 추가
ALTER TABLE schedule_rules ADD COLUMN retry_count INT NOT NULL DEFAULT 0;
```

### JPA Entity 매핑

```java
@Entity
@Table(name = "schedule_rules")
public class ScheduledMessageEntity {
    @Id private String id;
    @Enumerated(EnumType.STRING) private ScheduleType scheduleType;
    @Enumerated(EnumType.STRING) private ScheduleStatus status;
    private String messageId;
    private String channelId;
    private String senderId;
    // MessageContent columns
    private String messageText;
    private String messageMediaUrl;
    private String messageFileName;
    private Long messageFileSize;
    private String messageMimeType;
    private ZonedDateTime scheduledAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime executedAt;
    private ZonedDateTime cancelledAt;
    private int retryCount;
}
```

---

## 10. Workflow / State Transitions

### 예약 발송 상태 전이

```
[생성 요청]
    │
    ▼
 PENDING ──── [취소 요청] ────► CANCELLED
    │
    │  [Quartz Job 트리거: scheduledAt 도달]
    ▼
EXECUTING ──── [발송 성공] ────► EXECUTED
    │
    └── [발송 실패, retryCount < 3] ──► PENDING (재스케줄)
    └── [발송 실패, retryCount >= 3] ──► FAILED
```

### 발송 Job 흐름

```
ScheduledMessageJob.execute(context)
  1. scheduleId = context.getMergedJobDataMap().getString("scheduleId")
  2. scheduledMessage = repository.findById(scheduleId)
  3. scheduledMessage.markExecuting()
  4. 채널 멤버십 재검증:
     └── 멤버 아님 → scheduledMessage.cancel() → return
  5. MessageSendService.send(channelId, senderId, content)
  6. 성공: scheduledMessage.markExecuted()
  7. 실패:
     └── scheduledMessage.markFailed()
     └── isRetryable() → QuartzJobScheduler.reschedule(+30s)
     └── !isRetryable() → log.error("최대 재시도 초과")
```

---

## 11. Validation Rules & Edge Cases

| 규칙 | 검증 위치 | 처리 |
|------|----------|------|
| scheduledAt ≤ now()+5분 | CommandService | `SCHEDULED_AT_PAST` 예외 |
| scheduledAt > now()+30일 | CommandService | `SCHEDULED_AT_TOO_FAR` 예외 |
| 채널 일일 한도 10개 | CommandService | `SCHEDULE_LIMIT_EXCEEDED` 예외 |
| 채널 멤버 아님 (생성 시) | CommandService | `CHANNEL_NOT_MEMBER` 예외 |
| 채널 멤버 아님 (발송 시) | Job | CANCELLED 처리 |
| EXECUTED 취소 시도 | CommandService | `SCHEDULE_ALREADY_EXECUTED` 예외 |
| 다른 사람 예약 취소 | CommandService | `SCHEDULE_NOT_OWNER` 예외 |
| retryCount >= 3 실패 | Job | FAILED 처리, 더 이상 재스케줄 안 함 |

---

## 12. Security / Privacy / Compliance

- **인증**: `SecurityUtils.getCurrentUserId()` — JWT 기반 인증 필수
- **인가**: 예약 취소는 `senderId == currentUserId` 검증
- **채널 멤버십**: 생성 시 및 발송 시 각각 검증
- **내용 감사**: 발송된 message_id를 schedule_rules에 기록 (추적 가능)

---

## 13. Observability

### Logs
```
INFO  scheduled-message-created  scheduleId={} channelId={} senderId={} scheduledAt={}
INFO  scheduled-message-executed scheduleId={} channelId={} messageId={} duration={}ms
WARN  scheduled-message-retry    scheduleId={} retryCount={} error={}
ERROR scheduled-message-failed   scheduleId={} retryCount=3 error={}
INFO  scheduled-message-cancelled scheduleId={} reason={}
```

### Metrics
- `scheduled_message_created_total` (counter)
- `scheduled_message_executed_total` (counter)
- `scheduled_message_failed_total` (counter)
- `scheduled_message_execution_duration_ms` (histogram)

---

## 14. Test Strategy

### 단위 테스트 (ScheduledMessageCommandService)

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledMessageCommandService 단위 테스트")
class ScheduledMessageCommandServiceTest {

    @Nested @DisplayName("예약 생성")
    class Create {
        @Test @DisplayName("정상 예약 생성 - 성공")
        void success() { ... }

        @Test @DisplayName("scheduledAt이 5분 이내 - 실패")
        void failWhenTooSoon() { ... }

        @Test @DisplayName("scheduledAt이 30일 초과 - 실패")
        void failWhenTooFar() { ... }

        @Test @DisplayName("채널 일일 한도 초과 - 실패")
        void failWhenLimitExceeded() { ... }

        @Test @DisplayName("채널 멤버 아님 - 실패")
        void failWhenNotMember() { ... }
    }

    @Nested @DisplayName("예약 취소")
    class Cancel {
        @Test @DisplayName("PENDING 상태 취소 - 성공")
        void successWhenPending() { ... }

        @Test @DisplayName("EXECUTED 상태 취소 - 실패")
        void failWhenExecuted() { ... }

        @Test @DisplayName("본인 예약 아님 - 실패")
        void failWhenNotOwner() { ... }
    }
}
```

### 단위 테스트 (ScheduledMessageJob)

```java
@DisplayName("ScheduledMessageJob 단위 테스트")
class ScheduledMessageJobTest {

    @Test @DisplayName("정상 발송 - EXECUTED 상태로 전이")
    void executeSuccess() { ... }

    @Test @DisplayName("채널 멤버 탈퇴 시 - CANCELLED 처리")
    void cancelWhenNotMember() { ... }

    @Test @DisplayName("발송 실패 retryCount < 3 - 재스케줄")
    void retryWhenFailed() { ... }

    @Test @DisplayName("발송 실패 retryCount = 3 - FAILED 처리")
    void failWhenMaxRetry() { ... }
}
```

### 도메인 단위 테스트 (ScheduledMessage)

```java
@DisplayName("ScheduledMessage 도메인 단위 테스트")
class ScheduledMessageTest {

    @Test @DisplayName("PENDING → CANCELLED 전이 - 성공")
    void cancelFromPending() { ... }

    @Test @DisplayName("EXECUTED → CANCELLED 전이 - 실패")
    void cannotCancelExecuted() { ... }

    @Test @DisplayName("retryCount < 3 - isRetryable true")
    void isRetryableWhenUnderLimit() { ... }

    @Test @DisplayName("retryCount = 3 - isRetryable false")
    void isNotRetryableAtLimit() { ... }
}
```

---

## 15. Risks & Assumptions

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Quartz Job 중복 실행 | Medium | `@DisallowConcurrentExecution` 적용 |
| 서버 재시작 시 PENDING 예약 누락 | Low | Quartz DB 스토어 사용 (QRTZ_* 테이블 활용) |
| scheduledAt 시간대 불일치 | Medium | 모든 시각을 UTC로 저장, 클라이언트에서 변환 |
| 채널 삭제 후 Job 실행 | Low | Job에서 채널 존재 여부 검증, 없으면 CANCELLED |

**Assumptions**:
- Quartz 테이블(QRTZ_*)은 V5 마이그레이션에 이미 생성되어 있다
- `spring-boot-starter-quartz` 의존성을 chat-server build.gradle에 추가
- `schedule_rules.message_id`는 Job 실행 후 발송된 메시지 ID로 채움
- 단일 인스턴스 환경 (Quartz 클러스터링 불필요)

---

## 16. Open Questions

1. `schedule_rules.message_id` 컬럼이 `NOT NULL`인데 생성 시 메시지 ID가 없다 — 임시 UUID 채우기 vs. nullable 변경 여부?
   - **제안**: Flyway V10에서 `message_id` 컬럼을 nullable로 변경
2. 재시도 간격: 30초 고정 vs. 지수 백오프?
   - **제안**: 1차 30s, 2차 120s, 3차 300s (지수 백오프)
3. 예약 취소 소프트 딜리트 vs. 하드 딜리트?
   - **제안**: 소프트 딜리트 (status=CANCELLED, cancelledAt 기록)
