



# Design: chat-improve

> **Feature**: 채팅 애플리케이션 전면 고도화
> **Date**: 2026-03-24
> **Phase**: design
> **Plan**: [chat-improve.plan.md](../../01-plan/features/chat-improve.plan.md)
> **SDD**: [SDD_chat-improve.md](../../specs/SDD_chat-improve.md)

---

## 1. 설계 범위

이 설계는 Plan의 **P0 + P1** 항목을 우선 구현 대상으로 한다.
P2/P3는 별도 SDD로 분리한다.

| 그룹 | 항목 | 우선순위 |
|------|------|----------|
| 컨벤션 수정 | PushMessage @Setter 제거, @CurrentUser 통일, mock 스토어 분리 | P0 |
| 보안 | Rate Limiting, WebSocket 토큰 보안 | P0 |
| 품질 | 글로벌 ExceptionHandler, parseUserFromToken 수정 | P0 |
| 예약 발송 | 백엔드 Quartz Job + API, 프론트엔드 UI | P1 |
| 파일 업로드 | 백엔드 S3 API + 프론트엔드 진행률 UI | P1 |
| 타이핑 인디케이터 | WebSocket endpoint + 프론트엔드 표시 | P1 |
| 가상 스크롤 | vue-virtual-scroller 도입 | P1 |
| 오프라인 큐 | Service Worker Background Sync | P1 |

---

## 2. DDD 레이어 설계

### 2.1 예약 발송 (scheduled-message)

```
apps/chat/chat-server/src/main/java/com/example/chat/
└── scheduled/
    ├── domain/
    │   ├── model/
    │   │   ├── ScheduledMessage.java          # Aggregate Root
    │   │   ├── ScheduleType.java              # Value Object (ONCE | RECURRING)
    │   │   └── ScheduleStatus.java            # Value Object (PENDING | EXECUTED | CANCELLED)
    │   ├── service/
    │   │   └── ScheduledMessageDomainService.java
    │   └── repository/
    │       └── ScheduledMessageRepository.java  # Port (interface)
    ├── application/
    │   ├── service/
    │   │   ├── ScheduledMessageCommandService.java  # 예약 생성/취소
    │   │   └── ScheduledMessageQueryService.java    # 예약 목록 조회
    │   └── job/
    │       └── ScheduledMessageJob.java             # Quartz Job 구현
    ├── event/
    │   ├── MessageScheduledEvent.java
    │   └── ScheduledMessageExecutedEvent.java
    ├── infrastructure/
    │   ├── datasource/
    │   │   └── JpaScheduledMessageRepository.java
    │   └── quartz/
    │       └── QuartzJobScheduler.java
    └── api/
        ├── controller/
        │   └── ScheduledMessageController.java
        └── request/
        │   ├── CreateScheduledMessageRequest.java
        │   └── UpdateScheduledMessageRequest.java
        └── response/
            └── ScheduledMessageResponse.java
```

### 2.2 파일 업로드 (file-upload)

```
apps/chat/chat-server/src/main/java/com/example/chat/
└── file/
    ├── domain/
    │   ├── model/
    │   │   ├── UploadedFile.java               # Value Object (파일 메타데이터)
    │   │   └── FileType.java                   # Value Object (IMAGE | VIDEO | DOCUMENT | OTHER)
    │   └── service/
    │       └── FileStoragePort.java            # Port (interface)
    ├── application/
    │   └── service/
    │       └── FileUploadService.java          # Command (단일 서비스, Command/Query 통합)
    ├── infrastructure/
    │   └── s3/
    │       └── S3FileStorageAdapter.java       # FileStoragePort 구현
    └── api/
        ├── controller/
        │   └── FileUploadController.java
        └── response/
            └── FileUploadResponse.java
```

### 2.3 타이핑 인디케이터 (typing-indicator)

```
apps/chat/websocket-server/src/main/java/com/example/chat/
└── typing/
    ├── domain/
    │   └── model/
    │       └── TypingSession.java              # Value Object (userId, channelId, expiresAt)
    ├── application/
    │   └── service/
    │       ├── TypingCommandService.java       # 타이핑 시작/종료
    │       └── TypingQueryService.java         # 현재 타이핑 중인 사용자 조회
    ├── infrastructure/
    │   └── redis/
    │       └── RedisTypingRepository.java      # TTL=3s로 자동 만료
    └── presentation/
        └── TypingWebSocketController.java
```

### 2.4 Rate Limiting (api-gateway 레벨)

```
infrastructure/api-gateway/src/main/java/com/example/chat/gateway/
└── filter/
    └── RateLimitGlobalFilter.java              # Bucket4j + Redis
```

### 2.5 Audit Logging (공통 모듈)

```
common/core/src/main/java/com/example/chat/common/
└── audit/
    ├── AuditLog.java                           # Entity
    ├── AuditableOperation.java                 # @Annotation
    └── AuditAspect.java                        # AOP Aspect
```

---

## 3. API 계약 설계

### 3.1 예약 발송 API

