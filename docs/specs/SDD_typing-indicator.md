# SDD: Typing Indicator

## 1. Title / Version / Status / Owners
- **Title**: 실시간 타이핑 인디케이터 (BE + FE 통합)
- **Version**: 1.0
- **Status**: Approved
- **Owners**: Backend (websocket-server), Frontend (chat-view)
- **Related Docs**:
  - Plan: `docs/01-plan/features/typing-indicator.plan.md`
  - Skeleton: `apps/chat/websocket-server/src/main/java/com/example/chat/websocket/typing/`
  - FE Skeleton: `chat-view/components/chat/TypingIndicator.vue`

---

## 2. Problem Statement

**무엇을 해결하는가?**
채팅 중 상대방이 입력하고 있는지 알 수 없어 사용자 경험이 단절된다.
"응답을 기다려야 하나?" 라는 불확실성이 커뮤니케이션 효율을 낮춘다.

**왜 지금인가?**
파일 업로드(BE-P1-2) 완료 후 실시간 피드백 기능이 최우선 개선 항목.
기존 WebSocket + Redis Pub/Sub 인프라를 그대로 재활용하므로 비용 최소.

---

## 3. Goals / Non-Goals

### Goals
- 사용자가 입력창에 타이핑 시 동일 채널의 다른 사용자에게 "X가 입력 중..." 표시
- 3초 무활동 후 자동 인디케이터 소멸
- 메시지 전송 완료 시 즉시 인디케이터 소멸
- 최대 3명 이름 표시 + "외 N명" 표기

### Non-Goals
- HTTP 폴링 방식 (WebSocket 전용)
- 타이핑 내용 미리보기
- 타이핑 이력 저장 (영속성 없음 — Redis TTL만 사용)
- push-service 알림 연동

---

## 4. Stakeholders / Target Users

- **채팅 사용자**: 상대방 입력 여부를 실시간으로 확인
- **개발팀**: 기존 WebSocket/Redis 아키텍처 확장 — 새 인프라 불필요

---

## 5. Requirements

### Functional Requirements

**BE:**
- FR-BE-01: `ChatWebSocketHandler`가 `TYPING_START`, `TYPING_STOP` 메시지 타입을 처리한다
- FR-BE-02: TYPING_START 수신 시 Redis `chat:typing:{channelId}`에 `TypingEvent` 발행
- FR-BE-03: TYPING_STOP 수신 시 Redis `chat:typing:{channelId}`에 `TypingEvent` 발행
- FR-BE-04: `TypingRedisSubscriber`가 `chat:typing:*` 패턴 구독 후 동일 채널 세션에 WebSocket 브로드캐스트
- FR-BE-05: 발신자 본인은 타이핑 이벤트를 수신하지 않는다 (FE 측 필터링)
- FR-BE-06: 채널 미가입 사용자의 타이핑 이벤트는 무시한다 (세션 없으면 자동 처리됨)

**FE:**
- FR-FE-01: `MessageInput.vue` 입력 감지 → debounce 300ms 후 `TYPING_START` WS 전송
- FR-FE-02: 3초 무활동 후 자동으로 `TYPING_STOP` WS 전송
- FR-FE-03: 메시지 전송(`handleSend`) 시 즉시 `TYPING_STOP` 전송
- FR-FE-04: 컴포넌트 `onUnmounted` 시 `TYPING_STOP` 전송
- FR-FE-05: `store/chat.ts`의 `typingUsers`에 채널별 타이핑 사용자 Map 관리
- FR-FE-06: 수신 후 5초 경과 시 클라이언트 TTL로 자동 제거
- FR-FE-07: 자신의 `userId`와 동일한 타이핑 이벤트는 무시
- FR-FE-08: `TypingIndicator.vue`가 최대 3명 이름 + "외 N명" 표시

### Non-Functional Requirements
- NFR-01: 타이핑 이벤트 전달 지연 ≤ 200ms (LAN 기준)
- NFR-02: debounce로 초당 타이핑 이벤트 ≤ 3회 제한
- NFR-03: Redis TTL 5초로 stale 상태 자동 정리 보장

---

## 6. Domain Knowledge

