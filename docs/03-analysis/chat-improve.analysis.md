# Gap Analysis: chat-improve

> **Feature**: 채팅 애플리케이션 전면 고도화
> **Analysis Date**: 2026-03-25
> **Analyst**: gap-detector Agent

---

## Overall Scores

| Category | Score | Status |
|----------|:-----:|:------:|
| Plan Completion (P0+P1 scope) | 25% | ❌ |
| Design Match (구현된 코드 기준) | 78% | ⚠️ |
| Architecture Compliance | 82% | ⚠️ |
| Convention Compliance | 90% | ✅ |
| **Overall Match Rate** | **52%** | ❌ |

---

## Spec Checklist 검증

### P0 — 즉시 수정 (프로덕션 블로커)

| ID | 항목 | 상태 | 비고 |
|----|------|:----:|------|
| BE-P0-1 | parseUserFromToken 에러 수정 (FE) | ❌ | FE 범위 — chat-view 변경 없음 |
| BE-P0-2 | PushMessage @Setter 제거 | ✅ | `@Setter` 제거 완료; `markProcessing()`, `markCompleted()`, `markFailed()` 도메인 메서드로 교체 |
| BE-P0-3 | FriendshipController @CurrentUser 통일 | ✅ | `@RequestHeader("X-User-Id")` → `SecurityUtils.getCurrentUserId()`로 교체 |
| FE-P0-1 | mock data 스토어 분리 | ❌ | FE 변경 없음 |
| FE-P0-2 | WebSocket 토큰 보안 개선 | ❌ | FE/WS 인증 변경 없음 |
| BE-P0-4 | Rate Limiting 구현 | ❌ | Bucket4j 미추가, `RateLimitGlobalFilter` 없음 |
| BE-P0-5 | 글로벌 ExceptionHandler 통일 | ✅ | `GlobalExceptionHandler` 기존 존재 — `BaseException`, 유효성, HTTP, 일반 예외 처리 |

**P0 Score: 3/7 (42.9%)**

### P1 — 미완성 핵심 기능

| ID | 항목 | 상태 | 비고 |
|----|------|:----:|------|
| BE-P1-1 | 예약 발송 기능 완성 | ⚠️ 70% | 핵심 구조 구현됨; 도메인 이벤트, 실제 메시지 발송 연동, 재시도 재스케줄링 미구현 |
| BE-P1-2 | 파일 업로드 API | ❌ | `file/` 패키지 없음 |
| BE-P1-3 | 타이핑 인디케이터 WebSocket | ❌ | `typing/` 패키지 없음 |
| FE-P1-1 | 예약 발송 UI | ❌ | FE 컴포넌트 변경 없음 |
| FE-P1-2 | 파일 업로드 진행률 UI | ❌ | FE 컴포넌트 변경 없음 |
| FE-P1-3 | 타이핑 인디케이터 UI | ❌ | FE 컴포넌트 변경 없음 |
| FE-P1-4 | 가상 스크롤 구현 | ❌ | FE 변경 없음 |
| FE-P1-5 | 오프라인 메시지 큐 | ❌ | FE 변경 없음 |

**P1 Score: ~0.7/8 (8.75%)**

### P2/P3 — 현재 구현 범위 외

P2 (6개) 및 P3 (5개) 항목은 모두 미구현 — 별도 SDD 기반 구현 예정.

---

## Gap 목록 — BE-P1-1 예약 발송 (주요 구현 대상)

### ❌ 미구현 항목 (Design에 있으나 구현 없음)

