# 친구 및 채팅방 기능 고도화 - 구현 계획 요약

> **작성일**: 2026-02-17  
> **참고 문서**: [FRIEND_AND_CHANNEL_ENHANCEMENT_DESIGN.md](./FRIEND_AND_CHANNEL_ENHANCEMENT_DESIGN.md)

---

## 🎯 핵심 고도화 전략

### 1. **친구 관리 시스템 신규 구축**

#### Friendship Aggregate 추가

```java
Friendship
├──

FriendshipId(Value Object)
├──userId +

friendId(양방향 관계)
├──

status(PENDING, ACCEPTED, BLOCKED)
├──

nickname(친구 별칭)
└──

favorite(즐겨찾기)
```

**주요 기능**:

- ✅ 친구 요청/수락/거절
- ✅ 친구 차단/차단 해제
- ✅ 친구 별칭 설정
- ✅ 친구 즐겨찾기
- ✅ 친구 목록 조회 (수락된 친구만)
- ✅ 받은 친구 요청 목록

**Domain Service 역할**:

```java
FriendshipDomainService
├──

requestFriendship()   #
양방향 관계
생성 보장
├──

acceptFriendship()    #
양방향 수락
처리
└──

blockFriend()         #
일방적 차단
허용
```

---

### 2. **채팅방 메타데이터 시스템**

#### ChannelMetadata Aggregate 추가 (CQRS Pattern)

```java
ChannelMetadata
├──channelId +

userId(사용자별 설정)
├──

notificationEnabled(알림 ON/OFF)
├──

favorite(즐겨찾기)
├──

pinned(상단 고정)
├──

lastReadMessageId(마지막 읽은 메시지)
├──

unreadCount(읽지 않은 메시지 수)
└──

lastActivityAt(마지막 활동 시간)
```

**주요 기능**:

- ✅ 채팅방별 알림 설정
- ✅ 채팅방 즐겨찾기
- ✅ 채팅방 상단 고정
- ✅ 읽지 않은 메시지 수 추적
- ✅ 마지막 읽은 위치 저장

---

### 3. **채팅방 고급 조회 시스템**

#### ChannelListQuery (필터링 + 정렬)

```java
ChannelListQuery
├──

type(DIRECT, GROUP, PUBLIC, PRIVATE)
├──

onlyFavorites(즐겨찾기만)
├──

onlyUnread(읽지 않은 메시지 있는 것만)
├──

searchKeyword(채널명 검색)
├──

sortBy(LAST_ACTIVITY, NAME, UNREAD_COUNT)
└──page +

size(페이징)
```

#### ChannelListItem (UI 최적화 DTO)

```java
ChannelListItem
├──
채널 기본

정보(id, name, type)
├──
마지막 메시지

정보(content, sender, time)
├──
읽지 않은
메시지 수
├──

사용자별 설정(favorite, pinned, notification)
├── 1:1
채팅 상대방

정보(name, onlineStatus)
└──
그룹 채팅
멤버 수
```

**성능 최적화**:

- ✅ 배치 조회로 N+1 문제 해결
- ✅ Redis 캐싱 (10분 TTL)
- ✅ 이벤트 기반 캐시 무효화

---

### 4. **실시간 사용자 상태 관리**

#### UserOnlineStatus (Redis 캐시)

```
Redis Key: "user:status:{userId}"
Value: ONLINE | AWAY | OFFLINE
TTL: 5분 (하트비트로 갱신)
```

**WebSocket 통합**:

```java
WebSocket 연결
시 →

setOnline()

WebSocket 종료
시 →

setOffline()

하트비트 API →

heartbeat() (
TTL 갱신)
```

**이벤트 발행**:

```java
UserOnlineEvent  →
친구들에게 알림
UserOfflineEvent →
친구들에게 알림
```

---

## 📊 데이터베이스 설계

### 1. friendships 테이블

```sql
CREATE TABLE friendships
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    friend_id  VARCHAR(36) NOT NULL,
    status     VARCHAR(20) NOT NULL, -- PENDING, ACCEPTED, BLOCKED
    nickname   VARCHAR(100),
    favorite   BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,

    INDEX      idx_user_id (user_id),
    INDEX      idx_user_status (user_id, status),
    UNIQUE KEY uk_friendship (user_id, friend_id)
);
```