**Glossary:**
- `TYPING_START`: 사용자가 입력을 시작했음을 알리는 WS 이벤트 타입
- `TYPING_STOP`: 사용자가 입력을 중단했음을 알리는 WS 이벤트 타입 (전송 완료 or 3초 무활동)
- `TypingEvent`: Redis Pub/Sub으로 전달되는 타이핑 상태 DTO
- `typingUsers`: FE Pinia store의 채널별 타이핑 사용자 Map
- `debounce`: 연속 이벤트를 N ms 지연 후 한 번만 처리하는 패턴

**Invariants:**
- 타이핑 이벤트는 영속화하지 않는다 (Redis volatile key만 사용)
- 자기 자신의 타이핑 이벤트는 FE에서 필터링한다 (`senderId === currentUser.id`)
- Redis TTL(5s)이 지나면 BE 상태는 자동 소멸 (STOP 미수신 보호)

**Domain Rules:**
- 타이핑 이벤트는 기존 `chat:room:*` 채널과 분리된 `chat:typing:*` 채널 사용
- `MessageEvent` (채팅 메시지 DTO)와 `TypingEvent`는 별도 타입 유지

---

## 7. Domain Model & Boundaries

### Bounded Context
- **websocket-server** (BE): 타이핑 이벤트 수신 → Redis 발행 → WS 브로드캐스트
- **chat-view** (FE): 타이핑 이벤트 전송 + 수신 + UI 렌더링

### 새로운 클래스 (BE — websocket-server)

```
websocket-server/
└── src/main/java/com/example/chat/websocket/
    ├── presentation/handler/
    │   └── ChatWebSocketHandler.java    ← TYPING 분기 추가 (기존 수정)
    ├── infrastructure/redis/
    │   ├── TypingEvent.java             ← 신규: Redis 발행/수신 DTO
    │   └── TypingRedisSubscriber.java   ← 신규: chat:typing:* 구독
    ├── application/service/
    │   └── WebSocketBroadcastService.java ← broadcastTypingEvent 추가 (기존 수정)
    └── config/
        └── RedisConfig.java            ← chat:typing:* 구독 추가 (기존 수정)
```

### 새로운 파일 (FE — chat-view)

```
chat-view/
├── components/chat/
│   └── TypingIndicator.vue             ← 신규
├── services/websocket/
│   └── chat-websocket.service.ts       ← TYPING 핸들러 추가 (기존 수정)
└── store/
    └── chat.ts                         ← typingUsers 상태 + actions 추가 (기존 수정)
```

### Key Entities

**TypingEvent (BE — Redis DTO):**
```java
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class TypingEvent {
    private String eventType;   // "TYPING_START" | "TYPING_STOP"
    private String channelId;
    private String senderId;    // userId (String)
}
```

**typingUsers (FE — Pinia state):**
```typescript
typingUsers: Record<string, Map<string, number>>
// channelId → Map<userId, expiresAt(timestamp)>
```

---

## 8. Interfaces

### BE: 수신 메시지 (FE → BE via WebSocket)

```json
// TYPING_START 전송
{
  "type": "TYPING_START",
  "channelId": "ch-uuid-123"
}

// TYPING_STOP 전송
{
  "type": "TYPING_STOP",
  "channelId": "ch-uuid-123"
}
```

> `type` 필드 사용 (기존 `READ_RECEIPT`와 동일한 패턴, `messageType` 아님)

### BE: 발신 메시지 (BE → FE via WebSocket)

```json
{
  "eventType": "TYPING_START",
  "channelId": "ch-uuid-123",
  "senderId": "user-uuid-456"
}
```

### Redis Pub/Sub 채널

```
# 기존 (변경 없음)
chat:room:{channelId}      — 일반 메시지
chat:read:event:{channelId} — 읽음 이벤트

# 신규
chat:typing:{channelId}    — 타이핑 이벤트
```

### FE: WebSocket 메시지 타입 확장

```typescript
export interface WebSocketMessage {
  // 기존
  messageId?: string;
  channelId: string;
  userId?: string;
  messageType?: string;
  textContent?: string;
  imageUrls?: string[];
  sentAt?: string;

  // 신규 (타이핑 이벤트)
  eventType?: 'TYPING_START' | 'TYPING_STOP';
  senderId?: string;
}
```

