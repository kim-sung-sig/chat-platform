# 읽음 기능 현황 요약

## 기술 스택

| 항목 | 기술 |
|------|------|
| Spring Boot | 3.4.4 |
| Java | 21 (Virtual Threads 가능) |
| 주 DB | PostgreSQL (Source/Replica 분리, HikariCP) |
| 실시간 메시징 | Redis Pub/Sub (`chat:room:{channelId}`) |
| 세션 메타데이터 | Redis (`chat:session:info:*`, `chat:room:sessions:*`, `chat:user:sessions:*`) |
| 푸시 알림 | Kafka (`notification-events` 토픽 → push-service) |
| DB 마이그레이션 | Flyway (V1~V8) |
| 서비스 디스커버리 | Eureka + Config Server |

---

## 현재 읽음 관련 구현 상태

| 기능 | 구현 여부 | 비고 |
|------|-----------|------|
| `unreadCount` DB 증가 | ❌ 미구현 | `incrementUnreadCount()` 호출 누락 |
| `readAt` 메시지 저장 | ❌ 미구현 | `markAsRead()` 호출 누락, 1대1만 유효 |
| WebSocket read receipt 수신 | ❌ 미구현 | `handleTextMessage()` 완전 차단 |
| Redis read event 브로드캐스트 | ❌ 없음 | Redis Pub/Sub에 read 채널 없음 |
| Redis unread 캐시 | ❌ 없음 | 매번 PostgreSQL 직접 조회 |
| 그룹 채팅 읽음 | ❌ 설계 없음 | `readAt` 단일 컬럼으론 그룹 불가 |
| Kafka 푸시 알림 | ❌ 미호출 | `KafkaMessageProducer` 클래스만 존재 |
| 메시지 per-message unreadCount | ❌ DB 컬럼 없음 | Phase 2에서 추가 |

---

## 핵심 설계 결정

### "Last-Read Cursor + Per-Message Unread Counter" 전략 채택

```
사용자별:  chat_channel_metadata.last_read_message_id  (커서)
           chat_channel_metadata.unread_count          (사용자별 미읽음 수)

메시지별:  chat_messages.unread_count                  (Phase 2: 전체 미읽음 뷰어 수)
```

### Redis 채널 설계

| 채널 패턴 | 방향 | 용도 |
|-----------|------|------|
| `chat:room:{channelId}` | chat-server → websocket-server | 신규 메시지 브로드캐스트 |
| `chat:read:{channelId}` | websocket-server → chat-server | 클라이언트 read 이벤트 전달 |
| `chat:read:event:{channelId}` | chat-server → websocket-server | 읽음 처리 완료 브로드캐스트 |

---

## 진행 현황

### 단기 (Phase 1~4)

- [x] **Phase 1**: JpaChannelMetadataRepository bulk increment, MessageSendService 연결
- [x] **Phase 2**: KafkaMessageProducer 실제 호출 (push 알림)
- [x] **Phase 3**: ReadReceiptEventPublisher, ReadReceiptRedisSubscriber (websocket-server)
- [x] **Phase 4**: ChatWebSocketHandler read receipt 수신, chat-server Redis subscriber

### 중기 (Phase 5~8)

- [ ] **Phase 5**: Flyway V9, ChatMessageEntity.unreadCount, MessageResponse 확장
- [ ] **Phase 6**: read-receipt-events Kafka 토픽, 비동기 batch decrement
- [ ] **Phase 7**: 그룹 채팅 읽음 실시간 "N명 읽음" 브로드캐스트
- [ ] **Phase 8**: 멤버 입/퇴장 보정 로직

### 장기 (Phase 9~12)

- [ ] **Phase 9**: Redis Hash 기반 unread 캐시 계층
- [ ] **Phase 10**: Redis Cluster/Stream 전환
- [ ] **Phase 11**: CQRS Read Model
- [ ] **Phase 12**: 부하 테스트 및 튜닝

---

## WebSocket 클라이언트 프로토콜

### 읽음 이벤트 전송 (Client → Server)

```json
{
  "type": "READ_RECEIPT",
  "channelId": "channel-uuid",
  "lastReadMessageId": "message-uuid"
}
```

### 읽음 이벤트 수신 (Server → Client)

```json
{
  "eventType": "READ_RECEIPT",
  "channelId": "channel-uuid",
  "userId": "user-uuid",
  "lastReadMessageId": "message-uuid",
  "readAt": "2026-03-09T12:00:00Z"
}
```

### 신규 메시지 수신 (Server → Client)

```json
{
  "eventType": "MESSAGE",
  "messageId": "message-uuid",
  "channelId": "channel-uuid",
  "senderId": "user-uuid",
  "messageType": "TEXT",
  "content": "안녕하세요",
  "status": "SENT",
  "unreadCount": 3,
  "sentAt": "2026-03-09T12:00:00Z"
}
```
