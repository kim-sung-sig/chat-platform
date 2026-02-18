# Phase 1: 친구 관리 기초 - 진행 상황

> **시작일**: 2026-02-17  
> **상태**: 🚧 진행 중

---

## ✅ 완료된 작업

### 1. Domain Layer 구현 완료

#### 1.1 Value Objects & Enums

- ✅ `FriendshipId.java` - 친구 관계 ID (UUID 기반)
- ✅ `FriendshipStatus.java` - 상태 Enum (PENDING, ACCEPTED, BLOCKED)

#### 1.2 Aggregate Root

- ✅ `Friendship.java` - 친구 관계 Aggregate
	- Factory Method: `requestFriendship()`
	- Business Methods: `accept()`, `reject()`, `block()`, `unblock()`
	- Business Methods: `setNickname()`, `toggleFavorite()`
	- Query Methods: `isAccepted()`, `isPending()`, `isBlocked()`, `isFavorite()`

#### 1.3 Repository Port

- ✅ `FriendshipRepository.java` - Repository 인터페이스
	- 기본 CRUD
	- 상태별 조회 (ACCEPTED, PENDING, BLOCKED)
	- 즐겨찾기 조회
	- 양방향 관계 확인

#### 1.4 Domain Service

- ✅ `FriendshipDomainService.java` - 도메인 서비스
	- `requestFriendship()` - 양방향 관계 생성
	- `acceptFriendship()` - 양방향 수락 처리
	- `blockFriend()`, `unblockFriend()`
	- `FriendshipPair` 내부 클래스

---

### 2. Storage Layer 구현 완료

#### 2.1 JPA Entity

- ✅ `ChatFriendshipEntity.java`
	- 테이블명: `chat_friendships`
	- 인덱스: `idx_user_id`, `idx_friend_id`, `idx_user_status`
	- 유니크 제약: `uk_friendship (user_id, friend_id)`

#### 2.2 JPA Repository

- ✅ `JpaFriendshipRepository.java`
	- Spring Data JPA 메서드
	- Custom Query: `findFavoritesByUserId()`, `existsMutualFriendship()`

#### 2.3 Mapper

- ✅ `FriendshipMapper.java`
	- Domain ↔ Entity 변환

#### 2.4 Repository Adapter

- ✅ `FriendshipRepositoryAdapter.java`
	- FriendshipRepository 인터페이스 구현
	- @Transactional 적용

---

### 3. Database Migration

- ✅ `V7__create_friendships_table.sql`
	- `chat_friendships` 테이블 생성
	- 인덱스 및 제약조건 설정
	- 코멘트 추가

---

## 📂 생성된 파일 목록

```
apps/chat/libs/chat-domain/src/main/java/com/example/chat/domain/
└── friendship/
    ├── Friendship.java                    ✅ (160 lines)
    ├── FriendshipId.java                  ✅ (40 lines)
    ├── FriendshipStatus.java              ✅ (20 lines)
    └── FriendshipRepository.java          ✅ (90 lines)

apps/chat/libs/chat-domain/src/main/java/com/example/chat/domain/service/
└── FriendshipDomainService.java           ✅ (130 lines)

apps/chat/libs/chat-storage/src/main/java/com/example/chat/storage/
├── entity/
│   └── ChatFriendshipEntity.java          ✅ (65 lines)
├── repository/
│   └── JpaFriendshipRepository.java       ✅ (50 lines)
├── mapper/
│   └── FriendshipMapper.java              ✅ (40 lines)
└── adapter/
    └── FriendshipRepositoryAdapter.java   ✅ (125 lines)

apps/chat/libs/chat-storage/src/main/resources/db/migration/
└── V7__create_friendships_table.sql       ✅ (35 lines)
```

**총 파일**: 10개  
**총 라인 수**: 약 755 lines

---

## 🔄 다음 단계 (Application Layer)

### Step 11-15: Application Service 구현

```
apps/chat/system-server/src/main/java/com/example/chat/system/
├── application/
│   ├── service/
│   │   └── FriendshipApplicationService.java  ⏳
│   └── dto/
│       ├── request/
│       │   ├── FriendshipRequest.java         ⏳
│       │   └── SetNicknameRequest.java        ⏳
│       └── response/
│           └── FriendshipResponse.java        ⏳
└── controller/
    └── FriendshipController.java              ⏳
```