---

## 9. Data Model

### Redis (영속화 없음, 휘발성)

```
Key: (사용 안 함 — SET/DEL 방식 사용 안 함)
Channel: chat:typing:{channelId}  →  TypingEvent JSON 발행
```

> 타이핑 상태를 Redis Key로 저장하는 대신 Pub/Sub 이벤트만 사용.
> FE 클라이언트 TTL(5s)로 stale 상태 처리.

---

## 10. Workflow / State Transitions

### BE 처리 흐름

```
[FE] WS 전송: {"type":"TYPING_START","channelId":"ch-123"}
    ↓
[BE] ChatWebSocketHandler.handleTextMessage()
    ↓ type == "TYPING_START" or "TYPING_STOP"
[BE] handleTypingEvent(session, node)
    ↓ userId = extractUserIdAsString(session)
[BE] redisTemplate.convertAndSend("chat:typing:ch-123", typingEventJson)
    ↓
[BE] TypingRedisSubscriber.onMessage() (다른 websocket-server 인스턴스 포함)
    ↓ deserialize TypingEvent
[BE] WebSocketBroadcastService.broadcastTypingEvent("ch-123", typingEvent)
    ↓ sessionManager.getActiveSessionsByRoom("ch-123")
[FE] 모든 연결된 세션으로 WS 전송
```

### FE 타이핑 이벤트 전송 흐름

```
[사용자] 입력창 타이핑
    ↓ @input 이벤트
[MessageInput.vue] debounce 300ms 후 sendTypingStart()
    ↓
[ChatWebSocketService] ws.send({type:"TYPING_START", channelId})
    ↓ clearTimeout(stopTimer)
    ↓ stopTimer = setTimeout(sendTypingStop, 3000)

[사용자] 메시지 전송 (Enter)
    ↓ handleSend()
[MessageInput.vue] sendTypingStop() 즉시 호출 + clearTimeout(stopTimer)
```

### FE 타이핑 이벤트 수신 흐름

```
[WS] {"eventType":"TYPING_START","channelId":"ch-123","senderId":"user-456"}
    ↓
[ChatWebSocketService.onMessage] eventType 분기
    ↓ senderId !== currentUserId
[useChatStore.setTypingUser("ch-123","user-456")]
    ↓ typingUsers["ch-123"].set("user-456", Date.now() + 5000)
    ↓ setTimeout(() => removeTypingUser(), 5000)

[TypingIndicator.vue] computed: typingUsers[activeChannelId]
    ↓ 이름 목록 표시 (최대 3명 + "외 N명")
```

---

## 11. Validation Rules & Edge Cases

| Rule | Handling |
|------|---------|
| `channelId` 없는 타이핑 이벤트 | BE에서 warn 로그 후 무시 |
| `userId` 없는 세션의 타이핑 이벤트 | BE에서 warn 로그 후 무시 |
| WS 연결 없이 타이핑 이벤트 전송 시도 | FE: `isConnected()` 확인 후 skip |
| STOP 이벤트 미수신 (네트워크 단절) | FE 클라이언트 TTL 5초 자동 제거 |
| 동일 사용자 TYPING_START 중복 수신 | FE: Map overwrite (expiresAt 갱신) |
| 자기 자신의 타이핑 이벤트 수신 | FE: `senderId === store.currentUser?.id` → skip |
| 3명 초과 동시 타이핑 | FE: 이름 3개 + "외 N명" 표시 |

---

## 12. Security / Privacy / Compliance

- **AuthZ**: WebSocket 연결 시 이미 JWT 검증 완료 (`AuthChannelInterceptor`)
- **타이핑 내용 비공개**: 이벤트에는 userId만 포함, 입력 내용 없음
- **채널 격리**: `chat:typing:{channelId}`로 채널별 독립 Pub/Sub
- **데이터 보존 없음**: Redis Pub/Sub 이벤트는 영속화하지 않음

---

## 13. Observability

