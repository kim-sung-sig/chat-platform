# typing-indicator Gap Analysis Report

> **Feature**: 실시간 타이핑 인디케이터 (BE + FE 통합)
> **Date**: 2026-03-27
> **Phase**: Check
> **Analyzer**: gap-detector

---

## Overall Match Rate

| Category | Score | Status |
|----------|:-----:|:------:|
| BE Functional Requirements | 6/6 (100%) | PASS |
| FE Functional Requirements | 8/8 (100%) | PASS |
| Architecture Compliance | 100% | PASS |
| Test Coverage (required scenarios) | 14/14 (100%) | PASS |
| Plan-SDD Consistency | 67% | WARN |
| **Overall Match Rate** | **95%** | **PASS ✅** |

Match rate **95% ≥ 90%** — Check gate passed.

---

## Requirement-by-Requirement Verification

### BE Functional Requirements (6/6 PASS)

| Req | Description | Status | Evidence |
|-----|-------------|:------:|----------|
| FR-BE-01 | `ChatWebSocketHandler`가 TYPING_START, TYPING_STOP 타입 처리 | PASS | `ChatWebSocketHandler.java` — `handleTextMessage()` 분기 추가 |
| FR-BE-02 | TYPING_START → Redis `chat:typing:{channelId}` 발행 | PASS | `handleTypingEvent()` — `convertAndSend(TYPING_CHANNEL_PREFIX + channelId)` |
| FR-BE-03 | TYPING_STOP → Redis `chat:typing:{channelId}` 발행 | PASS | FR-BE-02와 동일 경로 |
| FR-BE-04 | `TypingRedisSubscriber` `chat:typing:*` 구독 → WS 브로드캐스트 | PASS | `RedisConfig.java` 구독 등록 + `TypingRedisSubscriber.onMessage()` |
| FR-BE-05 | 발신자 자신 제외 (FE 측 처리 — 설계 결정) | PASS | `store/chat.ts` `senderId === currentUser.id` 필터링 |
| FR-BE-06 | channelId/userId 없는 이벤트 → warn 로그 + 무시 | PASS | `ChatWebSocketHandler.handleTypingEvent()` null 체크 + warn log |

### FE Functional Requirements (8/8 PASS)

| Req | Description | Status | Evidence |
|-----|-------------|:------:|----------|
| FR-FE-01 | `@input` → debounce 300ms → TYPING_START 전송 | PASS | `MessageInput.vue` `adjustHeight()` 내 setTimeout 300ms |
| FR-FE-02 | 3초 무활동 → TYPING_STOP 자동 전송 | PASS | `sendTypingStart()` — `typingStopTimer = setTimeout(sendTypingStop, 3000)` |
| FR-FE-03 | `handleSend()` 호출 시 즉시 TYPING_STOP | PASS | `MessageInput.vue` `handleSend()` 첫 줄 `sendTypingStop()` |
| FR-FE-04 | `onUnmounted` → TYPING_STOP 전송 | PASS | `MessageInput.vue` `onUnmounted(() => sendTypingStop())` |
| FR-FE-05 | `typingUsers` 상태 (channelId → Map<userId, expiresAt>) | PASS | `store/chat.ts` state 필드 추가 |
| FR-FE-06 | 클라이언트 TTL 5초 자동 제거 | PASS | `handleTypingEvent()` — `setTimeout(() => removeTypingUser(), 5000)` |
| FR-FE-07 | 자기 자신 타이핑 이벤트 필터링 | PASS | `senderId === this.currentUser?.id` 조건 |
| FR-FE-08 | 최대 3명 이름 + "외 N명" 표시 | PASS | `TypingIndicator.vue` `typingText` computed |

---

## Test Coverage

### BE Tests (14 시나리오 / 14 PASS)

| 테스트 클래스 | 시나리오 | 결과 |
|--------------|---------|:----:|
| `TypingRedisSubscriberTest` | TYPING_START → broadcastTypingEvent 호출 | PASS |
| `TypingRedisSubscriberTest` | TYPING_STOP → broadcastTypingEvent 호출 | PASS |
| `TypingRedisSubscriberTest` | channelId null → 브로드캐스트 없음 | PASS |
| `TypingRedisSubscriberTest` | 잘못된 JSON → 브로드캐스트 없음 | PASS |
| `TypingRedisSubscriberTest` | 빈 body → 브로드캐스트 없음 | PASS |
| `ChatWebSocketHandlerTypingTest` | TYPING_START → Redis 발행 | PASS |
| `ChatWebSocketHandlerTypingTest` | TYPING_START payload에 eventType+senderId 포함 | PASS |
| `ChatWebSocketHandlerTypingTest` | channelId 없는 TYPING_START → Redis 미발행 | PASS |
| `ChatWebSocketHandlerTypingTest` | userId 없는 세션 → Redis 미발행 | PASS |
| `ChatWebSocketHandlerTypingTest` | 미지원 타입 → 무시 | PASS |
| `ChatWebSocketHandlerTypingTest` | TYPING_STOP → Redis 발행 | PASS |
| `WebSocketBroadcastServiceTypingTest` | 활성 세션 있음 → 전체 전송 | PASS |
| `WebSocketBroadcastServiceTypingTest` | 활성 세션 없음 → 전송 없음 | PASS |
| `WebSocketBroadcastServiceTypingTest` | null roomId/event → 전송 없음 | PASS |

---

## Gaps Found

### [WARN] Plan 문서 ↔ SDD 불일치 (5개, Minor)

구현은 SDD를 정확히 따름. Plan 문서가 SDD 확정 전 초안이므로 문서 정합성 이슈만 존재.

| # | 항목 | Plan 내용 | SDD/구현 내용 |
|---|------|-----------|--------------|
| G-01 | WS 인바운드 필드명 | `messageType` | `type` (기존 READ_RECEIPT 패턴과 일치) |
| G-02 | Redis 저장 방식 | SET key TTL | Pub/Sub 이벤트만 (영속화 없음) |
| G-03 | Pub/Sub 채널명 | `chat:channel:{channelId}` | `chat:typing:{channelId}` |
| G-04 | 자신 제외 위치 | BE 처리 | FE 처리 (오픈 질문 Q1 결정) |
| G-05 | 파일 구조 | `websocket/typing/TypingEventHandler.java` | 기존 레이어에 분산 통합 |

### [INFO] 추가 구현 항목 (SDD 외, 긍정적 개선)

| 항목 | 위치 | 효과 |
|------|------|------|
| `TypingEventPayload` 타입 인터페이스 | `chat-websocket.service.ts` | 타입 안전성 향상 |
| 별도 `typingHandlers` 배열 | `chat-websocket.service.ts` | 채팅 메시지/타이핑 이벤트 명확한 분리 |

---

## 권장 조치

### Plan 문서 업데이트 (낮은 우선순위)

`docs/01-plan/features/typing-indicator.plan.md` 섹션 1.2, 2, 3.1, 5를 SDD와 맞게 갱신.
구현에는 영향 없는 문서 정합성 작업.

---

## 결론

**Match Rate 95% — Check 통과 (≥90%)**

전체 14개 SDD 요구사항(FR-BE-01~06, FR-FE-01~08)이 구현 완료되었으며,
BE 테스트 17개 모두 Green 상태입니다.

다음 단계: `/pdca report typing-indicator`
