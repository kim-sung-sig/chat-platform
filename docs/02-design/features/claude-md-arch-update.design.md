# Design: CLAUDE.md 역할 재정의 및 아키텍처 설명 개선

**설계일**: 2026-03-20
**Plan 문서**: [claude-md-arch-update.plan.md](../../01-plan/features/claude-md-arch-update.plan.md)

---

## 1. CLAUDE.md 목표 구조

```
# CLAUDE.md
[페르소나 섹션]         ← 신규: AI 에이전트 동작 원칙
[프로젝트 개요]         ← 기존 Architecture Overview → 요약만 유지
[빌드/실행 명령]        ← 유지
[인프라 Docker]         ← 유지 (단축)
[아키텍처 → 링크]       ← 기존 상세 내용 제거, CONVENTIONS.md 링크
[하네스 인덱스]         ← 유지
[스킬 인덱스]           ← 유지
```

### 페르소나 섹션 내용

```markdown
## AI Agent Persona

이 프로젝트에서 Claude는:
- **DDD 아키텍트**로 동작: 모든 코드 제안은 도메인 계층부터 설계
- **컨텍스트 네비게이터**: CLAUDE.md → 인덱스, CONVENTIONS.md → 규칙, SKILL.md → 도구
- **최소 변경 원칙**: 요청된 범위 외 코드 변경 금지
```

### 아키텍처 섹션 (슬림화)

기존의 상세 패키지 트리 제거 → CONVENTIONS.md 링크 + 서비스 테이블만 유지.

---

## 2. CONVENTIONS.md DDD Layering 섹션 교체 명세

### 2.1 교체 대상 (현재)

```
domain/model, service, repository
application/service
infrastructure/kafka, redis, datasource
rest/controller, dto
```

### 2.2 신규 패키지 구조

```
<context>/
├── domain/
│   ├── model/        ← Entity, Value Object, Enum
│   └── service/      ← Domain Service
│
├── application/
│   ├── service/      ← CommandService / QueryService (Use Case)
│   ├── dto/          ← 레이어 간 이동 DTO
│   └── listener/     ← 이벤트 핸들러 (@EventListener)
│
├── event/
│   └── model/        ← 이벤트 페이로드 (불변 record)
│
├── infrastructure/
│   ├── kafka/        ← Kafka 어댑터
│   ├── redis/        ← Redis 어댑터
│   └── datasource/   ← JPA 어댑터
│
└── api/
    ├── controller/   ← REST 컨트롤러
    ├── request/      ← 요청 DTO (record)
    └── response/     ← 응답 DTO (record + factory method)
```

### 2.3 레이어 의존 방향 규칙 (업데이트)

```
api/ → application/ → domain/
infrastructure/ → application/ → domain/
event/model/ ← application/listener/ 참조
```

- `domain/` : 프레임워크 의존 금지 (pure Java)
- `application/` : Spring @Service 허용, JPA 금지
- `api/` : 비즈니스 로직 금지, application layer 위임만

### 2.4 Presentation Layer 규칙 (신규 섹션)

**핵심 원칙**: `api/response/`, `api/request/` 클래스는 모두 `record`

```java
// ✅ 올바른 패턴
public record ChannelResponse(Long id, String name, LocalDateTime createdAt) {
    public static ChannelResponse from(Channel domain) {
        return new ChannelResponse(domain.getId(), domain.getName(), domain.getCreatedAt());
    }
}

// 컨트롤러에서 사용
ChannelResponse response = ChannelResponse.from(channel);  // ✅

// ❌ 금지: 서비스에서 Response 필드 직접 조작
response.setId(channel.getId());  // record이므로 컴파일 에러
```

**규칙 목록**:
1. `record` 사용 — 모든 필드 자동 불변
2. `public static XxxResponse from(DomainObject obj)` factory method 필수
3. 서비스 레이어는 `Response.from(domain)` 호출만 허용, Response 직접 생성 금지
4. `api/request/` record는 `@Valid` 어노테이션 허용 (Bean Validation과 호환)

---

## 3. 완료 기준

- [ ] CLAUDE.md: 페르소나 섹션 추가
- [ ] CLAUDE.md: Architecture Overview → 서비스 테이블 + CONVENTIONS.md 링크만
- [ ] CLAUDE.md: 80줄 이하
- [ ] CONVENTIONS.md: DDD Layering 섹션 교체 (신규 패키지 구조)
- [ ] CONVENTIONS.md: `rest/` → `api/` 전체 교체
- [ ] CONVENTIONS.md: Presentation Layer 규칙 섹션 추가
- [ ] CONVENTIONS.md: `event/model/`, `application/listener/` 정의 추가
