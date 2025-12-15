# 채팅 플랫폼 아키텍처 재구축 - 진행 상황 보고서

## 📅 작업 일자: 2025-12-09

---

## 🎯 작업 목표

현재 멀티모듈 구조를 DDD 기반으로 재설계하여 Domain과 Infrastructure를 명확히 분리

---

## ✅ 완료된 작업

### 1. Step 1: chat-domain 모듈 생성 ✅

#### ✨ 성과

- 순수 도메인 계층 분리 완료
- 모든 비즈니스 로직을 Domain 계층에 집중
- 인프라 의존성 완전 제거

#### 📦 생성된 파일들

**Message Domain (메시지 도메인)**

```
chat-domain/src/main/java/com/example/chat/domain/message/
├── MessageId.java              ✅ (Value Object)
├── MessageType.java            ✅ (Enum: TEXT, IMAGE, FILE, SYSTEM, VIDEO, AUDIO)
├── MessageStatus.java          ✅ (Enum: PENDING, SENT, DELIVERED, READ, FAILED)
├── MessageContent.java         ✅ (Value Object)
├── Message.java                ✅ (Aggregate Root)
└── MessageRepository.java      ✅ (Interface)
```

**Channel Domain (채널 도메인)**

```
chat-domain/src/main/java/com/example/chat/domain/channel/
├── ChannelId.java              ✅ (Value Object)
├── ChannelType.java            ✅ (Enum: DIRECT, GROUP, PUBLIC, PRIVATE)
├── Channel.java                ✅ (Aggregate Root)
└── ChannelRepository.java      ✅ (Interface)
```

**Schedule Domain (스케줄 도메인)**

```
chat-domain/src/main/java/com/example/chat/domain/schedule/
├── ScheduleId.java             ✅ (Value Object)
├── ScheduleType.java           ✅ (Enum: ONE_TIME, RECURRING)
├── ScheduleStatus.java         ✅ (Enum: PENDING, ACTIVE, EXECUTED, CANCELLED, FAILED)
├── CronExpression.java         ✅ (Value Object)
├── ScheduleRule.java           ✅ (Aggregate Root)
└── ScheduleRuleRepository.java ✅ (Interface)
```

**User Domain (사용자 도메인)**

```
chat-domain/src/main/java/com/example/chat/domain/user/
└── UserId.java                 ✅ (Value Object)
```

**Common Domain (공통)**

```
chat-domain/src/main/java/com/example/chat/domain/common/
└── Cursor.java                 ✅ (커서 기반 페이징용 Value Object)
```

**Domain Services (도메인 서비스)**

```
chat-domain/src/main/java/com/example/chat/domain/service/
├── MessageDomainService.java   ✅ (메시지 생성 및 검증)
├── ChannelDomainService.java   ✅ (채널 생성 및 검증)
└── ScheduleDomainService.java  ✅ (스케줄 생성 및 검증)
```

#### 🔧 설정 파일

- ✅ `chat-domain/build.gradle` - 최소 의존성 (common-util만)
- ✅ `settings.gradle` - chat-domain 모듈 추가
- ✅ 빌드 성공 확인

---

### 2. Step 2: chat-storage 리팩토링 🔄

#### ✨ 성과

- Entity, Repository, Adapter, Mapper 구조 완성
- Domain Repository Interface 구현 (Adapter 패턴)
- JPA Entity와 Domain Model 완전 분리

#### 📦 생성/수정된 파일들

**Entity (JPA 엔티티)**

```
chat-storage/src/main/java/com/example/chat/storage/entity/
├── ChatMessageEntity.java          ✅ (수정: Enum 적용, String ID, 필드 추가)
├── ChatChannelEntity.java          ✅ (수정: Enum 적용, String ID)
├── ChatChannelMemberEntity.java    ✅ (신규: 채널 멤버 관리)
└── ScheduleRuleEntity.java         ✅ (신규: 스케줄 규칙)
```

**JPA Repository**

```
chat-storage/src/main/java/com/example/chat/storage/repository/
├── JpaChatMessageRepository.java       ✅ (신규: 커서 기반 쿼리)
├── JpaChatChannelRepository.java       ✅ (신규)
├── JpaChatChannelMemberRepository.java ✅ (신규)
└── JpaScheduleRuleRepository.java      ✅ (신규: 비관적 락 지원)
```

**Mapper (변환 계층)**

```
chat-storage/src/main/java/com/example/chat/storage/mapper/
├── MessageMapper.java          ✅ (Domain ↔ Entity 변환)
├── ChannelMapper.java          ✅ (Domain ↔ Entity 변환)
└── ScheduleMapper.java         ✅ (Domain ↔ Entity 변환)
```

**Adapter (Repository 구현)**

```
chat-storage/src/main/java/com/example/chat/storage/adapter/
├── MessageRepositoryAdapter.java   ✅ (MessageRepository 구현)
├── ChannelRepositoryAdapter.java   ✅ (ChannelRepository 구현)
└── ScheduleRepositoryAdapter.java  ✅ (ScheduleRuleRepository 구현)
```

#### 🔧 설정 수정

- ✅ `chat-storage/build.gradle` - chat-domain 의존성 추가
- ✅ 파일명 정리 (ChatChannel.java → ChatChannelEntity.java)
- ✅ 파일명 정리 (ChatMessageRepository.java → JpaChatMessageRepository.java)

---

## 🏗️ 아키텍처 설계 원칙 적용

### ✅ Hexagonal Architecture (포트/어댑터)