### 2. channel_metadata 테이블

```sql
CREATE TABLE channel_metadata
(
    id                   VARCHAR(36) PRIMARY KEY,
    channel_id           VARCHAR(36) NOT NULL,
    user_id              VARCHAR(36) NOT NULL,
    notification_enabled BOOLEAN DEFAULT TRUE,
    favorite             BOOLEAN DEFAULT FALSE,
    pinned               BOOLEAN DEFAULT FALSE,
    last_read_message_id VARCHAR(36),
    unread_count         INT     DEFAULT 0,
    last_activity_at     TIMESTAMP,
    created_at           TIMESTAMP   NOT NULL,
    updated_at           TIMESTAMP   NOT NULL,

    INDEX                idx_user_activity (user_id, last_activity_at DESC),
    UNIQUE KEY uk_channel_user (channel_id, user_id)
);
```

---

## 🚀 구현 우선순위

### Phase 1: 친구 관리 기초 (1주) ⭐⭐⭐

**파일 생성 목록**:

```
apps/chat/libs/chat-domain/
├── friendship/
│   ├── Friendship.java
│   ├── FriendshipId.java
│   ├── FriendshipStatus.java
│   └── FriendshipRepository.java
└── service/
    └── FriendshipDomainService.java

apps/chat/libs/chat-storage/
├── entity/
│   └── ChatFriendshipEntity.java
├── repository/
│   └── JpaFriendshipRepository.java
├── adapter/
│   └── FriendshipRepositoryAdapter.java
└── mapper/
    └── FriendshipMapper.java

apps/chat/system-server/
├── application/service/
│   └── FriendshipApplicationService.java
├── controller/
│   └── FriendshipController.java
└── dto/
    ├── FriendshipRequest.java
    └── FriendshipResponse.java
```

**REST API**:

```
POST   /api/friendships              # 친구 요청
GET    /api/friendships              # 친구 목록
GET    /api/friendships/pending      # 받은 요청
PUT    /api/friendships/{id}/accept  # 수락
DELETE /api/friendships/{id}         # 삭제
POST   /api/friendships/{id}/block   # 차단
PUT    /api/friendships/{id}/nickname # 별칭
PUT    /api/friendships/{id}/favorite # 즐겨찾기
```

---

### Phase 2: 채팅방 메타데이터 (1주) ⭐⭐⭐

**파일 생성 목록**:

```
apps/chat/libs/chat-domain/
├── channel/
│   ├── ChannelMetadata.java
│   ├── ChannelMetadataId.java
│   └── ChannelMetadataRepository.java

apps/chat/libs/chat-storage/
├── entity/
│   └── ChatChannelMetadataEntity.java
├── repository/
│   └── JpaChannelMetadataRepository.java
└── adapter/
    └── ChannelMetadataRepositoryAdapter.java

apps/chat/system-server/
├── application/service/
│   └── ChannelMetadataApplicationService.java
└── controller/
    └── ChannelMetadataController.java (or 기존 ChannelController 확장)
```

**REST API**:

```
PUT    /api/channels/{id}/favorite       # 즐겨찾기
PUT    /api/channels/{id}/pin            # 상단 고정
PUT    /api/channels/{id}/notification   # 알림 설정
PUT    /api/channels/{id}/read           # 읽음 처리
GET    /api/channels/{id}/unread-count   # 읽지 않은 수
```

---

### Phase 3: 고급 조회 기능 (1주) ⭐⭐

**파일 생성 목록**:

```
apps/chat/system-server/
├── application/
│   ├── query/
│   │   ├── ChannelListQuery.java
│   │   ├── ChannelListItem.java
│   │   └── ChannelSortBy.java
│   └── service/
│       └── ChannelQueryService.java
└── controller/
    └── ChannelQueryController.java
```

**개선된 REST API**:

```
GET /api/channels?type=DIRECT
                 &onlyFavorites=true
                 &onlyUnread=true
                 &search=keyword
                 &sortBy=LAST_ACTIVITY
                 &page=0&size=20
```