| # | 항목 | 심각도 | 설명 |
|---|------|:------:|------|
| G-01 | 실제 메시지 발송 연동 | Critical | `executeScheduledMessage()`에 TODO만 존재 — `MessageCommandService` 연동 없이 상태 전이만 수행 |
| G-02 | `@DisallowConcurrentExecution` | High | Quartz 중복 실행 방지 어노테이션 `ScheduledMessageJob`에 누락 |
| G-03 | 실패 시 재시도 재스케줄링 | High | `markFailed()` 호출 후 `isRetryable()==true`일 때 Quartz 재등록 로직 없음 |
| G-04 | 실행 시 채널 멤버십 재검증 | Medium | Job 실행 시점에 채널 가입 여부 재확인 로직 없음 |
| G-05 | `ScheduledMessageDomainService` | Medium | Design에 명시된 도메인 서비스 클래스 미생성 (검증 로직이 CommandService에 혼재) |
| G-06 | `event/MessageScheduledEvent` | Medium | 예약 생성 시 도메인 이벤트 발행 없음 |
| G-07 | `event/ScheduledMessageExecutedEvent` | Medium | 실행 완료 시 도메인 이벤트 발행 없음 |

### ⚠️ 불일치 항목 (Design ≠ Implementation)

| # | 항목 | Design | Implementation | 영향도 |
|---|------|--------|----------------|:------:|
| D-01 | 패키지명 | `api/controller/` | `rest/controller/` | None (기존 코드베이스 컨벤션 따름) |
| D-02 | Request body 구조 | 중첩 `content: { type, text, mediaUrl }` | 플랫 필드 `contentType`, `text`, `mediaUrl`, `fileName`, `fileSize`, `mimeType` | Medium (API 계약 불일치) |
| D-03 | Response body 구조 | 중첩 `content` 객체 | 플랫 필드 + `retryCount` 추가 | Medium (프론트엔드 적응 필요) |
| D-04 | 시간 검증 에러코드 | `SCHEDULED_AT_PAST` + `SCHEDULED_AT_TOO_FAR` (별도) | 단일 `SCHEDULE_INVALID_TIME` (CHAT-SCH-002) | Low |
| D-05 | 에러코드 `SCHEDULE_ALREADY_EXECUTED` | HTTP 409 | `SCHEDULE_NOT_CANCELLABLE` (CHAT-SCH-005) HTTP 400 | Low |
| D-06 | 인증 추출 방식 | `@CurrentUser` 어노테이션 | `SecurityUtils.getCurrentUserId()` | None (`@CurrentUser`가 코드베이스에 없음) |

### ✅ 올바르게 구현된 항목

| # | 항목 |
|---|------|
| 1 | `ScheduledMessage` Aggregate Root (필드, 상태 전이, 재시도 로직 SDD와 100% 일치) |
| 2 | `ScheduleStatus` enum — PENDING/EXECUTING/EXECUTED/CANCELLED/FAILED |
| 3 | `ScheduleType` enum — ONCE (Non-Goals에 따라 RECURRING 제외) |
| 4 | Repository Port/Adapter 패턴 (domain interface + infrastructure JPA adapter) |
| 5 | CQRS 분리 — `CommandService` + `QueryService` with Impl |
| 6 | `ScheduledMessageEntity` JPA 매핑 (fromDomain/toDomain, UTC 변환) |
| 7 | `QuartzJobScheduler` — schedule/unschedule, misfire 처리 |
| 8 | `ScheduledMessageJob` — CommandService 위임, `JobExecutionException` 래핑 |
| 9 | V10 Flyway migration — `message_id` nullable, `retry_count`, composite index |
| 10 | API 엔드포인트 3개 — POST/GET/DELETE 모두 Design과 일치 |
| 11 | `ScheduledMessageResponse.from()` factory method 패턴 |
| 12 | `@Transactional(readOnly = true)` on QueryServiceImpl |
| 13 | Quartz 의존성 (`spring-boot-starter-quartz`) 추가 |
| 14 | 일일 한도 10개 검증 |
| 15 | 시간 범위 검증 (now+5분 ~ now+30일) |
| 16 | 테스트 커버리지 33개 — 도메인(14), CommandService(11), Job(3), Entity(5) |

---

## Architecture Compliance

