# SDD: chat-improve

## 1. Title / Version / Status / Owners

- **Title**: 채팅 애플리케이션 전면 고도화 (P0 + P1)
- **Version**: 1.0
- **Status**: Draft
- **Owners**: chat-platform team
- **Related Docs**:
  - Plan: `docs/01-plan/features/chat-improve.plan.md`
  - Design: `docs/02-design/features/chat-improve.design.md`
  - 참조 SDD: `SDD_webrtc-voice.md`, `SDD_approval-system.md`

---

## 2. Problem Statement

- **What**: 예약 발송·파일 업로드·타이핑 인디케이터 등 핵심 기능이 미완성이고, 아키텍처 컨벤션 위반(3건)과 보안 취약점(Rate Limiting 없음, WebSocket 토큰 URL 노출)이 존재한다.
- **Why now**: 프론트엔드 가상 스크롤 미구현으로 1000+ 메시지 채널에서 성능 저하가 실사용 중 발생 중이며, P0 보안 이슈는 즉시 해결이 필요하다.

---

## 3. Goals / Non-Goals

### Goals
- P0: 컨벤션 위반 3건 수정, Rate Limiting 구현, WebSocket 토큰 보안 강화, 글로벌 에러 핸들러 통일
- P1: 예약 발송(Quartz), 파일 업로드(S3), 타이핑 인디케이터(WebSocket), 가상 스크롤(vue-virtual), 오프라인 큐(Service Worker)
- 모든 신규 코드: DDD/CQRS 컨벤션 100% 준수, 단위 테스트 80% 이상

### Non-Goals
- P2(도메인 이벤트 추가, Audit Log, 테스트 커버리지): 별도 SDD
- P3(승인 시스템 완성, 전문 검색, 리액션): 별도 SDD
- WebRTC 개선: 기존 `SDD_webrtc-voice.md` 참조

---

## 4. Stakeholders / Target Users

- **채팅 사용자**: 예약 발송, 파일 첨부, 빠른 메시지 로딩 혜택
- **백엔드 개발자**: DDD 레이어 일관성, 컨벤션 준수 코드
- **운영팀**: Rate Limiting으로 DDoS 방어, Audit Log 기반 디버깅

---

## 5. Requirements

### Functional Requirements

**FR-01: 예약 발송**
- 사용자는 메시지를 미래 특정 시각에 발송 예약할 수 있다
- 예약은 채널당 10개/일로 제한
- 예약 취소는 실행 전까지 가능
- 지원 범위: 현재 시각 + 5분 ~ 30일 이내

**FR-02: 파일 업로드**
- 이미지(JPEG, PNG, GIF, WebP): 최대 10MB
- 문서(PDF, DOC, DOCX, ZIP): 최대 50MB
- 동영상(MP4, MOV): 최대 100MB
- 업로드 완료 후 CDN URL 반환

**FR-03: 타이핑 인디케이터**
- 사용자가 입력 시작 시 채널 멤버에게 "입력 중" 표시
- 3초 동안 키 입력 없으면 자동 종료
- 최대 3명까지 표시: "홍길동, 김철수, 이영희 님이 입력 중..."
- 3명 초과: "3명이 입력 중..."

**FR-04: 가상 스크롤**
- 10,000개 메시지에서 렌더링 60fps 유지
- 스크롤 위치: 채널 전환 시 마지막 위치 복원
- 새 메시지 도착 시: 하단 스크롤 중이면 자동 스크롤, 아니면 "N개의 새 메시지" 배지

**FR-05: 오프라인 메시지 큐**
- 오프라인 중 전송 시도한 메시지를 IndexedDB에 저장
- 온라인 복귀 시 자동 전송 (순서 보장)
- 오프라인 배너: "오프라인 상태입니다 (N개 메시지 대기 중)"

### Non-Functional Requirements

- Rate Limit 응답 시간: < 5ms (Redis 기반)
- 파일 업로드 처리: presigned URL 방식으로 서버 부하 최소화
- 타이핑 인디케이터 지연: < 500ms (Redis TTL 활용)
- 가상 스크롤 DOM 노드: 최대 50개 유지 (메모리 최적화)

---

## 6. Domain Knowledge

### Glossary
- **ScheduledMessage**: 미래 시각에 자동 발송되도록 예약된 메시지 집합
- **ScheduleStatus**: `PENDING` (대기) → `EXECUTED` (완료) | `CANCELLED` (취소)
- **UploadedFile**: S3에 업로드된 파일의 메타데이터 (URL, 크기, MIME 타입)
- **TypingSession**: 특정 사용자가 특정 채널에서 타이핑 중인 임시 상태 (TTL=3s)
- **PendingMessage**: 오프라인 상태에서 전송 대기 중인 메시지 (IndexedDB 저장)