**Response 예시**:

```json
{
  "content": [
    {
      "channelId": "ch-123",
      "channelName": "김철수",
      "channelType": "DIRECT",
      "lastMessageContent": "안녕하세요",
      "lastMessageTime": "2026-02-17T10:30:00Z",
      "unreadCount": 5,
      "favorite": true,
      "pinned": false,
      "otherUserName": "김철수",
      "otherUserStatus": "ONLINE"
    }
  ],
  "totalElements": 42,
  "totalPages": 3
}
```

---

### Phase 4: 실시간 상태 (3일) ⭐

**파일 생성 목록**:

```
apps/chat/websocket-server/
├── cache/
│   └── UserOnlineStatusCache.java
├── handler/
│   └── WebSocketConnectionHandler.java (기존 수정)
└── event/
    ├── UserOnlineEvent.java
    └── UserOfflineEvent.java

apps/chat/system-server/
├── controller/
│   └── UserStatusController.java
└── event/
    └── UserStatusEventListener.java
```

**REST API**:

```
POST /api/users/heartbeat           # 하트비트
GET  /api/users/{id}/status         # 상태 조회
GET  /api/users/batch-status?ids=... # 배치 조회
```

---

### Phase 5: 성능 최적화 (3일) ⭐

**캐싱 구현**:

```
apps/chat/system-server/
├── cache/
│   └── ChannelListCacheManager.java
└── event/
    └── MessageSentEventListener.java (캐시 무효화)
```

**배치 조회 최적화**:

```
apps/chat/libs/chat-domain/
└── message/
    └── MessageRepository.java
        └── findLastMessageByChannelIds() 추가

apps/chat/libs/chat-storage/
└── adapter/
    └── MessageRepositoryAdapter.java
        └── Native Query로 배치 조회 구현
```

---

## 🧪 테스트 전략

### 단위 테스트 (JUnit 5 + AssertJ)

```java
// Domain 테스트
FriendshipTest.java
├──
친구 요청
생성
├──
친구 수락
├──
친구 차단
└──
도메인 규칙
검증

FriendshipDomainServiceTest.java
├──
양방향 관계
생성 검증
├──
수락 시
양방향 처리
검증
└──
예외 상황
테스트

ChannelMetadataTest.java
├──
읽음 처리
├──
읽지 않은
수 증가
└──
설정 토글
```

### 통합 테스트 (TestContainers)

```java
FriendshipApplicationServiceTest.java
├──
친구 요청
전체 플로우
├──
Repository 통합
테스트
└──
이벤트 발행
검증

ChannelQueryServiceTest.java
├──
복잡한 필터링
쿼리
├──
배치 조회
성능 검증
└──
정렬 로직
검증
```

### 성능 테스트

```java
ChannelListPerformanceTest.java
├── 100
개 채팅방

조회(100ms 이내)
├── 1000
개 채팅방

필터링(500ms 이내)
└──
캐시 히트율
측정
```

---

## 📈 성능 목표

| 기능         | 목표 응답 시간          | 최적화 방법          |
|------------|-------------------|-----------------|
| 채팅방 목록 조회  | 100ms (캐시 시 10ms) | Redis 캐싱, 배치 조회 |
| 친구 목록 조회   | 50ms              | 인덱스 최적화         |
| 읽지 않은 수 계산 | 실시간               | 이벤트 기반 증분 업데이트  |
| 온라인 상태 조회  | 5ms               | Redis In-Memory |
| 친구 요청 처리   | 200ms             | 트랜잭션 최적화        |

---

## 🔄 이벤트 플로우

### 친구 요청 플로우

```
1. POST /api/friendships
2. FriendshipApplicationService.requestFriendship()
3. FriendshipDomainService.requestFriendship()
   → 양방향 Friendship 생성
4. FriendshipRepository.save() x 2
5. Event 발행: FriendRequestedEvent
6. → Push Service (FCM 알림)
```

### 메시지 발송 시 캐시 무효화

```
1. MessageSentEvent 발행
2. MessageSentEventListener.onMessageSent()
3. ChannelRepository.findById() (멤버 조회)
4. 모든 멤버의 ChannelListCache 무효화
5. ChannelMetadata.incrementUnreadCount() (발신자 제외)
```

