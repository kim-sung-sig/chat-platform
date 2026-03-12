# 읽음 기능 설계 계획 (KakaoTalk-style Read Receipt)

> 대규모 채팅 서비스를 목표로 하며, 1대1/그룹 채팅 모두에서 실시간 읽음 처리를
> 고성능으로 제공하는 것을 목표로 한다.

## 현황 진단

| 항목 | 현재 상태 | 문제점 |
|------|-----------|--------|
| `ChatMessageEntity.readAt` | 정의만 있음, 항상 NULL | 1대1 전용 설계, 그룹 미지원 |
| `ChatChannelMetadataEntity.unreadCount` | 정의만 있음, 항상 0 | `incrementUnreadCount()` 미호출 |
| WebSocket read receipt | 주석으로만 언급 | 클라이언트→서버 방향 차단됨 |
| Redis unread 캐시 | 없음 | 매 조회마다 PostgreSQL 직접 질의 |
| Kafka 푸시 알림 | Producer 정의만 있음 | `KafkaMessageProducer` 실제 미호출 |

## 핵심 설계 방향

### 전략: Last-Read Cursor + Per-Message Unread Counter

```
메시지 발송 시:
  ChatChannelMetadataEntity.unreadCount += 1  (발신자 제외 멤버별)
  ChatMessageEntity.unreadCount = memberCount - 1  (Phase 2)

멤버가 읽을 때:
  metadata.lastReadMessageId 갱신 (Last-Read Cursor)
  metadata.unreadCount = 0
  Kafka → 커서 이전 message.unreadCount 일괄 감소 (Phase 2)
  Redis → chat:read:event:{channelId} 발행 → WebSocket 실시간 전파

KakaoTalk "N명 안 읽음" 표시 (grroup):
  → message.unreadCount 필드 직접 표시 (Phase 2)
```

### 왜 이 전략인가?

| 방식 | 장점 | 단점 |
|------|------|------|
| `readAt` 단일 컬럼 | 구현 간단 | 1대1 전용, 그룹 불가 |
| `message_read_receipts` 별도 테이블 | 정확한 집계 | 쓰기 폭발 (1000명 = 1000 INSERT) |
| **Last-Read Cursor + Counter** | 쓰기 최소화, 확장성 | 집계 근사치 (적합) |

---

## 단기 계획 (0 ~ 4주): 기반 수정 및 1대1 정상화

### Phase 1. 핵심 버그 수정 — unreadCount 실제 동작

- [x] `JpaChannelMetadataRepository`에 `bulkIncrementUnreadCount` 쿼리 추가
- [x] `MessageSendService.sendMessage()` 내 bulk increment 호출 연결 (발신자 제외)
- [x] `MessageSentEvent`에 `unreadCount` 필드 추가
- [x] `MessageEvent`(websocket-server)에 `unreadCount` + `eventType` 필드 추가
- [x] `MessageEventPublisher`가 memberCount를 받아 unreadCount 포함해 발행
- [x] `ChannelMetadataApplicationService.markAsRead()` lastActivityAt 갱신 추가

### Phase 2. Kafka 푸시 알림 실제 호출 연결

- [x] `MessageSendService`에서 `KafkaMessageProducer.publishNotification()` 실제 호출
- [x] 수신자 목록 (발신자 제외 채널 멤버) 기반 Kafka 발행
- [ ] 온라인 사용자 필터링 (RedisSessionMetadataManager 조회, Phase 1.5)

### Phase 3. 읽음 처리 후 실시간 WebSocket 전파

- [x] `ReadReceiptEventPublisher` 생성 (Redis → `chat:read:event:{channelId}`)
- [x] `ChannelMetadataApplicationService.markAsRead()` 후 ReadReceiptEventPublisher 호출
- [x] `ReadReceiptEvent` DTO 생성 (websocket-server)
- [x] websocket-server `RedisConfig`에 `chat:read:event:*` 구독 추가
- [x] `ReadReceiptRedisSubscriber` 생성 (웹소켓 브로드캐스트)
- [x] `WebSocketBroadcastService.broadcastReadReceipt()` 메서드 추가

