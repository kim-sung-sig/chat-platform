# Plan: typing-indicator

> **Feature**: 실시간 타이핑 인디케이터 (BE + FE 통합)
> **Date**: 2026-03-27
> **Phase**: plan
> **Scope**: chat-server (WebSocket) + chat-view (Nuxt 3)

---

## Executive Summary

| Perspective | Description |
|-------------|-------------|
| **Problem** | 채팅 중 상대방이 입력 중인지 알 수 없어 사용자 경험이 단절되고, "응답을 기다려야 하나?" 불확실성이 발생한다 |
| **Solution** | WebSocket을 통해 TYPING_START/TYPING_STOP 이벤트를 브로드캐스트하고, 프론트엔드에서 "User가 입력 중..." 인디케이터를 실시간 표시한다 |
| **Function UX Effect** | 메시지 입력창 위에 입력 중인 사용자 목록이 애니메이션으로 표시되어, Discord 수준의 실시간 커뮤니케이션 피드백을 제공한다 |
| **Core Value** | 최소한의 인프라 변경(Redis TTL + 기존 WebSocket 재활용)으로 사용자 체감 품질을 크게 향상시킨다 |

---

## 1. 기능 개요

### 1.1 동작 흐름

```
[FE] 사용자 입력 감지
  → debounce 300ms → TYPING_START WebSocket 전송
  → 3초 무활동 → TYPING_STOP WebSocket 전송

[BE] TYPING_START/TYPING_STOP 수신
  → Redis에 typing:{channelId}:{userId} key 저장 (TTL 5초)
  → 동일 채널 구독자에게 Redis Pub/Sub로 브로드캐스트

[FE] TYPING_START/TYPING_STOP 수신
  → useChatStore.typingUsers[channelId] 업데이트
  → TypingIndicator.vue 렌더링
```

### 1.2 메시지 타입 정의

```json
// TYPING_START
{
  "messageType": "TYPING_START",
  "channelId": "ch-123",
  "userId": "user-456"
}

// TYPING_STOP
{
  "messageType": "TYPING_STOP",
  "channelId": "ch-123",
  "userId": "user-456"
}
```

---

## 2. Spec Checklist

### BE (chat-server / websocket-server)

- [ ] **WebSocket 메시지 타입 확장**: `TYPING_START`, `TYPING_STOP` 타입 처리
- [ ] **Redis 상태 저장**: `typing:{channelId}:{userId}` key, TTL 5초
  - TYPING_START: `SET typing:{channelId}:{userId} 1 EX 5`
  - TYPING_STOP: `DEL typing:{channelId}:{userId}`
- [ ] **채널 브로드캐스트**: Redis Pub/Sub 채널(`chat:channel:{channelId}`)로 타이핑 이벤트 발행
- [ ] **자기 자신 제외**: 발신자 본인에게는 브로드캐스트 하지 않음
- [ ] **WebSocket 메시지 DTO**: `TypingEventMessage` (messageType, channelId, userId)
- [ ] **예외 처리**: 채널 미가입 사용자의 타이핑 이벤트 무시
- [ ] **도메인 이벤트**: `TypingStartedEvent`, `TypingStoppedEvent` (선택적)
- [ ] **단위 테스트**: TypingEventHandler — TYPING_START/STOP 시나리오

### FE (chat-view, Nuxt 3)

- [ ] **WebSocket 메시지 타입 핸들링**: `TYPING_START`, `TYPING_STOP` 분기 처리 (`ChatWebSocketService`)
- [ ] **Pinia 상태 추가**: `typingUsers: Record<channelId, Map<userId, timestamp>>` in `store/chat.ts`
- [ ] **타이핑 이벤트 전송**: `MessageInput.vue` → `@input` → debounce 300ms → `sendTypingEvent('TYPING_START')`
- [ ] **자동 STOP 전송**: 3초 무활동 후 `sendTypingEvent('TYPING_STOP')` (clearTimeout 패턴)
- [ ] **메시지 전송 시 STOP**: `handleSend()` 호출 시 즉시 `TYPING_STOP` 전송
- [ ] **TypingIndicator.vue 컴포넌트**: 입력 중인 사용자 표시 (최대 3명 + "외 N명")
- [ ] **ChatArea.vue 통합**: TypingIndicator를 MessageInput 위에 배치
- [ ] **클라이언트 TTL**: 수신 후 5초 경과 시 자동으로 typingUsers에서 제거
- [ ] **컴포넌트 해제 시 STOP**: `onUnmounted` → `TYPING_STOP` 전송