### Domain Invariants
- ScheduledMessage는 현재 시각 이후에만 생성 가능
- ScheduledMessage는 `PENDING` 상태에서만 취소 가능
- 동일 사용자의 동일 채널 TypingSession은 1개만 존재 (덮어쓰기)
- UploadedFile은 불변 (업로드 후 URL 변경 불가)

---

## 7. Domain Model & Boundaries

### Bounded Contexts (신규)

```
scheduled/          ← 예약 발송 컨텍스트
file/               ← 파일 업로드 컨텍스트
typing/ (websocket) ← 타이핑 인디케이터 (websocket-server 내)
```

### Aggregate Boundaries

**ScheduledMessage (Aggregate Root)**
```
ScheduledMessage
├── id: UUID
├── channelId: UUID
├── senderId: UUID
├── content: MessageContent (Value Object, 재사용)
├── scheduleType: ScheduleType (ONCE)
├── status: ScheduleStatus
├── scheduledAt: ZonedDateTime
├── executedAt: ZonedDateTime?
├── cancelledAt: ZonedDateTime?
└── createdAt: ZonedDateTime
```

**UploadedFile (Value Object — 독립 entity 없음)**
```
UploadedFile
├── fileUrl: String
├── fileName: String
├── fileSize: Long (bytes)
└── mimeType: String
```

**TypingSession (Value Object — Redis only)**
```
TypingSession
├── userId: String
├── username: String
├── channelId: String
└── expiresAt: Instant (TTL=3s)
```

---

## 8. Interfaces

### Commands

| Command | Service | Description |
|---------|---------|-------------|
| `CreateScheduledMessageCommand` | `ScheduledMessageCommandService` | 예약 발송 생성 |
| `CancelScheduledMessageCommand` | `ScheduledMessageCommandService` | 예약 취소 |
| `UploadFileCommand` | `FileUploadService` | 파일 업로드 |
| `StartTypingCommand` | `TypingCommandService` | 타이핑 시작 알림 |
| `StopTypingCommand` | `TypingCommandService` | 타이핑 종료 알림 |

### Queries

| Query | Service | Description |
|-------|---------|-------------|
| `GetScheduledMessagesQuery` | `ScheduledMessageQueryService` | 채널 예약 목록 |
| `GetTypingUsersQuery` | `TypingQueryService` | 현재 타이핑 중인 사용자 |

### Events

| Event | Published by | Consumed by |
|-------|-------------|-------------|
| `MessageScheduledEvent` | `ScheduledMessageCommandService` | 알림(선택) |
| `ScheduledMessageExecutedEvent` | `ScheduledMessageJob` | push-service |

### HTTP Endpoints

```
# 예약 발송
POST   /api/messages/schedule
GET    /api/messages/schedule/{channelId}
DELETE /api/messages/schedule/{id}

# 파일 업로드
POST   /api/files/upload  (multipart/form-data)
```

### WebSocket Messages (기존 채널에 추가)

```
TYPING_START  → { type: "TYPING_START", channelId: string }
TYPING_STOP   → { type: "TYPING_STOP", channelId: string }
// Broadcast
TYPING_UPDATE → { type: "TYPING", userId, username, channelId, action: "START"|"STOP" }
```

### Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `SCHEDULED_AT_PAST` | 400 | scheduledAt이 현재 시각 이전 |
| `SCHEDULED_AT_TOO_FAR` | 400 | scheduledAt이 30일 초과 |
| `SCHEDULE_LIMIT_EXCEEDED` | 429 | 채널당 일일 예약 한도 초과 |
| `SCHEDULE_NOT_FOUND` | 404 | 예약 메시지 없음 |
| `SCHEDULE_ALREADY_EXECUTED` | 409 | 이미 실행된 예약 |
| `FILE_TOO_LARGE` | 413 | 파일 크기 초과 |
| `FILE_TYPE_NOT_ALLOWED` | 415 | 허용되지 않는 파일 타입 |
| `RATE_LIMIT_EXCEEDED` | 429 | Rate Limit 초과 |

---

## 9. Data Model

### schedule_rules (기존 테이블 재사용)

```sql
-- 기존 V5 스키마 그대로 활용
-- Domain model만 신규 추가 (ScheduledMessage aggregate)
-- 컬럼 매핑:
--   schedule_rules.message_text → MessageContent.Text
--   schedule_rules.scheduled_at → ScheduledMessage.scheduledAt
--   schedule_rules.schedule_status → ScheduleStatus enum
```