### Phase 4. WebSocket 클라이언트 → 읽음 이벤트 수신

- [x] `ChatWebSocketHandler.handleTextMessage()` 파싱 활성화
- [x] 클라이언트 페이로드 포맷 정의: `{"type":"READ_RECEIPT","channelId":"...","lastReadMessageId":"..."}`
- [x] WebSocket READ_RECEIPT 수신 → Redis `chat:read:{channelId}` 발행
- [x] chat-server `ReadReceiptRedisSubscriber` 생성 (`chat:read:{channelId}` → markAsRead)
- [x] chat-server `RedisListenerConfig` 생성 (RedisMessageListenerContainer 구독 설정)

---

## 중기 계획 (1 ~ 3개월): 그룹 채팅 읽음 기능

### Phase 5. Per-Message Unread Count 도입

- [x] Flyway V9: `chat_messages`에 `unread_count INTEGER DEFAULT 0` 컬럼 추가
- [x] `ChatMessageEntity`에 `unreadCount` 필드 추가 + `initUnreadCount()`, `decrementUnread()` 메서드
- [x] 메시지 저장 시 `unreadCount = memberCount - 1` 설정 (MessageSendService)
- [x] `MessageResponse` DTO에 `unreadCount` 필드 추가
- [x] WebSocket 브로드캐스트 페이로드에 `unreadCount` 포함 (MessageSentEvent)

### Phase 6. 읽음 처리의 Kafka 비동기화

- [x] `read-receipt-events` Kafka 토픽 설계 및 생성
- [x] `markAsRead` 시 Kafka 발행 (`ReadReceiptKafkaEvent`: userId, channelId, lastReadMessageId, lastReadCreatedAt)
- [x] Kafka Consumer(`ReadReceiptKafkaConsumer`): 커서 이전 메시지들의 `unreadCount` 일괄 감소
- [x] Consumer: `@KafkaListener(ackMode=MANUAL_IMMEDIATE)` 멱등성 처리
- [x] 동시성 처리: `CASE WHEN unread_count > 0 THEN unread_count - 1 ELSE 0` JPQL UPDATE

### Phase 7. 그룹 채팅 읽음 브로드캐스트

- [x] `ReadReceiptEvent` DTO 설계 (websocket-server: eventType, userId, channelId, lastReadMessageId, readAt)
- [x] 채널 내 모든 접속자에게 읽음 상태 변경 실시간 전파 (`WebSocketBroadcastService.broadcastReadReceipt()`)
- [x] Redis `chat:read:event:{channelId}` → `ReadReceiptRedisSubscriber` → WebSocket 브로드캐스트
- [ ] 클라이언트: 메시지별 "N명 읽음" 카운터 실시간 업데이트 연동 (프론트엔드 작업)

### Phase 8. 멤버 입/퇴장 시 unreadCount 보정

- [x] 채널 가입 시: 신규 멤버는 이전 메시지 unread_count 보정 없음 (미참여 메시지이므로 올바른 동작)
- [x] 채널 퇴장 시: `ChannelCommandService.removeMember()`에서 `MemberLeftKafkaEvent` 발행
- [x] `MemberLeftKafkaConsumer`: lastReadAt 커서 이후 메시지 unread_count 일괄 -1 + metadata 삭제
- [x] 대규모 채널(멤버 1000+): Kafka 비동기로 분리 완료 (`member-left-events` 토픽, channelId 파티션 키)

---

## 장기 계획 (3 ~ 6개월): 대규모 최적화

### Phase 9. Redis unread 캐시 계층 도입

- [x] `HSET chat:channel:{channelId}:unread {userId} {count}` Redis Hash 구조 설계
- [x] 메시지 발송 시 Redis `HINCRBY` (Pipeline, 단일 왕복)
- [x] `markAsRead` 시 Redis `HSET {userId} 0`
- [x] 채널 목록 조회 시 Redis 우선 → miss 시 PostgreSQL fallback
- [ ] Lua 스크립트로 increment/decrement 원자적 처리 (현재 Pipeline 사용)
- [x] TTL 전략: 24h (세션 만료 동기화)

