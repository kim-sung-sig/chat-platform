# Phase 2: 채팅방 메타데이터 시스템 - 완료 보고서

> **완료일**: 2026-02-17  
> **소요 시간**: 약 1.5시간  
> **상태**: ✅ **완료**

---

## 🎉 Phase 2 완료!

**채팅방 메타데이터 시스템**이 성공적으로 구현되었습니다.

---

## 📊 구현 결과

### 생성된 파일 통계

| Layer       | 파일 수   | 라인 수      | 상태    |
|-------------|--------|-----------|-------|
| Domain      | 3      | 320       | ✅     |
| Storage     | 4      | 270       | ✅     |
| Application | 2      | 250       | ✅     |
| Controller  | 1      | 145       | ✅     |
| Migration   | 1      | 50        | ✅     |
| **합계**      | **11** | **1,035** | **✅** |

---

## 🔧 구현된 기능

### 1. 채팅방 메타데이터 관리

✅ **사용자별 설정**

- 알림 ON/OFF (notificationEnabled)
- 즐겨찾기 (favorite)
- 상단 고정 (pinned)

✅ **읽기 상태 추적**

- 마지막 읽은 메시지 ID (lastReadMessageId)
- 마지막 읽은 시간 (lastReadAt)
- 읽지 않은 메시지 수 (unreadCount)

✅ **활동 추적**

- 마지막 활동 시간 (lastActivityAt)
- 채팅방 정렬 기준으로 활용

---

### 2. 읽기 상태 관리

✅ **읽음 처리**

```java
// 메시지 읽음 처리 → 읽지 않은 수 0으로 초기화
metadata.markAsRead(messageId);
```

✅ **읽지 않은 메시지 수 증가**

```java
// 새 메시지 수신 시
metadata.incrementUnreadCount();
```

---

### 3. 다양한 조회 옵션

✅ **필터링 조회**

- 즐겨찾기 채팅방만
- 상단 고정 채팅방만
- 읽지 않은 메시지가 있는 채팅방만

---

## 🌐 REST API 엔드포인트

### 메타데이터 관리

```http
GET    /api/channels/{channelId}/metadata    # 메타데이터 조회/생성
PUT    /api/channels/{channelId}/read        # 읽음 처리
PUT    /api/channels/{channelId}/notification # 알림 토글
PUT    /api/channels/{channelId}/favorite    # 즐겨찾기 토글
PUT    /api/channels/{channelId}/pin         # 상단 고정 토글
```

### 목록 조회

```http
GET    /api/channels/favorites               # 즐겨찾기 목록
GET    /api/channels/pinned                  # 상단 고정 목록
GET    /api/channels/unread                  # 읽지 않은 메시지 있는 목록
```

**총 8개 엔드포인트**

---

## 🗄️ 데이터베이스

### chat_channel_metadata 테이블

```sql
CREATE TABLE chat_channel_metadata
(
    id                   VARCHAR(36) PRIMARY KEY,
    channel_id           VARCHAR(36)              NOT NULL,
    user_id              VARCHAR(36)              NOT NULL,
    notification_enabled BOOLEAN                  NOT NULL DEFAULT TRUE,
    favorite             BOOLEAN                  NOT NULL DEFAULT FALSE,
    pinned               BOOLEAN                  NOT NULL DEFAULT FALSE,
    last_read_message_id VARCHAR(36),
    last_read_at         TIMESTAMP WITH TIME ZONE,
    unread_count         INTEGER                  NOT NULL DEFAULT 0,
    last_activity_at     TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL
);
```

**인덱스**:

- `idx_user_id` - 사용자별 조회
- `idx_channel_id` - 채널별 조회
- `idx_user_activity` - 활동 시간 기준 정렬 (복합 인덱스)
- `idx_user_favorite` - 즐겨찾기 필터링
- `idx_user_pinned` - 상단 고정 필터링
- `uk_channel_user` - 중복 방지 (유니크 제약)

---

## 🏗️ 아키텍처 설계

### CQRS 패턴 적용

```
Command Side (Channel Aggregate)
└── 채널 생성/수정/삭제

Query Side (ChannelMetadata Aggregate)
└── 사용자별 읽기 최적화
    ├── 읽기 상태
    ├── 사용자 설정
    └── 빠른 조회
```

### Domain Model

```
ChannelMetadata (Aggregate Root)
├── ChannelMetadataId (Value Object)
├── ChannelId (참조)
├── UserId (참조)
├── 설정 (notification, favorite, pinned)
└── 읽기 상태 (lastReadMessageId, unreadCount)
```

---

## ✨ 핵심 설계 결정

### 1. Aggregate 분리 (CQRS)

**결정**: Channel과 ChannelMetadata를 별도 Aggregate로 분리

**이유**:

- Channel: 채널 자체의 정보 (이름, 타입, 멤버)
- ChannelMetadata: 사용자별 읽기/설정 정보
- 각 사용자마다 독립적인 설정 필요
- 조회 성능 최적화

**장점**:

- 읽기/쓰기 분리로 성능 향상
- 사용자별 데이터 독립성
- 확장성 (샤딩 가능)

---

### 2. 읽지 않은 메시지 수 관리

**결정**: ChannelMetadata에 unreadCount 저장

**이유**:

- 매번 COUNT 쿼리 방지
- 읽기 성능 최적화
- 실시간 업데이트 가능

**업데이트 시점**:

```
새 메시지 수신 → incrementUnreadCount()
메시지 읽음 → markAsRead() (count = 0)
```

---

### 3. 배치 조회 지원

**결정**: `findByChannelIdsAndUserId()` 메서드 제공

