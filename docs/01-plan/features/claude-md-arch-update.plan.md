# Plan: CLAUDE.md 역할 재정의 및 아키텍처 설명 개선

## Executive Summary

| 항목 | 내용 |
|------|------|
| Feature | `claude-md-arch-update` |
| 날짜 | 2026-03-20 |
| 유형 | 개발 환경 / 아키텍처 문서 개선 |

### 4-Perspective Value Table

| 관점 | 내용 |
|------|------|
| **Problem** | CLAUDE.md가 인덱스·페르소나·아키텍처를 모두 담아 역할이 모호하고, 패키지 구조가 `rest/`·`dto/` 등 실제 코드와 다른 명칭을 사용 |
| **Solution** | CLAUDE.md는 TOC/인덱스·페르소나·프로젝트 요약으로 슬림화, 상세 아키텍처는 CONVENTIONS.md로 위임 + 새 패키지 구조 반영 |
| **Function UX Effect** | AI 에이전트가 짧은 CLAUDE.md로 페르소나·인덱스를 빠르게 로드 후, 필요 시 CONVENTIONS.md를 정확히 참조 |
| **Core Value** | "CLAUDE.md = 나침반, CONVENTIONS.md = 지도" 분리 → 컨텍스트 효율성과 가이드 정확성 동시 향상 |

---

## 1. 현재 상태 (As-Is)

### 1.1 CLAUDE.md 현재 역할 (혼재)

| 섹션 | 현재 위치 | 적합 위치 |
|------|-----------|----------|
| Build & Run Commands | CLAUDE.md | CLAUDE.md (유지) |
| Infrastructure Docker | CLAUDE.md | CLAUDE.md (유지, 단축) |
| Architecture Overview (Services, 패키지 구조) | CLAUDE.md | CONVENTIONS.md로 이동 |
| Event Flow | CLAUDE.md | CONVENTIONS.md로 이동 |
| Key Design Decisions | CLAUDE.md | CONVENTIONS.md로 이동 |
| Development Principles | CLAUDE.md | CLAUDE.md (링크만 유지) |
| Harness 섹션 | CLAUDE.md | CLAUDE.md (유지 — 인덱스 역할) |
| Agent Workflow & Slash Commands | CLAUDE.md | CLAUDE.md (유지 — 인덱스 역할) |

### 1.2 CONVENTIONS.md 현재 패키지 구조

```
<context>/
├── domain/model, service, repository
├── application/service
├── infrastructure/kafka, redis, datasource
└── rest/controller, dto        ← 변경 필요
```

**문제점**:
- `rest/` → `api/`로 명칭 변경 필요
- DTO가 `rest/dto/`에 혼재 → `api/request, /response`로 분리 필요
- `event/` 레이어 미정의
- presentation layer 객체 생성 책임 규칙 미정의
- `application/dto/` 위치 미정의

---

## 2. 목표 상태 (To-Be)

### 2.1 CLAUDE.md 새 역할 (슬림화)

```
CLAUDE.md = 나침반
├── [페르소나] AI 에이전트 기본 동작 원칙 (2-3줄)
├── [프로젝트 요약] 서비스 목록 + 기술 스택 한 줄 요약
├── [빌드/실행 명령] Gradle 명령 (유지)
├── [인프라] Docker one-liner (유지, 단축)
├── [인덱스 — 아키텍처] → CONVENTIONS.md 링크
├── [인덱스 — 하네스] .claude/ 구조 표 (유지)
└── [인덱스 — 스킬] 슬래시 커맨드 목록 (유지)
```

목표 라인 수: **80줄 이하** (현재 150줄 → 약 47% 감소)

### 2.2 새 패키지 구조 (CONVENTIONS.md에 정의)

```
<context>/
├── domain/
│   ├── model/        ← Entity, Value Object, Enum (순수 Java, 프레임워크 의존 금지)
│   └── service/      ← Domain Service (비즈니스 규칙, 상태 없는 순수 로직)
│
├── application/
│   ├── service/      ← Use Case (CommandService / QueryService) + Facade
│   └── dto/          ← 레이어 간 데이터 이동 DTO (ApplicationCommandDto 등)
│   └── listener/     ← 이벤트 핸들러 (ApplicationEvent → 도메인 서비스 오케스트레이션)
│
├── event/
│   └── model/        ← 이벤트 값 객체 (불변 record, 도메인 이벤트 페이로드)
│
├── infrastructure/
│   ├── kafka/        ← Kafka consumer/producer 어댑터
│   ├── redis/        ← Cache + Pub/Sub 어댑터
│   └── datasource/   ← JPA Repository 어댑터 (domain port 구현)
│
└── api/
    ├── controller/   ← REST 엔드포인트 (얇음, application layer 위임만)
    ├── request/      ← HTTP 요청 DTO (record, 불변, 자체 생성 책임)
    └── response/     ← HTTP 응답 DTO (record, 불변, factory method)
```

### 2.3 event/listener 레이어 배치 결정

**`application/listener/` 채택 이유**:
- EventListener = "이벤트를 input으로 받는 ApplicationService"
- HTTP Request → CommandService, Spring Event → EventListener : 동일한 application-layer 책임
- `event/model/`은 순수 이벤트 페이로드(VO)만 보관 → 역할 명확화
- `domain/listener`는 부적합 (도메인 계층에 Spring @EventListener 의존 금지)

### 2.4 Presentation Layer 규칙 (api/ 레이어)

```java
// ✅ 올바른 패턴 — record로 불변, factory method로 생성 책임
public record ChannelResponse(
    Long id,
    String name,
    LocalDateTime createdAt
) {
    // 서비스에서 값 주입 금지 — 객체 스스로 생성 책임
    public static ChannelResponse from(ChannelEntity entity) {
        return new ChannelResponse(entity.getId(), entity.getName(), entity.getCreatedAt());
    }
}

// ❌ 금지 패턴 — 서비스에서 필드 직접 세팅
ChannelResponse response = new ChannelResponse();
response.setId(channel.getId());  // 불변 원칙 위반
```

**규칙 요약**:
- `api/response/` 모든 클래스는 `record` 사용 (불변 보장)
- `api/request/` 모든 클래스는 `record` 사용 (Jackson deserialization 지원 확인)
- 응답 변환: 서비스 → `XxxResponse.from(domainObject)` 호출만 허용
- 서비스 레이어에서 Response 필드를 직접 채우는 코드 금지

---

## 3. 구현 범위

### 3.1 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `CLAUDE.md` | 아키텍처 섹션 제거 → 인덱스·페르소나·링크로 대체 |
| `docs/conventions/CONVENTIONS.md` | DDD Layering 섹션 전면 업데이트 (새 패키지 구조 + presentation 규칙) |

### 3.2 변경하지 않는 것

- 실제 소스 코드 패키지 구조 (문서 변경만, 코드 리팩토링 별도)
- 빌드 명령, Docker 섹션
- Harness 섹션 (.claude/ 구조표)
- 슬래시 커맨드 인덱스

---

## 4. 완료 기준 (Done Criteria)

- [ ] CLAUDE.md 80줄 이하
- [ ] CLAUDE.md에 아키텍처 상세 없음 — CONVENTIONS.md 링크만
- [ ] CLAUDE.md 상단에 AI 페르소나 섹션 추가
- [ ] CONVENTIONS.md DDD Layering 섹션에 새 패키지 구조 반영
- [ ] `event/model/`, `application/listener/`, `api/` 레이어 정의 포함
- [ ] presentation layer record + factory method 규칙 명시
- [ ] `rest/` → `api/` 전체 교체 (CONVENTIONS.md 내)
