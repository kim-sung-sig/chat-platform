---
name: spring-boot
description: >
  Build Spring Boot 3.x applications for this Chat Platform project.
  Use this skill:
    * When developing new bounded contexts following DDD layering (domain/application/event/infrastructure/api)
    * When implementing CQRS: CommandService vs QueryService split
    * When writing REST controllers, JPA entities, repositories, application services
    * When integrating WebSocket/STOMP, Kafka, Redis, Quartz scheduler
    * When writing Flyway migration scripts
    * When configuring Gradle build files or adding dependencies
    * When writing unit/integration tests (JUnit5 + Mockito + Testcontainers)
    * When applying Lombok, SpringDoc OpenAPI, Micrometer tracing
---

# Spring Boot Skill (Chat Platform)

This project uses **Spring Boot 3.x + Java 21 + Gradle**. Apply the rules below for every bounded context.

---

## Tech Stack

| Layer | Tech |
|-------|------|
| Framework | Spring Boot 3.x, Java 21 (virtual threads enabled) |
| Build | Gradle (multi-module monorepo) |
| Persistence | Spring Data JPA + PostgreSQL + Flyway |
| Cache / PubSub | Spring Data Redis |
| Messaging | Spring Kafka |
| WebSocket | Spring WebSocket + STOMP |
| Scheduler | Spring Quartz |
| Security | Spring Security 6 |
| API Docs | SpringDoc OpenAPI 2.x |
| Boilerplate | Lombok (`@Getter`, `@Builder`; avoid `@Data` on JPA entities) |
| Testing | JUnit5, Mockito, Testcontainers (PostgreSQL, Kafka) |

---

## Package Structure (per bounded context)

```
com.example.chat.<context>/
├── domain/
│   ├── model/        ← Entity, Value Object, Enum (JPA 어노테이션 허용, 책임 경계 명확)
│   └── service/      ← Domain Service (단일 도메인 내 순수 비즈니스 규칙, 외부 I/O 금지)
│
├── application/
│   ├── service/      ← XxxCommandService / XxxQueryService (interface + impl)
│   ├── dto/          ← Application 경계 내부 DTO
│   └── listener/     ← @EventListener 핸들러
│
├── event/
│   └── model/        ← 도메인 이벤트 페이로드 (불변 record)
│
├── infrastructure/
│   ├── datasource/   ← JPA Entity, JpaRepository, RepositoryAdapter
│   ├── kafka/        ← Kafka consumer/producer
│   └── redis/        ← Cache / Pub-Sub adapter
│
└── api/
    ├── controller/   ← REST 컨트롤러 (얇게, application layer 위임만)
    ├── request/      ← HTTP 요청 DTO (record + Bean Validation)
    └── response/     ← HTTP 응답 DTO (record + static from() factory)
```

**레이어 의존 방향**: `api → application → domain`, `infrastructure → application → domain`

---

## CQRS Rules

- Write operations → `XxxCommandService` / `XxxCommandServiceImpl`
- Read operations → `XxxQueryService` / `XxxQueryServiceImpl`
- Read queries: cursor-based pagination (offset 금지)
- CommandService는 side effect 허용 (저장, 이벤트 발행)
- QueryService는 순수 조회만 (상태 변경 금지)

---

## DDD Layer Rules

- `domain/model/`: JPA `@Entity`, `@Column` 등 허용. **책임 경계 원칙**: 도메인 불변식(invariant)은 반드시 이 레이어에 위치
- `domain/service/`: 단일 도메인 내 순수 계산/검증 로직. **외부 I/O(Kafka, Redis, HTTP) 금지**
- `application/service/`: `@Service` + `@Transactional` 허용. Use Case 오케스트레이션 (저장, 이벤트 발행 포함)
- `api/controller/`: 비즈니스 로직 **완전 금지** — application service에만 위임
- Response DTO: `public static XxxResponse from(DomainObj)` factory method 필수

---

## Flyway Convention

- Migration 파일: `apps/chat/libs/chat-storage/src/main/resources/db/migration/`
- 파일명: `V{N}__{snake_case_description}.sql`
- 새 마이그레이션은 현재 최대 V번호 + 1

---

## Testing Rules

```java
// 단위 테스트 구조
@ExtendWith(MockitoExtension.class)
@DisplayName("XxxCommandServiceImpl")
class XxxCommandServiceImplTest {
    @Mock XxxRepository repository;
    @InjectMocks XxxCommandServiceImpl sut;

    @Nested
    @DisplayName("create() 메서드")
    class Create {
        @Test @DisplayName("정상 생성 - HappyPath")
        void happyPath() { ... }

        @Test @DisplayName("중복 이름 - Failure")
        void duplicateName() { ... }
    }
}
```

- Mockito: `@Mock` + `@InjectMocks`, 도메인 자체는 mock 금지
- `@DisplayName` 한국어 필수
- Testcontainers: PostgreSQL/Kafka 통합 테스트에 사용

---

## Quartz Scheduler Pattern

```java
// Job 클래스
@Component
public class XxxJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // JobDataMap에서 파라미터 추출
    }
}

// Scheduler 서비스: QuartzJobScheduler 참조
```

---

## Common Error Codes

- `ChatErrorCode` enum (`common/core`)에 에러 코드 추가
- HTTP status는 annotation으로 지정: `@ResponseStatus(HttpStatus.NOT_FOUND)`

---

## Skill Connection

- SDD 작성: `/sdd-requirements`
- 코드 뼈대: `/spec-to-skeleton`
- 테스트 작성: `/skeleton-to-tests`
- 완료 검증: `/done-gate`