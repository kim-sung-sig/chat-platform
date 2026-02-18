# Phase 1: 친구 관리 기초 - 완료 보고서

> **완료일**: 2026-02-17  
> **소요 시간**: 약 3시간  
> **상태**: ✅ **완료**

---

## 🎉 성공적으로 완료!

**Phase 1: 친구 관리 기초 시스템**이 성공적으로 구현되었습니다.

---

## 📊 구현 결과

### 생성된 파일 통계

| Layer       | 파일 수   | 라인 수      | 상태    |
|-------------|--------|-----------|-------|
| Domain      | 5      | 370       | ✅     |
| Storage     | 4      | 280       | ✅     |
| Application | 4      | 437       | ✅     |
| Controller  | 1      | 186       | ✅     |
| Event       | 3      | 30        | ✅     |
| Migration   | 1      | 35        | ✅     |
| **합계**      | **18** | **1,338** | **✅** |

---

## 🔧 구현된 기능

### 1. 친구 요청 시스템

✅ **양방향 친구 관계 생성**

- A가 B에게 요청 → 자동으로 B→A 관계도 생성
- Domain Service를 통한 일관성 보장

✅ **친구 요청 상태 관리**

```
PENDING → ACCEPTED  (수락)
PENDING → 삭제       (거절)
```

✅ **검증 규칙**

- 자기 자신에게 요청 불가
- 비활성 사용자에게 요청 불가
- 차단된 사용자에게 요청 불가
- 중복 요청 방지

---

### 2. 친구 관리 기능

✅ **친구 차단/차단 해제**

```
ACCEPTED → BLOCKED  (차단)
BLOCKED → ACCEPTED  (차단 해제)
```

✅ **친구 삭제**

- 양방향 관계 모두 삭제
- CASCADE 처리

✅ **친구 설정**

- 별칭 설정 (친구 목록에서 표시할 이름)
- 즐겨찾기 토글

---

### 3. 친구 목록 조회

✅ **다양한 조회 옵션**

- 수락된 친구 목록
- 받은 친구 요청 목록
- 보낸 친구 요청 목록
- 즐겨찾기 친구 목록

---

### 4. 이벤트 발행

✅ **Spring Event 통합**

- `FriendRequestedEvent` - 친구 요청 시
- `FriendAcceptedEvent` - 친구 수락 시
- `FriendBlockedEvent` - 친구 차단 시

**활용 예**:

- Push 알림 발송
- 채팅방 자동 생성 (1:1 채팅)
- 활동 로그 기록

---

## 🌐 REST API 엔드포인트

### 친구 요청 관리

```http
POST   /api/friendships
GET    /api/friendships
GET    /api/friendships/pending
GET    /api/friendships/sent
GET    /api/friendships/favorites
PUT    /api/friendships/{id}/accept
DELETE /api/friendships/{id}/reject
```

### 친구 관리

```http
DELETE /api/friendships/users/{friendId}
POST   /api/friendships/users/{friendId}/block
DELETE /api/friendships/users/{friendId}/block
PUT    /api/friendships/users/{friendId}/nickname
PUT    /api/friendships/users/{friendId}/favorite
```

**총 12개 엔드포인트**

---

## 🗄️ 데이터베이스

### chat_friendships 테이블

```sql
CREATE TABLE chat_friendships
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36)              NOT NULL,
    friend_id  VARCHAR(36)              NOT NULL,
    status     VARCHAR(20)              NOT NULL,
    nickname   VARCHAR(100),
    favorite   BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

**인덱스**:

- `idx_user_id` - 사용자별 친구 목록 조회
- `idx_friend_id` - 받은 요청 조회
- `idx_user_status` - 상태별 필터링 (복합 인덱스)
- `uk_friendship` - 중복 방지 (유니크 제약)

---

## 🏗️ 아키텍처 설계

### DDD (Domain-Driven Design) 적용

```
Domain Layer (chat-domain)
├── Friendship (Aggregate Root)
│   ├── FriendshipId (Value Object)
│   └── FriendshipStatus (Enum)
├── FriendshipRepository (Port)
└── FriendshipDomainService
    └── 양방향 관계 생성/수락 규칙

Storage Layer (chat-storage)
├── ChatFriendshipEntity (JPA Entity)
├── JpaFriendshipRepository
├── FriendshipMapper
└── FriendshipRepositoryAdapter (Adapter)

Application Layer (system-server)
├── FriendshipApplicationService
│   └── Use Case 오케스트레이션
├── FriendshipController (REST)
└── DTOs (Request/Response)

