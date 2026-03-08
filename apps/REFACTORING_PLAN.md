# Chat Platform 리팩토링 계획

> 작성일: 2026-03-07  
> 대상: `chat-platform` 전체 모듈  
> 목표: SOLID 위반 제거 + 과도한 클린 아키텍처 → 실용적 레이어드 아키텍처 전환 + 서버 통합 결정

---

## 1. 현황 진단

### 현재 레이어 구조

chat-platform/
├── common/ (core / logging / security / web)
└── apps/
├── auth-server
└── chat/
├── libs/
│ ├── chat-domain # DDD Aggregate Root + Domain Service
│ └── chat-storage # JPA Entity + Adapter + Mapper
├── message-server # 메시지 송수신 + Kafka
├── system-server # 채널/친구/메타데이터
└── websocket-server # WebSocket 브로드캐스트

### 핵심 문제 요약

| 구분 | 문제 |
|------|------|
| 과도한 레이어 | Domain → DomainService → AppService → Adapter → Mapper → JPA (6단계) |
| 불필요한 분리 | chat-domain 이 JPA 없는 순수 도메인이지만 Entity 와 1:1 대응 |
| Mapper 과잉 | 단순 필드 복사 수준의 Mapper 레이어 |
| Domain Service 오용 | 단순 검증 + 팩토리 수준을 DomainService 로 분리 |
| SRP 위반 | FriendshipApplicationService (313줄), ChannelQueryService (246줄) |
| OCP 위반 | MessageDomainService.createXxxMessage() - 타입 추가 시 클래스 수정 |
| DIP 위반 | FriendshipDomainService 에 @Service - 도메인이 Spring 에 의존 |
| 미완성 코드 | cursor 페이징 FIXME 방치 - 전체 조회 후 자르기 (OOM 위험) |

---

## 2. chat-domain TODO

- [x] `FriendshipDomainService` `@Service` 제거
- [x] `MessageDomainService` createText/Image/FileMessage → `Message.create(channel, sender, content)` 단일 메서드 통합
- [x] `ChannelDomainService` 반복 검증 → `validateAndCreate()` 내부 메서드 통합
- [x] `UserId`, `ChannelId`, `MessageId`, `FriendshipId` → `record` 타입 교체, `getValue()` 중복 제거
- [x] `Channel.getMembers()` → `Collections.unmodifiableSet()` 반환
- [x] 아키텍처 전환 후 모듈 전체 제거 완료 (디렉토리 삭제 + settings.gradle 주석 처리 + 빌드 성공 확인)

---

## 3. chat-storage TODO

- [x] `ChatMessageEntity` `@Setter` 제거, `markAsSent()` / `markAsDelivered()` / `markAsRead()` / `markAsFailed()` 추가
- [x] `ChatChannelEntity` `@Setter` 제거, `deactivate()` / `activate()` / `updateName()` / `updateDescription()` 추가
- [x] `ChatFriendshipEntity` `@Setter` 제거, `accept()` / `block()` / `unblock()` / `toggleFavorite()` / `updateNickname()` 추가
- [x] `UserEntity` `@Setter` 제거, `suspend()` / `ban()` / `activate()` / `updateLastActive()` 추가
- [x] `MessageRepositoryAdapter.findByChannelId()` cursor 기반 페이징 구현
- [x] Adapter `@Transactional` 중복 → Application Service 트랜잭션 경계 일원화
- [x] Mapper / Adapter 제거 완료 (Phase 2-3)
- [x] `db/migration` 스크립트 존재 확인 (V1~V8)

---

## 4. message-server TODO

- [x] `SendMessageRequest` Bean Validation 추가, Application Service 수동 검증 제거
- [x] `createMessageByType()` switch 분기 → `MessageContent` sealed interface 패턴 매칭으로 제거
- [x] `StoragePropertiesConfig` 제거 → `application.properties` 직접 통합
- [x] `MessageApplicationService` → `MessageSendService` + `MessageQueryService` 분리
- [x] 메시지 조회 API 구현 (`GET /api/messages/{channelId}?cursor=...&limit=...`)

---

## 5. system-server TODO