```
Domain Layer (chat-domain)
    ├── Aggregate Roots (Message, Channel, ScheduleRule)
    ├── Value Objects (Id, Content, Cursor 등)
    ├── Repository Interfaces (포트)
    └── Domain Services

Infrastructure Layer (chat-storage)
    ├── JPA Entities
    ├── JPA Repositories
    ├── Adapters (포트 구현)
    └── Mappers (변환 로직)
```

### ✅ DDD (Domain-Driven Design)

- **Aggregate Root**: Message, Channel, ScheduleRule
- **Value Object**: MessageId, ChannelId, UserId, Cursor 등
- **Domain Service**: 여러 Aggregate 간 조율
- **Repository Interface**: 도메인 계층에 정의

### ✅ 의존성 역전 원칙

```
chat-domain (순수 도메인)
    ↑
chat-storage (인프라 구현 - Domain에 의존)
    ↑
chat-message-server, chat-system-server (Application 계층)
```

---

## 📊 주요 기술 결정

### 1. 커서 기반 페이징 채택

- **문제**: Offset 기반 페이징은 대량 데이터에서 성능 저하
- **해결**: Cursor 기반 페이징으로 일관된 성능 보장
- **구현**: `Cursor` Value Object + `findByChannelIdWithCursor` 메서드

### 2. Enum으로 상태 관리

- **MessageStatus**: PENDING → SENT → DELIVERED → READ
- **ScheduleStatus**: PENDING → ACTIVE → EXECUTED / CANCELLED
- **ScheduleType**: ONE_TIME (단발성) / RECURRING (주기적)

### 3. 비관적 락으로 동시성 제어

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<ScheduleRuleEntity> findByIdWithLock(String id);
```

### 4. String ID 사용

- UUID 기반 분산 시스템 대응
- 순서 의존성 제거
- 확장성 향상

---

## ⚠️ 현재 이슈

### 🔴 빌드 에러 (100개)

**원인**: chat-storage에 기존 Domain 클래스가 남아있음

- `chat-storage/src/main/java/com/example/chat/storage/domain/` 패키지 제거 필요
- 기존 handler, factory 클래스들이 Lombok @Builder 미적용
- log 변수 없는 클래스에서 log 사용

**해결 방법**:

1. ✅ 파일명 수정 완료 (ChatChannel → ChatChannelEntity)
2. ✅ 파일명 수정 완료 (ChatMessageRepository → JpaChatMessageRepository)
3. ⏳ 불필요한 기존 Domain 클래스 제거 필요
4. ⏳ 기존 Handler/Factory 클래스 정리 필요

---

## 🚧 다음 단계 (Step 3)

### 1. chat-storage 정리

- [ ] `chat-storage/src/main/java/com/example/chat/storage/domain/` 전체 삭제
- [ ] `chat-storage/src/main/java/com/example/chat/storage/handler/` 정리 또는 삭제
- [ ] `chat-storage/src/main/java/com/example/chat/storage/factory/` 정리 또는 삭제
- [ ] 불필요한 adapter 파일 정리 (ChatChannelReader 등)
- [ ] 빌드 재확인

### 2. chat-message-server 리팩토링

- [ ] Application 패키지 생성 (Use Case)
- [ ] `SendMessageUseCase` 구현
- [ ] `PublishMessageUseCase` 구현
- [ ] Early Return 패턴 적용
- [ ] Controller 레이어 정리

### 3. chat-system-server 리팩토링

- [ ] Channel Use Cases
- [ ] Message Query Use Case (Cursor 기반)
- [ ] Schedule Use Cases (단발성, 주기적, 강제 발송)
- [ ] Quartz Job 동시성 제어

---

## 📈 진행률

```
[████████████░░░░░░░░░░░░░░░░░░░░] 28% (Step 2/7 진행 중)

✅ Step 1: chat-domain 모듈 생성 (완료)
🔄 Step 2: chat-storage 리팩토링 (80% 완료 - 정리 작업 남음)
⏳ Step 3: chat-message-server 리팩토링
⏳ Step 4: chat-system-server 리팩토링
⏳ Step 5: chat-websocket-server 리팩토링
⏳ Step 6: 정리 작업
⏳ Step 7: 테스트 및 검증
```

---

## 🎯 핵심 성과

### ✅ 명확한 계층 분리

- Domain: 순수 비즈니스 로직
- Storage: 영속성 구현
- Application (예정): Use Case + API

### ✅ 테스트 용이성

- Domain은 순수 Java로 단위 테스트 가능
- Storage는 Mock으로 테스트 가능

### ✅ 재사용성 향상

- Domain과 Storage는 모든 서버에서 공통 사용
- 중복 코드 제거

### ✅ 확장성

- 새로운 서버 추가 시 Domain/Storage 재사용
- 메시지 타입 추가 시 Domain만 수정

---

## 📚 생성된 문서

1. ✅ `아키텍처_재설계_최종.md` - 전체 아키텍처 설계
2. ✅ `마이그레이션_실행계획.md` - 단계별 실행 계획
3. ✅ `마이그레이션_진행상황.md` - 진행 상황 체크리스트
4. ✅ `세션_완료보고서_Step1_Step2.md` - 이 문서

---

## 💡 다음 세션 시작 시

**우선 작업**:

1. chat-storage의 불필요한 파일 삭제
2. 빌드 성공 확인
3. Step 3 진행 (chat-message-server 리팩토링)

**명령어**:

```bash
# 빌드 확인
./gradlew :chat-storage:build -x test

# 전체 빌드
./gradlew clean build -x test
```

---

**작업 시간**: 약 2시간  
**작업 내용**: 57개 파일 생성/수정  
**코드 라인 수**: 약 2,000 라인