Event Layer (common)
└── Domain Events
```

### 설계 원칙

✅ **Hexagonal Architecture**

- Domain은 외부 의존성 없음
- Storage는 Domain 인터페이스 구현

✅ **Early Return Pattern**

- 모든 비즈니스 규칙에서 일관성 있게 적용

✅ **불변성 (Immutability)**

- Value Object는 final 필드
- Builder 패턴 사용

✅ **명확한 책임 분리**

- Domain: 비즈니스 규칙
- Application: Use Case 조율
- Controller: HTTP 처리

---

## ✨ 핵심 설계 결정

### 1. 양방향 관계 설계

**결정**: 두 개의 Friendship 엔티티로 양방향 표현

**이유**:

- 각 사용자별 독립적인 설정 (별칭, 즐겨찾기)
- 빠른 조회 성능 (사용자별 친구 목록)
- 일방적 차단 구현 가능

**트레이드오프**:

- 저장 공간 2배 (허용 가능)
- 일관성 유지 필요 (Domain Service로 해결)

---

### 2. Domain Service 사용

**결정**: FriendshipDomainService에서 양방향 로직 캡슐화

**이유**:

- User + Friendship 간 협력 필요
- 복잡한 비즈니스 규칙 (양방향 일관성)
- 재사용성 및 테스트 용이성

**코드 예시**:

```java
FriendshipPair friendships =
		friendshipDomainService.requestFriendship(requester, target);
// → 양방향 관계 자동 생성
```

---

### 3. Event-Driven Integration

**결정**: Spring ApplicationEvent 사용

**이유**:

- 느슨한 결합 (Loose Coupling)
- 확장 가능 (새로운 리스너 추가 용이)
- 트랜잭션 경계 제어 가능

**예시 플로우**:

```
1. 친구 수락
2. FriendAcceptedEvent 발행
3. → Push 알림 (비동기)
4. → 1:1 채팅방 자동 생성 (비동기)
```

---

## 🧪 테스트 준비 완료

### 테스트 가능한 레이어

✅ **Domain Layer**

- Friendship 단위 테스트
- FriendshipDomainService 테스트
- 비즈니스 규칙 검증

✅ **Storage Layer**

- Repository Adapter 통합 테스트
- Mapper 테스트

✅ **Application Layer**

- FriendshipApplicationService 테스트
- Event 발행 검증

✅ **Controller Layer**

- REST API 통합 테스트
- Validation 테스트

---

## 📈 성능 고려사항

### 인덱스 전략

```sql
-- 친구 목록 조회 (가장 빈번)
CREATE INDEX idx_user_status ON chat_friendships (user_id, status);

-- 받은 요청 조회
CREATE INDEX idx_friend_id ON chat_friendships (friend_id);
```

**예상 성능**:

- 친구 목록 조회: O(log n) - B-Tree 인덱스
- 친구 요청: O(1) - PK 조회

---

## 🔒 보안 및 검증

### 입력 검증

✅ **Bean Validation**

```java

@NotBlank(message = "Friend ID is required")
private String friendId;

@Size(max = 100)
private String nickname;
```

### 권한 검증

✅ **사용자별 액션 제한**

- 친구 수락: 요청 받은 사람만 가능
- 별칭 설정: 자신의 친구만 가능
- 차단: 자신의 관계만 가능

### 비즈니스 규칙 검증

✅ **Domain Layer에서 검증**

```java
if(userId.equals(friendId)){
		throw new

DomainException("Cannot add yourself");
}

		if(!user.

canSendMessage()){
		throw new

DomainException("User not active");
}
```

---

## 🚀 다음 단계 (Phase 2)

### 채팅방 메타데이터 시스템

**목표**:

- 사용자별 채팅방 설정
- 읽지 않은 메시지 수 추적
- 마지막 읽은 위치 저장
- 채팅방 즐겨찾기/알림 설정

**예상 작업**:

1. ChannelMetadata Aggregate 설계
2. 읽기 상태 추적 로직
3. CQRS 패턴 적용 (복잡한 조회)
4. Redis 캐싱 통합

---

## 🎓 배운 점 & 인사이트

### DDD 설계의 장점

✅ **명확한 경계**

- Aggregate는 일관성 경계
- Repository는 Aggregate 단위로만

✅ **도메인 로직 집중**

- Infrastructure 걱정 없이 비즈니스 규칙 구현
- 테스트 용이성 증가

### 양방향 관계의 trade-off

✅ **장점**:

- 조회 성능 향상
- 독립적인 사용자 설정

⚠️ **주의점**:

- 일관성 유지 필요 (Domain Service 필수)
- 저장 공간 2배

---

## 📚 참고 자료

### DDD 패턴

- Aggregate Root: Friendship
- Value Object: FriendshipId
- Domain Service: FriendshipDomainService
- Repository Pattern: FriendshipRepository

### 아키텍처 패턴

- Hexagonal Architecture (Ports & Adapters)
- Event-Driven Architecture
- CQRS (다음 Phase에서 본격 적용)

---

## ✅ 체크리스트

- [x] Domain 모델 설계
- [x] Repository 인터페이스 정의
- [x] JPA Entity 매핑
- [x] Database Migration
- [x] Application Service 구현
- [x] REST Controller 구현
- [x] DTO 설계
- [x] Event 정의
- [x] 빌드 성공 확인
- [ ] 단위 테스트 작성 (다음 작업)
- [ ] 통합 테스트 작성 (다음 작업)
- [ ] API 문서 자동화 (Swagger)

---

**Phase 1 완료!** 🎉

**다음**: Phase 2 - 채팅방 메타데이터 시스템  
**문서 작성**: AI Assistant  
**완료일**: 2026-02-17