```
POST   /api/messages/schedule           # 예약 발송 생성
GET    /api/messages/schedule/{channelId}  # 채널의 예약 목록
DELETE /api/messages/schedule/{id}      # 예약 취소
```

**POST /api/messages/schedule**
```json
// Request
{
  "channelId": "uuid",
  "content": {
    "type": "TEXT",
    "text": "예약된 메시지 내용"
  },
  "scheduledAt": "2026-03-25T09:00:00+09:00"
}

// Response 201
{
  "id": "uuid",
  "channelId": "uuid",
  "senderId": "uuid",
  "content": { "type": "TEXT", "text": "..." },
  "scheduledAt": "2026-03-25T09:00:00+09:00",
  "status": "PENDING",
  "createdAt": "2026-03-24T10:00:00+09:00"
}
```

**에러 케이스**:
- `400` scheduledAt이 현재 시각 이전
- `400` scheduledAt이 30일 초과
- `403` 채널 멤버가 아닌 경우
- `429` Rate Limit 초과 (채널당 10개/일)

### 3.2 파일 업로드 API

```
POST   /api/files/upload               # 파일 업로드
```

**POST /api/files/upload** (multipart/form-data)
```
field: file (binary)
field: channelId (string)
```

```json
// Response 201
{
  "fileUrl": "https://cdn.example.com/files/uuid/filename.jpg",
  "fileName": "filename.jpg",
  "fileSize": 204800,
  "mimeType": "image/jpeg"
}
```

**에러 케이스**:
- `400` 파일 없음
- `413` 파일 크기 초과 (이미지: 10MB, 문서: 50MB)
- `415` 허용되지 않는 파일 타입
- `503` S3 업로드 실패

### 3.3 타이핑 인디케이터 WebSocket

**endpoint**: STOMP `/ws/messages/{channelId}` 기존 채널 활용

```json
// Client → Server (타이핑 시작)
{
  "type": "TYPING_START",
  "channelId": "uuid"
}

// Server → Clients (브로드캐스트)
{
  "type": "TYPING",
  "userId": "uuid",
  "username": "홍길동",
  "channelId": "uuid",
  "action": "START"  // START | STOP
}
```

---

## 4. 데이터베이스 변경사항

### 4.1 신규 테이블 (Flyway V10: audit_log)

```sql
CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    VARCHAR(36) NOT NULL,
    operation   VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(36),
    detail      JSONB,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_audit_actor ON audit_log(actor_id);
CREATE INDEX idx_audit_operation ON audit_log(operation, created_at DESC);
```

### 4.2 schedule_rules 테이블 활용 (기존 V5)

기존 스키마 그대로 사용. Domain model만 추가:
- `schedule_type`: ONCE (단순 예약)
- `schedule_status`: PENDING → EXECUTED | CANCELLED
- `scheduled_at`: 실행 예정 시각 (인덱스 존재)

### 4.3 신규 테이블 (Flyway V11: message_reactions - P3)

```sql
-- P3 단계에서 추가 (현재 설계만 포함)
CREATE TABLE message_reactions (
    id          BIGSERIAL PRIMARY KEY,
    message_id  VARCHAR(36) NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    emoji       VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (message_id, user_id, emoji)
);
```

---

## 5. 컨벤션 수정 설계 (P0)

### 5.1 PushMessage @Setter 제거

```java
// AS-IS (위반)
@Setter
private PushStatus status;

// TO-BE (준수)
public void updateStatus(PushStatus newStatus) {
    this.status = newStatus;
}
```

### 5.2 FriendshipController @CurrentUser 통일

```java
// AS-IS (위반)
@RequestHeader("X-User-Id") String userId

// TO-BE (준수)
@CurrentUser String userId
```

### 5.3 글로벌 ExceptionHandler

```java
// common/web 모듈에 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(ChatException e) { ... }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) { ... }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) { ... }
}
```

---

## 6. 프론트엔드 설계 (chat-view)

### 6.1 Store 구조 개선

```
store/
├── auth.ts           # JWT + User (변경 없음, parseUserFromToken 수정)
├── chat.ts           # 완성: 실 API 연동 전용 (mock 제거)
├── ui.ts             # UI 상태 (변경 없음)
├── voice.ts          # Voice (변경 없음)
└── data.ts           # [DEPRECATED] 개발 전용 mock (flag으로 비활성화)
```

### 6.2 신규 Composable

```typescript
// composables/useTypingIndicator.ts
export function useTypingIndicator(channelId: Ref<string>) {
  const typingUsers = ref<{ userId: string; username: string }[]>([])

  // WebSocket으로 TYPING 이벤트 수신
  // 3초 후 자동 제거

  const startTyping = () => { /* TYPING_START 전송 */ }
  const stopTyping = () => { /* TYPING_STOP 전송 */ }

  return { typingUsers, startTyping, stopTyping }
}

// composables/useOfflineQueue.ts
export function useOfflineQueue() {
  // IndexedDB에 미전송 메시지 저장
  // 온라인 복귀 시 자동 재전송
  const queueMessage = async (msg: PendingMessage) => { ... }
  const flushQueue = async () => { ... }
  return { queueMessage, flushQueue }
}

// composables/useVirtualList.ts
export function useVirtualList(items: Ref<Message[]>, itemHeight = 60) {
  // @tanstack/vue-virtual 래핑
  // 동적 높이 지원
  return { virtualItems, totalHeight, containerRef }
}
```