### Phase 10. Redis Cluster 및 Stream 전환

- [ ] Redis Standalone → Cluster 전환 검토
- [ ] Redis Pub/Sub → Redis Stream 전환 검토 (메시지 유실 방지, ACK)
- [ ] Consumer Group 기반 내결함성 처리

### Phase 11. CQRS Read Model 도입

- [x] 채널 목록+미읽음 수 조회 전용 Read Model 설계
- [x] Write Model(PostgreSQL Master) / Read Model(Redis + Replica) 분리
- [x] `ChannelListQueryService` 리팩토링

### Phase 12. 성능 테스트 및 튜닝

- [ ] `unread_count` 인덱스 전략 검토
- [ ] 1000명 그룹 채팅 메시지 발송 부하 테스트 (k6/Gatling)
- [ ] Redis INCR/DECR 처리량 벤치마크
- [ ] Kafka Consumer lag 모니터링 대시보드

---

## 아키텍처 변경 요약

### 현재 (버그 상태)
```
Client → REST(sendMessage) → PostgreSQL insert (unreadCount 증가 없음 ❌)
                           → Redis Pub/Sub(chat:room:{channelId}) → WebSocket broadcast
```

### 단기 목표 (Phase 1~4)
```
Client → REST(sendMessage) → PostgreSQL insert
                           → bulk UPDATE metadata.unreadCount += 1 (발신자 제외)
                           → Kafka(notification-events) → push-service
                           → Redis Pub/Sub(chat:room:{channelId}, unreadCount 포함)
                           → WebSocket broadcast

Client → REST OR WebSocket(READ_RECEIPT)
       → chat-server markAsRead()
       → Redis Pub/Sub(chat:read:event:{channelId})
       → WebSocket: 채널 멤버 전체에게 read receipt 브로드캐스트
```

### 중기 목표 (Phase 5~8)
```
Client → REST(sendMessage) → PostgreSQL (message.unreadCount = memberCount-1)
                           → Kafka → Consumer: batch UPDATE(Phase 6)

Client → REST/WebSocket(READ_RECEIPT)
       → Kafka(read-receipt-events)
       → Consumer: UPDATE message.unreadCount -= 1 (배치)
       → Redis: 캐시 갱신
       → WebSocket: "N명 읽음" 실시간 업데이트
```

---

## 관련 파일 경로

| 파일 | 역할 |
|------|------|
| `chat-server/.../message/application/service/MessageSendService.java` | 메시지 발송 핵심 |
| `chat-server/.../channel/controller/ChannelMetadataController.java` | 읽음 처리 REST API |
| `chat-server/.../channel/application/service/ChannelMetadataApplicationService.java` | 읽음 처리 서비스 |
| `chat-server/.../channel/infrastructure/redis/ReadReceiptEventPublisher.java` | 읽음 이벤트 Redis 발행 |
| `chat-server/.../channel/infrastructure/redis/ReadReceiptRedisSubscriber.java` | 클라이언트 read 이벤트 수신 |
| `chat-server/.../config/RedisListenerConfig.java` | Redis 구독 설정 |
| `websocket-server/.../infrastructure/redis/ReadReceiptEvent.java` | 읽음 이벤트 DTO |
| `websocket-server/.../infrastructure/redis/ReadReceiptRedisSubscriber.java` | read:event 구독 및 브로드캐스트 |
| `websocket-server/.../config/RedisConfig.java` | chat:read:event:* 구독 추가 |
| `libs/chat-storage/.../entity/ChatMessageEntity.java` | 메시지 엔티티 |
| `libs/chat-storage/.../entity/ChatChannelMetadataEntity.java` | 메타데이터 엔티티 |
| `libs/chat-storage/.../repository/JpaChannelMetadataRepository.java` | bulk increment 쿼리 |
| `libs/chat-storage/.../db/migration/V9__add_message_unread_count.sql` | Phase 2: 메시지 컬럼 추가 |