**Logs:**
```
DEBUG  ChatWebSocketHandler    - Typing event received: type={}, userId={}, channelId={}
DEBUG  TypingRedisSubscriber   - Broadcasting typing event: eventType={}, channelId={}, senderId={}
WARN   ChatWebSocketHandler    - Typing event missing channelId or userId
```

**Metrics (선택적):**
- `typing.events.received` (counter, tags: type=TYPING_START|TYPING_STOP)

---

## 14. Test Strategy

### BE 단위 테스트 (`TypingEventHandlerTest`)

| 시나리오 | 기대값 |
|---------|--------|
| TYPING_START 수신 → Redis 발행 | `redisTemplate.convertAndSend("chat:typing:ch-123", json)` 호출 |
| TYPING_STOP 수신 → Redis 발행 | `redisTemplate.convertAndSend("chat:typing:ch-123", json)` 호출 |
| channelId 없는 이벤트 | Redis 발행 안 함, warn 로그 |
| userId 없는 세션 | Redis 발행 안 함, warn 로그 |

### BE 단위 테스트 (`TypingRedisSubscriberTest`)

| 시나리오 | 기대값 |
|---------|--------|
| TYPING_START 수신 → WS 브로드캐스트 | `broadcastService.broadcastTypingEvent()` 호출 |
| 역직렬화 실패 | 브로드캐스트 없음, warn 로그 |

### FE 동작 검증 (수동 E2E)

| 시나리오 | 기대값 |
|---------|--------|
| 탭 A에서 타이핑 시 탭 B에 인디케이터 표시 | ✅ |
| 탭 A에서 전송 시 탭 B 인디케이터 소멸 | ✅ |
| 타이핑 후 3초 경과 시 인디케이터 소멸 | ✅ |
| 자기 자신의 타이핑은 표시 안 됨 | ✅ |

---

## 15. Risks & Assumptions

| Risk | Mitigation |
|------|-----------|
| WS 연결 끊김으로 STOP 미전송 | FE 클라이언트 TTL 5초 |
| 다수 사용자 동시 타이핑 시 이벤트 폭주 | debounce 300ms, FE TTL 갱신으로 중복 제거 |
| Redis Pub/Sub 단건 전달 보장 없음 | 타이핑 이벤트는 best-effort 허용 |

**Assumptions:**
- websocket-server가 단일 인스턴스이거나 Redis Pub/Sub로 수평 확장 가능
- FE `store.currentUser.id`가 항상 설정되어 있음

---

## 16. Open Questions

- Q1: 타이핑 이벤트 발신자 자신 제외를 BE에서 처리할지 FE에서 처리할지?
  → **결정**: FE 처리 (`senderId === currentUser.id` 필터링) — BE 변경 최소화
- Q2: `TypingEvent`를 기존 `MessageEvent`에 통합할지 별도 DTO로 분리할지?
  → **결정**: 별도 DTO (`TypingEvent`) — 관심사 분리, 기존 MessageEvent 오염 방지

---

## 17. Traceability

| Requirement | Test |
|------------|------|
| FR-BE-02 (TYPING_START → Redis) | `TypingEventHandlerTest#typing_start_publishes_to_redis` |
| FR-BE-03 (TYPING_STOP → Redis) | `TypingEventHandlerTest#typing_stop_publishes_to_redis` |
| FR-BE-04 (Redis → WS broadcast) | `TypingRedisSubscriberTest#typing_event_broadcasts_to_room` |
| FR-FE-01 (debounce 300ms) | MessageInput 수동 테스트 |
| FR-FE-02 (3초 자동 STOP) | MessageInput 수동 테스트 |
| FR-FE-08 (최대 3명 + 외 N명) | TypingIndicator.vue 단위 확인 |

---

## 18. References

- 기존 구현: `ChatWebSocketHandler.java`, `RedisConfig.java`, `WebSocketBroadcastService.java`
- 패턴 참조: `ReadReceiptRedisSubscriber.java` (신규 Redis 구독 패턴)
- FE 참조: `chat-websocket.service.ts`, `store/chat.ts`, `MessageInput.vue`
- Plan: `docs/01-plan/features/typing-indicator.plan.md`