---

## 3. 아키텍처 설계

### 3.1 BE 아키텍처 (websocket-server)

```
websocket-server/
└── src/main/java/com/example/chat/
    └── websocket/
        ├── handler/
        │   └── ChatWebSocketHandler.java  (기존 — TYPING 분기 추가)
        ├── message/
        │   ├── InboundMessage.java        (기존 — messageType 필드 존재)
        │   └── TypingEventMessage.java    (신규 — outbound DTO)
        └── typing/
            └── TypingEventHandler.java    (신규)
```

**Redis 키 패턴:**
```
typing:{channelId}:{userId}  →  TTL 5s
```

**Redis Pub/Sub 채널 (기존 재활용):**
```
chat:channel:{channelId}
```

### 3.2 FE 아키텍처 (chat-view)

```
chat-view/
├── components/chat/
│   └── TypingIndicator.vue     (신규)
├── services/websocket/
│   └── chat-websocket.service.ts  (수정 — TYPING_START/STOP 핸들러 추가)
└── store/
    └── chat.ts                 (수정 — typingUsers 상태 + actions 추가)
```

---

## 4. 구현 우선순위

### Phase 1: BE 핵심 구현 (BE-P1-3)
1. `TypingEventMessage.java` — outbound DTO
2. `TypingEventHandler.java` — Redis SET/DEL + Pub/Sub 발행
3. `ChatWebSocketHandler.java` — TYPING_START/STOP 분기 추가
4. 단위 테스트 작성

### Phase 2: FE 핵심 구현 (FE-P1-3)
1. `chat-websocket.service.ts` — TYPING 메시지 타입 핸들러
2. `store/chat.ts` — typingUsers 상태 + sendTypingEvent action
3. `MessageInput.vue` — debounce 타이핑 이벤트 전송
4. `TypingIndicator.vue` — UI 컴포넌트

### Phase 3: 통합 검증
1. 로컬 환경에서 E2E 확인 (2개 브라우저 탭)
2. 자기 자신 메시지 미표시 확인
3. 3초 자동 소멸 확인

---

## 5. 기술 의사결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 타이핑 이벤트 전송 방식 | WebSocket (기존 연결 재활용) | 별도 HTTP 엔드포인트 불필요, 실시간성 보장 |
| BE 상태 저장소 | Redis TTL | 인메모리 빠른 접근, TTL로 자동 정리 |
| Debounce 시간 | 300ms | 타이핑 중 과도한 이벤트 방지, UX 반응성 유지 |
| 클라이언트 TTL | 5초 | 네트워크 지연으로 STOP 미수신 시 자동 정리 |
| 자기 자신 제외 | BE 처리 | FE에서도 필터링 가능하나 BE가 더 명확 |

---

## 6. 의존성 및 제약

- 기존 `ChatWebSocketService`의 `WebSocketMessage` 인터페이스 확장 필요
- websocket-server의 `ChatWebSocketHandler`에 TYPING 메시지 타입 분기 추가
- Redis는 이미 인프라에 구성되어 있음 (기존 Pub/Sub 활용)
- 테스트: Mockito 기반 단위 테스트 (Redis, WebSocket 모킹)

---

## 7. 완료 조건 (Done Gate)

- [ ] BE: `./gradlew :apps:chat:websocket-server:test` 통과
- [ ] BE: TYPING_START → Redis SET, TYPING_STOP → Redis DEL 동작 확인
- [ ] BE: 동일 채널 다른 사용자에게 브로드캐스트 확인
- [ ] FE: `TypingIndicator.vue` 렌더링 확인
- [ ] FE: 3초 자동 소멸 동작 확인
- [ ] Gap analysis ≥ 90%