**이유**:

- N+1 문제 방지
- 채팅방 목록 조회 시 성능 최적화
- 한 번의 쿼리로 여러 채널의 메타데이터 조회

**사용 예**:

```java
// Phase 3에서 사용
Map<ChannelId, ChannelMetadata> metadataMap =
		repository.findByChannelIdsAndUserId(channelIds, userId);
```

---

## 🎯 Phase 3 준비 완료

### 다음 단계에서 활용

Phase 3 (채팅방 고급 조회)에서 ChannelMetadata를 활용하여:

✅ **채팅방 목록 조회 시**

```java
ChannelListItem {
	channelId,
			channelName,
			lastMessage,
			unreadCount,        // ← ChannelMetadata
			favorite,           // ← ChannelMetadata
			pinned,             // ← ChannelMetadata
			notificationEnabled // ← ChannelMetadata
}
```

✅ **필터링**

- 즐겨찾기만 보기
- 읽지 않은 메시지 있는 것만 보기

✅ **정렬**

- 상단 고정 우선
- 마지막 활동 시간 순

---

## 📈 성능 최적화

### 인덱스 전략

```sql
-- 가장 빈번한 조회: 사용자별 활동 시간 순 정렬
CREATE INDEX idx_user_activity
    ON chat_channel_metadata (user_id, last_activity_at DESC);

-- 즐겨찾기 필터링
CREATE INDEX idx_user_favorite
    ON chat_channel_metadata (user_id, favorite);
```

**예상 성능**:

- 사용자별 메타데이터 조회: O(log n)
- 읽지 않은 수 업데이트: O(1) - PK 조회
- 배치 조회: O(m log n) - m개 채널

---

## 🔄 이벤트 통합 준비

### 메시지 발송 시 자동 업데이트

**시나리오**:

```
1. 메시지 발송 (MessageSentEvent)
2. → 수신자들의 unreadCount 증가
3. → lastActivityAt 업데이트
```

**구현 (다음 단계)**:

```java

@EventListener
public void onMessageSent(MessageSentEvent event) {
	Channel channel = channelRepository.findById(event.getChannelId());

	channel.getMemberIds().forEach(memberId -> {
		if (!memberId.equals(event.getSenderId())) {
			metadataService.incrementUnreadCount(
					memberId.getValue(),
					event.getChannelId().getValue()
			);
		}
	});
}
```

---

## 🧪 테스트 준비 완료

### 테스트 가능한 시나리오

✅ **Domain Layer**

- ChannelMetadata 단위 테스트
- 읽음 처리 로직
- 읽지 않은 수 증가/감소

✅ **Storage Layer**

- Repository Adapter 테스트
- 배치 조회 성능 테스트

✅ **Application Layer**

- 메타데이터 생성/조회 테스트
- 설정 토글 테스트

---

## 📝 생성된 파일 목록

```
apps/chat/libs/chat-domain/src/main/java/com/example/chat/domain/channel/metadata/
├── ChannelMetadata.java                    ✅ (210 lines)
├── ChannelMetadataId.java                  ✅ (40 lines)
└── ChannelMetadataRepository.java          ✅ (70 lines)

apps/chat/libs/chat-storage/src/main/java/com/example/chat/storage/
├── entity/
│   └── ChatChannelMetadataEntity.java      ✅ (75 lines)
├── repository/
│   └── JpaChannelMetadataRepository.java   ✅ (60 lines)
├── mapper/
│   └── ChannelMetadataMapper.java          ✅ (60 lines)
└── adapter/
    └── ChannelMetadataRepositoryAdapter.java ✅ (125 lines)

apps/chat/libs/chat-storage/src/main/resources/db/migration/
└── V8__create_channel_metadata_table.sql   ✅ (50 lines)

apps/chat/system-server/src/main/java/com/example/chat/system/
├── application/
│   ├── service/
│   │   └── ChannelMetadataApplicationService.java ✅ (190 lines)
│   └── dto/response/
│       └── ChannelMetadataResponse.java    ✅ (60 lines)
└── controller/
    └── ChannelMetadataController.java      ✅ (145 lines)
```

**총 11개 파일, 약 1,035 lines**

---

## ✅ 체크리스트

- [x] Domain 모델 설계
- [x] Repository 인터페이스 정의
- [x] JPA Entity 매핑
- [x] Database Migration
- [x] Application Service 구현
- [x] REST Controller 구현
- [x] DTO 설계
- [x] 배치 조회 지원
- [x] 빌드 성공 확인
- [ ] 단위 테스트 작성 (다음)
- [ ] Event Listener 구현 (Phase 3)
- [ ] Redis 캐싱 적용 (Phase 4)

---

## 🚀 다음 단계 (Phase 3)

### 채팅방 고급 조회 시스템

**목표**:

- ChannelListQuery 모델 설계
- 복잡한 필터링/정렬 구현
- ChannelMetadata + Channel + Message 조인 조회
- 마지막 메시지 정보 포함

**핵심 기능**:

```java
ChannelListItem {
	// Channel 정보
	channelId, channelName, channelType,

			// ChannelMetadata 정보
			unreadCount, favorite, pinned,

			// 마지막 메시지 정보
			lastMessageContent, lastMessageTime, lastMessageSender,

			// 1:1 채팅 상대방 정보
			otherUserName, otherUserOnlineStatus
}
```

---

**Phase 2 완료!** 🎉

**누적 진행률**:

- Phase 1: 100% ✅
- Phase 2: 100% ✅
- Phase 3: 0% (다음)

**작성자**: AI Assistant  
**완료일**: 2026-02-17