### audit_log (Flyway V10 신규)

```sql
CREATE TABLE audit_log (
    id            BIGSERIAL PRIMARY KEY,
    actor_id      VARCHAR(36) NOT NULL,
    operation     VARCHAR(100) NOT NULL,       -- e.g., FRIENDSHIP_BLOCKED, SCHEDULE_CANCELLED
    resource_type VARCHAR(50) NOT NULL,        -- e.g., ScheduledMessage, Friendship
    resource_id   VARCHAR(36),
    detail        JSONB,                       -- 추가 컨텍스트
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_audit_actor ON audit_log(actor_id);
CREATE INDEX idx_audit_operation ON audit_log(operation, created_at DESC);
```

---

## 10. Workflow / State Transitions

### 예약 발송 상태 전이

```
[생성] → PENDING
  ├── [취소 요청] → CANCELLED
  └── [Quartz Job 실행] → EXECUTING → EXECUTED
                                   └── [실패] → FAILED (재시도 3회)
```

### 파일 업로드 흐름

```
Client → POST /api/files/upload
       → FileUploadController
       → FileUploadService.upload()
       → S3FileStorageAdapter.store()
       → S3 PUT (presigned URL 또는 직접 업로드)
       → return UploadedFile
       → Client: { fileUrl, fileName, ... }
```

### 타이핑 인디케이터 흐름

```
Client 입력 시작
  → TYPING_START WebSocket 전송
  → TypingCommandService.startTyping()
  → RedisTypingRepository.set(key=typing:{channelId}:{userId}, TTL=3s)
  → 채널 멤버에게 TYPING_UPDATE 브로드캐스트

Client 3초 미입력 또는 TYPING_STOP
  → Redis TTL 만료 or DEL
  → 채널 멤버에게 TYPING_UPDATE(STOP) 브로드캐스트
```

---

## 11. Validation Rules & Edge Cases

| Rule | Field | Constraint |
|------|-------|-----------|
| 예약 시각 미래 | scheduledAt | > now() + 5분 |
| 예약 시각 상한 | scheduledAt | < now() + 30일 |
| 파일 크기 이미지 | fileSize | ≤ 10MB |
| 파일 크기 문서 | fileSize | ≤ 50MB |
| 파일 크기 동영상 | fileSize | ≤ 100MB |
| 타이핑 TTL | Redis key | 3초 |
| Rate Limit 메시지 | per user | 10/s |
| Rate Limit 예약 | per channel/user/day | 10개 |

**Edge Cases**:
- 예약 실행 시 채널에서 탈퇴한 경우: 발송 취소 + `CANCELLED` 상태 처리
- 파일 업로드 중 연결 끊김: S3 multipart upload abort 처리
- 타이핑 중 채널 전환: 이전 채널 TYPING_STOP 자동 처리

---

## 12. Security / Privacy / Compliance

- **파일 업로드**: MIME 타입 검증 (Content-Type 헤더 신뢰 불가 → magic bytes 검사)
- **예약 발송**: 발신자가 채널 멤버인지 검증 (발송 시점에도 재검증)
- **Rate Limiting**: 사용자 ID 기반 (IP 기반은 NAT 환경에서 부정확)
- **파일 URL**: CDN URL은 서명 없이 공개; 민감 파일은 presigned URL (TTL=1h) 사용

---

## 13. Observability

### Logs
- 예약 발송 실행: `INFO scheduled-message-executed messageId={} channelId={} senderId={}`
- 파일 업로드: `INFO file-uploaded fileId={} size={} mimeType={} userId={}`
- Rate Limit 트리거: `WARN rate-limit-triggered userId={} endpoint={}`

### Metrics
- `scheduled_message_executed_total` (counter)
- `file_upload_size_bytes` (histogram)
- `typing_indicator_active_users` (gauge, per channel)
- `rate_limit_rejected_total` (counter, per endpoint)

### Tracing
- 기존 Micrometer Tracing 연동 (trace ID 전파)

---

## 14. Test Strategy

### 예약 발송 단위 테스트

```java
@Nested
@DisplayName("ScheduledMessageCommandService 단위 테스트")
class ScheduledMessageCommandServiceTest {

    @Nested
    @DisplayName("예약 생성")
    class CreateSchedule {
        @Test @DisplayName("정상 예약 생성")
        void success() { ... }

        @Test @DisplayName("과거 시각 예약 - 실패")
        void failWhenPastTime() { ... }

        @Test @DisplayName("30일 초과 예약 - 실패")
        void failWhenTooFar() { ... }
    }

    @Nested
    @DisplayName("예약 취소")
    class CancelSchedule {
        @Test @DisplayName("PENDING 상태 취소 - 성공")
        void successWhenPending() { ... }

        @Test @DisplayName("EXECUTED 상태 취소 - 실패")
        void failWhenAlreadyExecuted() { ... }
    }
}
```