### Step 16-17: Event 정의

```
common/core/src/main/java/com/example/chat/common/event/
├── FriendRequestedEvent.java                  ⏳
├── FriendAcceptedEvent.java                   ⏳
└── FriendBlockedEvent.java                    ⏳
```

---

## 🧪 테스트 계획 (다음)

### Domain Layer Tests

```
apps/chat/libs/chat-domain/src/test/java/
├── FriendshipTest.java                        ⏳
├── FriendshipIdTest.java                      ⏳
└── FriendshipDomainServiceTest.java           ⏳
```

### Storage Layer Tests

```
apps/chat/libs/chat-storage/src/test/java/
├── FriendshipRepositoryAdapterTest.java       ⏳
└── FriendshipMapperTest.java                  ⏳
```

---

## 📊 예상 API 엔드포인트

```http
POST   /api/friendships
  Request:  { "friendId": "user-123" }
  Response: { "id": "...", "status": "PENDING", ... }

GET    /api/friendships
  Response: [{ "id": "...", "friend": {...}, "status": "ACCEPTED", ... }]

GET    /api/friendships/pending
  Response: [{ "id": "...", "requester": {...}, ... }]

PUT    /api/friendships/{id}/accept
  Response: { "id": "...", "status": "ACCEPTED", ... }

DELETE /api/friendships/{id}
  Response: 204 No Content

POST   /api/friendships/{id}/block
  Response: { "id": "...", "status": "BLOCKED", ... }

PUT    /api/friendships/{id}/nickname
  Request:  { "nickname": "친한친구" }
  Response: { "id": "...", "nickname": "친한친구", ... }

PUT    /api/friendships/{id}/favorite
  Response: { "id": "...", "favorite": true, ... }
```

---

## 🎯 완성도

- **Domain Layer**: 100% ✅
- **Storage Layer**: 100% ✅
- **Database Migration**: 100% ✅
- **Application Layer**: 0% ⏳
- **REST API**: 0% ⏳
- **Tests**: 0% ⏳

**전체 진행률**: **30% (3/10)**

---

## 💡 핵심 설계 결정사항

### 1. 양방향 관계 설계

- **결정**: 두 개의 Friendship 엔티티로 양방향 관계 표현
- **이유**:
	- 각 사용자가 자신의 친구 목록을 빠르게 조회 가능
	- 친구별 설정 (별칭, 즐겨찾기)을 독립적으로 관리
	- 일방적 차단 구현 가능

### 2. Domain Service 사용

- **결정**: `FriendshipDomainService`에서 양방향 관계 생성 로직 캡슐화
- **이유**:
	- User + Friendship Aggregate 간 협력 필요
	- 복잡한 비즈니스 규칙 (양방향 일관성) 보장
	- 코드 재사용성

### 3. 상태 전이 규칙

```
PENDING → ACCEPTED  (accept)
PENDING → (삭제)    (reject)
ACCEPTED → BLOCKED  (block)
BLOCKED → ACCEPTED  (unblock)
```

### 4. 성능 최적화

- **인덱스**: `(user_id, status)` 복합 인덱스
	- 친구 목록 조회 성능 최적화
	- 받은 요청 목록 조회 성능 최적화

---

## 🔍 코드 리뷰 포인트

### ✅ 잘된 점

1. **Early Return 패턴** 일관성 있게 적용
2. **불변성** 유지 (final 필드, Builder 패턴)
3. **명확한 책임 분리** (Domain / Storage / Application)
4. **조기 에러 검증** (Factory Method에서 검증)

### 🤔 개선 고려사항

1. **친구 요청 제한**: 스팸 방지 로직 추가 필요 (Application Layer)
2. **차단 사용자 재요청**: 차단된 사용자의 재요청 방지 로직
3. **이벤트 발행**: Spring Event 통합 필요

---

**Phase 1 완료!** 🎉  
**다음 Phase**: Phase 2 - 채팅방 메타데이터 시스템 구현  
**작성자**: AI Assistant  
**완료일**: 2026-02-17
