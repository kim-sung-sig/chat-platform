<!-- GENERATED FILE: DO NOT EDIT DIRECTLY
source: .harness/skills/arch-policy/SKILL.md
skill: arch-policy
generated-by: scripts/sync-skills.ps1
-->

---
name: arch-policy
description: >
  Enforce architecture policy and guide architectural decisions for the Chat Platform.
  Use this skill:
    * When starting a new bounded context and deciding package structure
    * When reviewing code for DDD layer violations
    * When choosing between Domain Service vs Application Service (Use Case)
    * When writing ArchUnit tests to automate layer enforcement
    * When an architecture decision needs justification or documentation
---

# Architecture Policy Skill

이 프로젝트는 **Layered Architecture + DDD tactical patterns**을 채택합니다.
Clean/Hexagonal Architecture의 엄격한 순수성을 요구하지 않으며,
`domain/` 레이어에 Spring JPA 어노테이션 사용이 허용됩니다.
**핵심 목표는 레이어별 책임과 경계의 명확성**입니다.

---

## 레이어 구조

```
api/              ← HTTP 진입점 (얇음, 위임만)
application/      ← Use Case 오케스트레이션 (CommandService / QueryService)
domain/           ← 비즈니스 규칙 + 도메인 모델 (Entity, VO, Domain Service)
infrastructure/   ← 기술 어댑터 (JPA, Kafka, Redis, Quartz)
```

**의존 방향**: `api → application → domain`, `infrastructure → domain`

---

## 핵심 경계 원칙

### domain/ 레이어
- **허용**: JPA `@Entity`, `@Column`, `@Id` 등 — 순수 Java 강제 없음
- **필수**: 비즈니스 불변식(invariant)은 반드시 도메인 내에 위치
- **금지**: 외부 시스템 호출 (Kafka produce, Redis 접근, HTTP 호출) — 이건 infrastructure
- **경계 기준**: "이 로직이 DB/메시징과 무관하게 테스트 가능한가?" → Yes면 domain

### domain/service/ vs application/service/ 구분

이것이 이 프로젝트에서 가장 중요한 경계입니다.

| 구분 | domain/service/ | application/service/ (Use Case) |
|------|-----------------|---------------------------------|
| 역할 | 하나의 도메인 개념 내 순수 비즈니스 규칙 | 여러 도메인/인프라를 조합하는 Use Case |
| 의존 | domain/model/ 만 | domain + infrastructure + event |
| 상태 | 무상태 (stateless) | 무상태 |
| 트랜잭션 | 없음 | `@Transactional` 가능 |
| 예시 | `MessageContentValidator`, `ScheduleTimeCalculator` | `ScheduledMessageCommandService` |
| 테스트 | 순수 단위 테스트 (mock 없이) | Mockito mock 사용 |

**판단 기준**:
- Repository / Kafka / Redis를 써야 하나? → `application/service/`
- 도메인 객체 하나만 다루는 순수 계산/검증? → `domain/service/`
- 여러 Aggregate를 조율해야 하나? → `application/service/`

---

## CQRS 경계

```
CommandService  ← 상태 변경 (생성/수정/삭제), 이벤트 발행 허용
QueryService    ← 조회 전용, 상태 변경 금지, cursor-based pagination
```

한 메서드가 쓰기 + 읽기를 동시에 하면 분리 신호.

---

## API 레이어 규칙

- 컨트롤러: `@RestController`, 요청 수신 → application service 위임 → 응답 반환
- 비즈니스 로직 **완전 금지** (if문 하나도 비즈니스 로직이면 서비스로 이동)
- Request DTO: `record` + `@Valid` / `@NotNull` 등 Bean Validation
- Response DTO: `record` + `static from(DomainObj)` factory method

---

## Infrastructure 레이어 규칙

- JPA Repository adapter: domain의 Repository 인터페이스 구현
- Kafka producer/consumer: 메시지 변환 + 라우팅만
- Redis adapter: 캐시/PubSub 접근만
- 비즈니스 로직 **완전 금지** — 기술 번역(translation) 역할만

---

## 이벤트 처리 경계

```
Domain Event 페이로드: event/model/ (불변 record, 순수 데이터)
이벤트 발행:          application/service/ (CommandService에서 ApplicationEventPublisher)
이벤트 소비:          application/listener/ (@EventListener, Use Case와 동일 책임)
금지:                domain/model/ 에서 @EventListener 직접 사용
```

---

## 판단 예시

**"예약 메시지 발송 시간 계산 로직"은 어디?**
- `ScheduledTime.isExpired()` → domain/model/ (VO 내부 메서드)
- `ScheduledTimeCalculator.calcNextFireTime()` → domain/service/ (순수 계산)
- `ScheduledMessageCommandService.createSchedule(request)` → application/service/ (저장 + Quartz 등록)

**"친구 차단 여부 확인"은 어디?**
- `Friendship.isBlocked()` → domain/model/ (Entity 메서드)
- 여러 사용자 조회 후 확인 → application/service/ (QueryService)

---

## ArchUnit 자동화 (선택)

```java
@ArchTest
ArchRule apiNoBusiness =
    noClasses().that().resideInAPackage("..api.controller..")
        .should().haveSimpleNameEndingWith("Service");

@ArchTest
ArchRule domainNoKafka =
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework.kafka..");
```

---

## Architecture Decision Record (ADR)

중요한 설계 결정은 문서화:

**파일**: `docs/conventions/ADR_<slug>.md`

```markdown
## Status: Accepted
## Context: 왜 이 결정이 필요했나?
## Decision: 무엇을 결정했나?
## Consequences: 결과 (긍정/부정/대안)
```

---

## Skill Connection

- 새 기능 설계: `$sdd-craft` → `$sdd-requirements`
- 뼈대 생성: `$spec-to-skeleton`
- 레이어 준수 검증: `$sdd-review`
- 완료 게이트: `$done-gate`