- [x] `FriendshipApplicationService` → `FriendshipCommandService` + `FriendshipQueryService` 분리
- [x] `ChannelQueryService.getChannelList()` 7단계 → 각 책임별 private 메서드 분리
- [x] `ChannelQueryService` 필터/정렬/페이징 → DB 쿼리 레벨 (`Pageable` + `JpaChannelRepository.findByMemberIdWithFilters`)
- [x] `ChannelMetadataApplicationService.getOrCreateMetadata()` 조회/생성 트랜잭션 분리
- [x] 예외 클래스 → `common/core` `BaseException` 상속 통합 (`ChatErrorCode` 추가)
- [x] `DomainServiceConfig` → 제거 완료
- [x] `ChannelCommandService` + `ChannelCommandController` 구현 (채널 생성/비활성화/멤버 관리)

---

## 6. 아키텍처 전환 계획 (Clean → Layered)

### 현재 vs 목표

현재: Controller → AppService → DomainService → Domain(POJO) → Adapter → Mapper → JPA Entity
목표: Controller → AppService → JPA Entity (Rich Domain) → Spring Data Repository

### 전환 시 유지할 것

| 유지 | 이유 |
|------|------|
| Application Service | 트랜잭션 경계, 오케스트레이션 |
| Request/Response DTO (record) | Controller ↔ Service 계약 |
| `MessageContent` sealed interface | 메시지 타입 다형성 - 실제 도메인 가치 |
| Entity 비즈니스 메서드 | 상태 변경 캡슐화 |

### 전환 순서

Phase 1: Entity 강화 - @Setter 제거, 비즈니스 메서드 추가
Phase 2: Adapter 제거 - JPA Repository 직접 주입
Phase 3: Mapper 제거 - Entity 에 from()/toResponse() 추가
Phase 4: chat-domain 모듈 제거
Phase 5: 도메인 Repository 인터페이스 제거

### TODO - 아키텍처 전환

- [x] **Phase 1** 전체 Entity 강화 (위 3, 4, 5장 TODO 동일)
- [x] **Phase 2** `MessageRepositoryAdapter` 삭제 → `JpaMessageRepository` 직접 주입
- [x] **Phase 2** `ChannelRepositoryAdapter`, `FriendshipRepositoryAdapter`, `UserRepositoryAdapter` 동일
- [x] **Phase 3** 모든 Mapper 클래스 삭제
- [x] **Phase 4** enum/sealed 타입을 `chat-storage` 또는 `common/core`로 이동
  - [x] `ChannelType`, `MessageType`, `MessageStatus`, `FriendshipStatus`, `UserStatus` → `chat-storage.enums` 패키지로 이동
  - [x] `MessageContent` sealed interface → `chat-server` 내부 `message.domain` 패키지로 이동 (도메인 가치 유지)
  - [x] `DomainException` → `ChatException` (`BaseException` 상속)으로 완전 대체
  - [x] `chat-storage` build.gradle에서 `chat-domain` 의존성 제거
  - [x] `chat-server` build.gradle에서 `chat-domain` 의존성 제거
  - [x] `websocket-server` build.gradle에서 `chat-domain` 의존성 제거
- [x] **Phase 5** `settings.gradle` `:apps:chat:libs:chat-domain` 주석 처리 완료
  - [x] `message-server`, `system-server`, `chat-domain` 디렉토리 물리적 삭제 완료 (빌드 성공 확인)
  - [x] `adapter/`, `mapper/` 빈 패키지 삭제, 미사용 `DataSourceContextHolder` 삭제

---

## 7. message-server vs system-server 통합 검토

### 현재 역할

| 서버 | 기능 | 포트 |
|------|------|------|
| message-server | 메시지 전송 + Kafka | 20001 |
| system-server | 채널/친구/메타데이터 CRUD | 20003 |
| websocket-server | WebSocket + Redis Pub/Sub | 별도 |

### 판단

두 서버가 **같은 DB, 같은 chat-storage/chat-domain 모듈을 공유**하므로  
물리적 분리의 실익이 없다. 개발팀 단일 구성, 운영 오버헤드 감소를 위해 **통합 권장**.

`websocket-server` 는 WebSocket 연결 수 기반 독립 스케일링이 필요하므로 **분리 유지**.
websocket-server (유지)

### TODO - 서버 통합

- [x] `apps/chat/chat-server/` 모듈 생성, `build.gradle` 작성
- [x] `message-server` Controller / Service / Config 이전
- [x] `system-server` Controller / Service / Config 이전
- [x] 배치/스케줄 코드 이전 → 확인 결과 system-server 에 스케줄 구현 없음 (chat-domain 의 테스트용 최소 구현만 존재, 이전 불필요)
- [x] Kafka / Redis / Security / JPA Config 통합
- [x] `message-server`, `system-server` 모듈 디렉토리 삭제 완료 (이전 완료 최종 확인 후)
- [x] `settings.gradle` 반영 (chat-server 추가, message/system-server 주석 처리)
- [x] API Gateway 라우팅 설정 업데이트 (CHAT-SERVER 단일 서비스 ID)