| 검증 항목 | 상태 | 세부 내용 |
|-----------|:----:|----------|
| DDD 레이어 분리 | ✅ | domain/model, domain/repository, application/service, application/job, infrastructure/datasource, infrastructure/quartz, rest/controller, rest/dto |
| CQRS 명명 | ✅ | `*CommandService` (쓰기) + `*QueryService` (읽기) |
| Record DTO | ✅ | `CreateScheduledMessageRequest`, `ScheduledMessageResponse` Java record |
| Factory method 패턴 | ✅ | `ScheduledMessageResponse.from(domain)` |
| 도메인 순수성 | ✅ | `ScheduledMessage` — JPA/Spring 어노테이션 없는 순수 POJO |
| Repository Port/Adapter | ✅ | domain에 인터페이스, infrastructure에 JPA 어댑터 |
| 도메인 이벤트 | ❌ | `event/` 패키지 전체 누락 |
| 도메인 서비스 | ⚠️ | `ScheduledMessageDomainService` 누락 (검증 로직이 CommandServiceImpl에 혼재) |
| 의존성 방향 | ✅ | domain에 infrastructure import 없음; rest → application 인터페이스 |

**Architecture Score: 82%**

---

## Convention Compliance

| 검증 항목 | 상태 | 세부 내용 |
|-----------|:----:|----------|
| JPA 엔티티 @Setter 금지 | ✅ | PushMessage 수정 완료; ScheduledMessageEntity는 @Getter + @NoArgsConstructor만 |
| SecurityUtils 인증 추출 | ✅ | FriendshipController + ScheduledMessageController 모두 준수 |
| @Nested + @DisplayName(한국어) | ✅ | 테스트 파일 4개 모두 패턴 준수 |
| @Valid on request body | ✅ | POST endpoint에 `@Valid @RequestBody` |
| Swagger 어노테이션 | ✅ | `@Tag`, `@Operation`, `@ApiResponse` 모두 작성 |
| 컴파일 게이트 | ✅ | `./gradlew compileJava compileTestJava` 통과 |

**Convention Score: 90%**

---

## 구현 완성도 요약

```
P0 (즉시 수정)        ████████░░░░░░░░░░░░   3/7  (43%)
P1 (핵심 기능)        ██░░░░░░░░░░░░░░░░░░   ~1/8 (12%)
P2 (아키텍처 개선)    ░░░░░░░░░░░░░░░░░░░░   0/6  (0%)  [범위 외]
P3 (추가 기능)        ░░░░░░░░░░░░░░░░░░░░   0/5  (0%)  [범위 외]

P0+P1 전체:           ████░░░░░░░░░░░░░░░░   25%
```

**BE-P1-1 예약 발송 상세:**

```
도메인 모델           ████████████████████  100%
Application Services  ████████████████░░░░   80% (실제 발송 연동 미완)
Infrastructure        ████████████████████  100%
REST Controller/DTO   ████████████████░░░░   85% (request shape 불일치)
Quartz Integration    ████████████████░░░░   80% (retry 재스케줄 없음)
도메인 이벤트         ░░░░░░░░░░░░░░░░░░░░    0%
테스트 커버리지       ████████████████████   95% (33개 테스트)
Flyway Migration      ████████████████████  100%
```

---

## 권장 조치사항

### Critical / High (즉시 수정 권장)

1. **[Critical] 실제 메시지 발송 구현** — `executeScheduledMessage()`에서 `MessageCommandService` 연동하여 실제 메시지 발송
2. **[High] `@DisallowConcurrentExecution` 추가** — `ScheduledMessageJob`에 Quartz 중복 실행 방지
3. **[High] 실패 시 재시도 재스케줄링** — `markFailed()` + `isRetryable()==true` 시 `quartzJobScheduler.schedule(now+30s)` 호출

### Medium (다음 이터레이션)

4. **도메인 이벤트 추가** — `MessageScheduledEvent`, `ScheduledMessageExecutedEvent` 생성 및 발행
5. **Request/Response shape 정렬** — Design 문서 또는 구현 코드 중 하나로 통일
6. **Rate Limiting 구현 (BE-P0-4)** — Bucket4j + `RateLimitGlobalFilter` 추가