### 파일 업로드 단위 테스트

```java
@Nested
@DisplayName("FileUploadService 단위 테스트")
class FileUploadServiceTest {
    @Test @DisplayName("이미지 10MB 이하 - 성공")
    @Test @DisplayName("이미지 10MB 초과 - 실패")
    @Test @DisplayName("허용되지 않는 MIME - 실패")
    @Test @DisplayName("S3 업로드 실패 - FileUploadException")
}
```

### Rate Limiting 통합 테스트

```java
@Test @DisplayName("동일 사용자 11번 연속 메시지 전송 시 429 반환")
```

---

## 15. Risks & Assumptions

| Risk | Likelihood | Mitigation |
|------|-----------|-----------|
| S3 연결 실패 | Low | Fallback: MinIO 로컬, 에러 응답 명확화 |
| Quartz Job 중복 실행 | Medium | `@DisallowConcurrentExecution` 적용 |
| Redis 장애 시 타이핑 인디케이터 | Low | 타이핑은 UX 기능, 실패해도 무시 |
| 가상 스크롤 동적 높이 | Medium | `estimateSize` + `measureElement` 혼용 |

**Assumptions**:
- S3(또는 MinIO) 버킷은 이미 프로비저닝되어 있다고 가정
- Quartz는 단일 인스턴스 환경 (DB 클러스터링 불필요)
- chat-view는 Nuxt 3 + Vue 3 Composition API 유지

---

## 16. Open Questions

1. S3 버킷 설정: AWS S3 vs MinIO 중 어느 것을 사용할지?
2. 파일 URL: 공개 CDN URL vs 만료 presigned URL 정책?
3. 예약 발송 재시도: 실패 시 재시도 횟수 및 간격?
4. Rate Limiting 키: userId 기반 vs IP 기반 병행?

---

## 17. Implementation Checklist (Do 단계 추적용)

### P0 수정
- [ ] `PushMessage.updateStatus()` 추가, `@Setter` 제거
- [ ] `FriendshipController` `@CurrentUser` 교체
- [ ] `GlobalExceptionHandler` 구현 (common/web)
- [ ] `parseUserFromToken()` 수정 (chat-view)
- [ ] `store/data.ts` mock init isDev 플래그 분리

### P1-1 예약 발송
- [ ] `ScheduledMessage` domain model 생성
- [ ] `ScheduledMessageCommandService` 구현
- [ ] `ScheduledMessageQueryService` 구현
- [ ] `QuartzJobScheduler` 설정
- [ ] `ScheduledMessageJob` 구현
- [ ] `ScheduledMessageController` + Request/Response records
- [ ] 단위 테스트 (CommandService, QueryService)
- [ ] 프론트엔드: `ScheduledMessageService`
- [ ] 프론트엔드: `ScheduledMessageModal.vue`

### P1-2 파일 업로드
- [ ] `FileStoragePort` interface
- [ ] `S3FileStorageAdapter` 구현
- [ ] `FileUploadService` 구현
- [ ] `FileUploadController` + Response record
- [ ] 파일 타입/크기 검증
- [ ] 단위 테스트
- [ ] 프론트엔드: `FileService`
- [ ] 프론트엔드: `FileUploadProgress.vue`

### P1-3 타이핑 인디케이터
- [ ] WebSocket 메시지 타입에 TYPING 추가
- [ ] `TypingCommandService`, `TypingQueryService`
- [ ] `RedisTypingRepository` (TTL=3s)
- [ ] 브로드캐스트 로직 (기존 WebSocket 채널 활용)
- [ ] 프론트엔드: `useTypingIndicator`
- [ ] 프론트엔드: `TypingIndicator.vue`

### P1-4 가상 스크롤
- [ ] `@tanstack/vue-virtual` 설치
- [ ] `useVirtualList` composable
- [ ] `VirtualMessageList.vue` 구현
- [ ] ChatArea.vue 교체

### P1-5 오프라인 큐
- [ ] `useOfflineQueue` composable
- [ ] IndexedDB 스키마 설계
- [ ] Service Worker Background Sync 등록
- [ ] `OfflineBanner.vue`

### Rate Limiting
- [ ] Bucket4j 의존성 추가 (api-gateway)
- [ ] `RateLimitGlobalFilter` 구현
- [ ] 429 응답 처리 (프론트엔드)