---

## 8. 공통 모듈 TODO

- [x] `security.jwt.secret-key` → Config Server 또는 환경변수 통일, 각 서버 하드코딩 제거
- [x] `common/logging` Reactive TracingFilter → 환경 자동 감지 (Servlet/Reactive), 별도 분리 불필요
- [x] `system-server` 예외 클래스 → `common/core` `BaseException` 상속 통합 (`ResourceNotFoundException`, `BusinessException`, `SchedulingException`)
- [x] `common/core` `BaseException` + `ErrorCode` 계층 정비 (`ChatErrorCode` 추가)

---

## 9. 전체 TODO 요약

### 🔴 즉시 (런타임 위험 / 코드 품질)

- [x] `MessageRepositoryAdapter.findByChannelId()` cursor 페이징 구현 (전체 조회 후 자르기 제거)
- [x] 전체 Entity `@Setter` 제거 + 상태 변경 메서드 추가
- [x] `FriendshipDomainService` `@Service` 제거
- [x] `SendMessageRequest` Bean Validation, Application Service 수동 검증 제거
- [x] `FriendshipApplicationService` → Command / Query 분리

### 🟡 중기 (아키텍처)

- [x] Phase 1-3: Entity 강화 → Adapter 제거 → Mapper 제거
  - [x] Phase 1: Entity 강화 (`@Setter` 제거, 비즈니스 메서드 추가)
  - [x] Phase 2: Adapter 제거 - JPA Repository 직접 주입 (`message-server`, `system-server`)
  - [x] Phase 3: Mapper 클래스 삭제 (`chat-storage`의 `mapper`/`adapter` 패키지 전체 제거)
- [x] `MessageDomainService` 타입별 메서드 → 단일 메서드 통합
- [x] `ChannelDomainService` `validateAndCreate()` 통합
- [x] `ChannelQueryService` N+1 제거, 책임별 메서드 분리 (배치 조회 적용)
- [x] `ChannelQueryService` 필터/정렬/페이징 → DB 쿼리 레벨 (`Pageable`)
- [x] `ChannelMetadataApplicationService` 트랜잭션 분리 (class readOnly, 쓰기 메서드 개별 지정)
- [x] 예외 클래스 통합 (`ChatErrorCode` + `BaseException` 상속)
- [x] `MessageApplicationService` → `MessageSendService` + `MessageQueryService` 분리
- [x] 메시지 조회 API (`GET /api/messages/{channelId}`)
- [x] `ChannelCommandService` + `ChannelCommandController` 구현

### 🟢 장기 (구조 변경)

- [x] Phase 4: `ChannelType`, `MessageType`, `MessageStatus`, `FriendshipStatus`, `UserStatus` → `common/core.enums`로 이동 (websocket-server가 JPA 불필요)
- [x] Phase 4: `MessageContent` sealed interface → `chat-server` 내부로 이동
- [x] Phase 4: `DomainException` → `ChatException` (`BaseException` 상속)으로 완전 대체
- [x] Phase 4: 모든 서버(chat-server, websocket-server, chat-storage)에서 `chat-domain` 의존성 제거
- [x] Phase 5: `settings.gradle`에서 `chat-domain` 주석 처리 완료
- [x] Phase 5 최종: `message-server`, `system-server`, `chat-domain` 디렉토리 물리적 삭제 완료 (빌드 성공 확인)
- [x] message-server + system-server → chat-server 통합 완료
- [x] 배치/스케줄 이전 불필요 확인 (system-server 에 스케줄 구현 없음)
- [x] JWT secret 환경변수 외부화 완료, Config Server import 준비
- [x] Config Server 활성화 (`spring.config.import=optional:configserver:...`) 및 각 서버별 yml 추가 (auth-server.yml, chat-server.yml, chat-websocket-server.yml)
- [x] `ChannelQueryService` 메모리 필터 → JPQL LEFT JOIN DB 레벨 전환 (favorite/pinned/unread)

---

*이 문서는 리팩토링 진행에 따라 완료된 항목을 체크하며 지속적으로 업데이트한다.*