### 6.3 신규 컴포넌트

```
components/
├── ScheduledMessageModal.vue     # 예약 발송 날짜/시간 선택
├── FileUploadProgress.vue        # 업로드 진행률 표시
├── TypingIndicator.vue           # "홍길동님이 입력 중..."
├── ToastNotification.vue         # 성공/에러/경고 토스트
├── OfflineBanner.vue             # 오프라인 상태 배너
└── VirtualMessageList.vue        # 가상 스크롤 메시지 목록 (ChatArea 대체)
```

### 6.4 신규 Service

```typescript
// services/api/scheduled-message.service.ts
export class ScheduledMessageService {
  async create(req: CreateScheduledMessageRequest): Promise<ScheduledMessageResponse>
  async list(channelId: string): Promise<ScheduledMessageResponse[]>
  async cancel(id: string): Promise<void>
}

// services/api/file.service.ts
export class FileService {
  async upload(file: File, channelId: string, onProgress?: (pct: number) => void): Promise<FileUploadResponse>
}
```

---

## 7. Rate Limiting 설계

### 7.1 API Gateway 레벨 (Bucket4j + Redis)

```yaml
# application.yml (api-gateway)
rate-limit:
  rules:
    - path: "/api/messages"
      method: POST
      limit: 10        # 10 req/sec per user
      capacity: 20     # burst capacity
    - path: "/api/friendship/request"
      method: POST
      limit: 5         # 5 req/min per user
    - path: "/api/files/upload"
      method: POST
      limit: 5         # 5 req/min per user
```

### 7.2 에러 응답

```json
// 429 Too Many Requests
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
  "retryAfter": 1000  // ms
}
```

---

## 8. WebSocket 토큰 보안 개선

### 현재 (취약)
```
WS 연결: ws://host/ws/chat?roomId=xxx&token=JWT_TOKEN
```

### 개선 (STOMP CONNECT 프레임 활용)
```javascript
// 프론트엔드
const stompClient = new Client({
  webSocketFactory: () => new WebSocket('ws://host/ws/chat'),
  connectHeaders: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Channel-Id': channelId
  }
})
```

```java
// 백엔드 StompChannelInterceptor
@Override
public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
        String token = accessor.getFirstNativeHeader("Authorization");
        // JWT 검증 후 principal 설정
    }
}
```

---

## 9. 구현 순서 (Do 단계 가이드)

### Phase 1 — P0 즉시 수정 (1~2일)
1. `PushMessage.updateStatus()` 메서드 추가, `@Setter` 제거
2. `FriendshipController` → `@CurrentUser` 교체
3. `GlobalExceptionHandler` 구현 (common/web 모듈)
4. `parseUserFromToken()` 수정 (chat-view)
5. `store/data.ts` mock init을 `isDev` 플래그로 분리

### Phase 2 — 예약 발송 (3~4일)
1. SDD: `/sdd-requirements scheduled-message`
2. Skeleton: `/spec-to-skeleton scheduled-message`
3. Tests: `/skeleton-to-tests scheduled-message`
4. 구현: ScheduledMessage domain → application → infrastructure → api
5. Quartz Job 등록 및 테스트

### Phase 3 — 파일 업로드 (2~3일)
1. S3 클라이언트 설정 (또는 MinIO 로컬 테스트)
2. `FileUploadController` + `FileUploadService` 구현
3. 프론트엔드: `FileService` + `FileUploadProgress.vue`

### Phase 4 — 타이핑 인디케이터 (1일)
1. WebSocket 메시지 타입에 `TYPING` 추가
2. `RedisTypingRepository` (TTL=3s)
3. 프론트엔드: `useTypingIndicator` + `TypingIndicator.vue`

### Phase 5 — 가상 스크롤 + 오프라인 큐 (2~3일)
1. `@tanstack/vue-virtual` 설치
2. `VirtualMessageList.vue` 구현
3. Service Worker Background Sync + IndexedDB

### Phase 6 — Rate Limiting (1~2일)
1. `spring-cloud-gateway-rate-limiter` 또는 Bucket4j 설정
2. Redis 기반 슬라이딩 윈도우 구현
3. 429 응답 프론트엔드 처리

---

## 10. 완료 기준 (Done Gate)

- [ ] `./gradlew compileJava compileTestJava --no-daemon` 통과
- [ ] 신규 CommandService/QueryService: 단위 테스트 최소 80%
- [ ] 모든 신규 Controller: `@CurrentUser` 사용
- [ ] 모든 신규 Response: Java record + `from()` factory method
- [ ] Flyway 마이그레이션 정상 실행 확인
- [ ] chat-view: `parseUserFromToken()` 에러 없음
- [ ] chat-view: Lighthouse Performance 점수 ≥ 70 (가상 스크롤 적용 후)