### 온라인 상태 변경

```
WebSocket 연결
1. UserOnlineStatusCache.setOnline()
2. Event 발행: UserOnlineEvent
3. → WebSocket으로 친구들에게 브로드캐스트

WebSocket 종료
1. UserOnlineStatusCache.setOffline()
2. Event 발행: UserOfflineEvent
3. → WebSocket으로 친구들에게 브로드캐스트
```

---

## 🎨 UI/UX 개선 포인트

### 채팅방 목록 화면

```
┌─────────────────────────────────────┐
│ 채팅방  [검색] [필터]                │
├─────────────────────────────────────┤
│ ⭐ [고정] 프로젝트팀        [5] 🔔   │
│   "김철수: 네 알겠습니다"             │
│   10:30                             │
├─────────────────────────────────────┤
│ 🟢 김철수              [2] 🔕       │
│   "안녕하세요"                       │
│   09:45                             │
├─────────────────────────────────────┤
│ ⚪ 이영희              [0]          │
│   "감사합니다"                       │
│   어제                              │
└─────────────────────────────────────┘

범례:
⭐ = 즐겨찾기
[5] = 읽지 않은 메시지 수
🔔/🔕 = 알림 ON/OFF
🟢 = 온라인
⚪ = 오프라인
```

### 친구 목록 화면

```
┌─────────────────────────────────────┐
│ 친구  [요청 3]  [차단]               │
├─────────────────────────────────────┤
│ ⭐ 즐겨찾기                          │
│   🟢 김철수 (동료)                   │
│   🟢 이영희 (친구)                   │
├─────────────────────────────────────┤
│ ㄱ                                  │
│   ⚪ 강민수                          │
│   🟡 김지은 (자리비움)                │
└─────────────────────────────────────┘
```

---

## 🔐 보안 고려사항

1. **친구 요청 스팸 방지**
	- 1일 친구 요청 제한 (예: 50건)
	- 차단된 사용자 재요청 금지

2. **권한 검증**
	- 친구 수락은 수신자만 가능
	- 채널 메타데이터는 멤버만 수정 가능

3. **개인정보 보호**
	- 온라인 상태는 친구에게만 노출
	- 마지막 접속 시간 설정 옵션

---

## 📝 다음 액션 아이템

### 즉시 시작 가능한 작업

1. **Friendship Domain 모델 생성**
   ```bash
   # Domain Layer
   apps/chat/libs/chat-domain/src/main/java/com/example/chat/domain/friendship/
   ```

2. **Database Migration 작성**
   ```sql
   V7__create_friendships_table.sql
   V8__create_channel_metadata_table.sql
   ```

3. **Storage Layer 구현**
   ```bash
   apps/chat/libs/chat-storage/src/main/java/com/example/chat/storage/
   ```

4. **Application Service 구현**
   ```bash
   apps/chat/system-server/src/main/java/com/example/chat/system/
   ```

---

## 🎓 핵심 설계 원칙 재확인

### DDD (Domain-Driven Design)

- ✅ Friendship, ChannelMetadata를 별도 Aggregate로 분리
- ✅ Domain Service로 복잡한 비즈니스 규칙 캡슐화
- ✅ Repository 패턴으로 영속성 추상화

### EDA (Event-Driven Architecture)

- ✅ 친구 요청/수락 이벤트 발행
- ✅ 메시지 발송 시 캐시 무효화 이벤트
- ✅ 온라인 상태 변경 이벤트 브로드캐스트

### CQRS (Command Query Responsibility Segregation)

- ✅ Channel (Command): 채널 생성/수정
- ✅ ChannelListQuery (Query): 복잡한 조회 로직 분리

### Clean Architecture

- ✅ Domain Layer는 외부 의존성 없음
- ✅ Storage Layer는 Domain 인터페이스 구현
- ✅ Application Layer가 Use Case 오케스트레이션

---

**작성자**: AI Assistant  
**최종 수정일**: 2026-02-17  
**다음 리뷰**: Phase 1 완료 후
