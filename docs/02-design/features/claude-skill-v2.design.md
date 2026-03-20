# Design: .claude Skills V2.0 전환

**Plan 참조**: [claude-skill-v2.plan.md](../../01-plan/features/claude-skill-v2.plan.md)
**작성일**: 2026-03-20

---

## 1. 핵심 설계 결정

### 1.1 V2.0 구조: `commands/{skillName}/SKILL.md`

CC는 `.claude/commands/` 아래 2-depth `.md` 파일을 슬래시 커맨드로 인식:
- `commands/jpa/SKILL.md` → `/jpa`
- `commands/sdd/requirements.md` → `/sdd:requirements`

`reference/` 및 `policy/` 는 3-depth 이하에 위치 → CC가 커맨드로 인식하지 않음.

```
.claude/commands/{skillName}/
  SKILL.md          ← CC 슬래시 커맨드 진입점 (/skillName)
  reference/        ← 참조문서 (CC 커맨드 아님, 3-depth)
    *.md
  policy/           ← 규칙/제약 (CC 커맨드 아님, 3-depth)
    *.md
```

> **검증 전제**: CC가 3-depth `.md`를 커맨드로 등록하지 않음을 구현 전 확인 필요.
> 만약 등록된다면 reference/policy 파일을 `.ref` 확장자로 전환.

### 1.2 SDD 스킬체인: 서브커맨드 방식

5개 개별 명령 → `commands/sdd/` 하위 파일로 재편:

| 기존 | 신규 | 호출 |
|------|------|------|
| `/sdd-requirements` | `commands/sdd/requirements.md` | `/sdd:requirements` |
| `/sdd-read` | `commands/sdd/read.md` | `/sdd:read` |
| `/spec-to-skeleton` | `commands/sdd/skeleton.md` | `/sdd:skeleton` |
| `/skeleton-to-tests` | `commands/sdd/tests.md` | `/sdd:tests` |
| `/sdd-review` | `commands/sdd/review.md` | `/sdd:review` |

`commands/sdd/SKILL.md` → `/sdd` : SDD 체인 전체 개요 및 사용 가이드

### 1.3 삭제 대상 확정

| 항목 | 대체 |
|------|------|
| `commands/plan.md` | `/pdca plan {feature}` |
| `commands/develop.md` | `/pdca do {feature}` |
| `commands/qa.md` | `/pdca analyze` + `bkit:zero-script-qa` |
| `commands/review.md` | `bkit:code-review` |
| `commands/sdlc.md` | `/pdca` 전체 워크플로우 |
| `agents/planner.md` | bkit `planner` agent |
| `agents/developer.md` | bkit `developer` agent |
| `agents/qa.md` | bkit `qa` agent |
| `agents/reviewer.md` | bkit `reviewer` agent |

---

## 2. 목표 파일 트리

```
.claude/
  commands/
    jpa/
      SKILL.md                     ← /jpa
      reference/
        JPA_CONVENTIONS.md         ← Entity/Repository 규칙 요약
      policy/
        checklist.md               ← 완료 체크리스트
    sdd/
      SKILL.md                     ← /sdd (체인 개요)
      requirements.md              ← /sdd:requirements
      read.md                      ← /sdd:read
      skeleton.md                  ← /sdd:skeleton
      tests.md                     ← /sdd:tests
      review.md                    ← /sdd:review
      reference/
        SDD_TEMPLATE_GUIDE.md      ← 템플릿 위치/섹션 요약
      policy/
        chain-rules.md             ← SDD 체인 불변 규칙
    explain/
      SKILL.md                     ← /explain
    patch/
      SKILL.md                     ← /patch
      policy/
        scope-rules.md             ← 최소 변경 범위 제약
    perf/
      SKILL.md                     ← /perf
      reference/
        hotspot-checklist.md       ← N+1, 인덱스, 캐시 체크리스트
    docs/
      SKILL.md                     ← /docs
    refactor/
      SKILL.md                     ← /refactor
      policy/
        refactor-rules.md          ← 행동 변경 금지 등 규칙
    security/
      SKILL.md                     ← /security
      reference/
        security-checklist.md      ← OWASP 항목 및 프로젝트 특화 체크
  agents/                          ← (모두 삭제 — bkit agents로 대체)
  hooks/                           ← 변경 없음
  settings.json                    ← 변경 없음
  settings.local.json              ← 변경 없음
```

---

## 3. 각 스킬 상세 설계

### 3.1 `/jpa`

**파일**: `commands/jpa/SKILL.md`

```markdown
Apply the project's JPA conventions to: $ARGUMENTS

Read `reference/JPA_CONVENTIONS.md` and `policy/checklist.md` before implementing.

[기존 jpa.md 내용 그대로]
```

**`reference/JPA_CONVENTIONS.md`** — Entity/Repository 규칙 핵심 요약 (docs/conventions에서 발췌)

**`policy/checklist.md`** — 기존 jpa.md의 "Checklist before finishing" 섹션 분리

---

### 3.2 `/sdd` 체인

**`commands/sdd/SKILL.md`** — 체인 개요:
```
SDD 스킬체인 개요. 세부 명령:
  /sdd:requirements  — 요구사항 → SDD 문서
  /sdd:read          — SDD → 개발자 브리프
  /sdd:skeleton      — SDD → 코드 스켈레톤
  /sdd:tests         — SDD + 스켈레톤 → TDD 테스트
  /sdd:review        — SDD + 스켈레톤 + 테스트 리뷰
```

**`commands/sdd/requirements.md`** — 기존 `sdd-requirements.md` 내용 (경로 업데이트)

**`commands/sdd/read.md`** — 기존 `sdd-read.md` 내용 (경로 업데이트)

**`commands/sdd/skeleton.md`** — 기존 `spec-to-skeleton.md` 내용 (경로 업데이트)

**`commands/sdd/tests.md`** — 기존 `skeleton-to-tests.md` 내용 (경로 업데이트)

**`commands/sdd/review.md`** — 기존 `sdd-review.md` 내용 (경로 업데이트)

**`reference/SDD_TEMPLATE_GUIDE.md`** — 템플릿 위치, 섹션 명세 요약

**`policy/chain-rules.md`** — 체인 실행 순서, 각 단계 필수 입력/출력 명세

---

### 3.3 `/explain`

**파일**: `commands/explain/SKILL.md`
- 기존 `explain.md` 내용 그대로 이전
- reference/policy 불필요 (단순 유틸리티)

---

### 3.4 `/patch`

**파일**: `commands/patch/SKILL.md`
- 기존 `patch.md` 내용

**`policy/scope-rules.md`**:
```
- 요청된 태스크만 변경, 주변 코드 리팩터링 금지
- 파일 생성보다 기존 파일 편집 우선
- 도메인 모델/서비스 변경 시 대응 테스트 필수 추가
```

---

### 3.5 `/perf`

**파일**: `commands/perf/SKILL.md`
- 기존 `perf.md` 내용

**`reference/hotspot-checklist.md`**:
```
HIGH:  N+1 쿼리, 인덱스 누락
MEDIUM: Redis 캐시 미스, 커서 페이지네이션 미적용
LOW:  가상 스레드 ThreadLocal, Kafka 컨슈머 지연
```

---

### 3.6 `/docs`

**파일**: `commands/docs/SKILL.md`
- 기존 `docs.md` 내용 그대로

---

### 3.7 `/refactor`

**파일**: `commands/refactor/SKILL.md`
- 기존 `refactor.md` 내용

**`policy/refactor-rules.md`**:
```
- 관찰 가능한 동작 변경 금지
- 메서드 시그니처(외부 공개 API) 변경 금지
- 리팩터링 후 기존 테스트 전체 통과 필수
- 3개 이상 사용처가 없으면 추상화 도입 금지
```

---

### 3.8 `/security`

**파일**: `commands/security/SKILL.md`
- 기존 `security.md` 내용

**`reference/security-checklist.md`**:
```
CRITICAL: 인증 누락 엔드포인트, JWT 검증 우회
HIGH:     입력 검증 누락(@Valid), IDOR, 민감정보 노출
MEDIUM:   Mass Assignment, SQL injection (JPQL)
LOW:      Kafka/Redis 민감 데이터 TTL 없음
```

---

## 4. CLAUDE.md 업데이트 명세

### 변경 섹션: `## Harness (.claude/)`

현재:
```markdown
| `.claude/commands/` | 슬래시 커맨드 정의 |
```

변경 후:
```markdown
| `.claude/commands/{skill}/SKILL.md` | 슬래시 커맨드 (V2.0 — skill당 1 디렉토리) |
```

### 변경 섹션: `## Agent Workflow & Slash Commands`

현재:
```
**SDD skill chain**: `/sdd-requirements` → `/sdd-read` → `/spec-to-skeleton` → `/skeleton-to-tests` → `/sdd-review`
```

변경 후:
```
**SDD skill chain**: `/sdd:requirements` → `/sdd:read` → `/sdd:skeleton` → `/sdd:tests` → `/sdd:review`
```

현재 SDLC 라인 제거:
```
**SDLC phases**: `/plan` → `/develop` → `/review` (score >80 to pass) → `/qa`
```

변경 후 (bkit PDCA로 대체):
```
**PDCA phases**: `/pdca plan` → `/pdca design` → `/pdca do` → `/pdca analyze` → `/pdca report`
```

---

## 5. AGENT_COMMANDS.md 업데이트 명세

삭제 항목:
- `/plan`, `/develop`, `/review`, `/qa`, `/sdlc`

추가 항목:
- `/sdd` — SDD 체인 개요
- `/sdd:requirements`, `/sdd:read`, `/sdd:skeleton`, `/sdd:tests`, `/sdd:review`

bkit 참조 추가:
- `/pdca plan|design|do|analyze|iterate|report` (bkit 제공)
- `bkit:code-review`, `bkit:zero-script-qa` (bkit 제공)

---

## 6. 구현 순서 (Do Phase 체크리스트)

### Step 1: 삭제 (5분)
- [ ] `commands/plan.md` 삭제
- [ ] `commands/develop.md` 삭제
- [ ] `commands/qa.md` 삭제
- [ ] `commands/review.md` 삭제
- [ ] `commands/sdlc.md` 삭제
- [ ] `agents/planner.md` 삭제
- [ ] `agents/developer.md` 삭제
- [ ] `agents/qa.md` 삭제
- [ ] `agents/reviewer.md` 삭제

### Step 2: jpa 스킬 V2.0 생성
- [ ] `commands/jpa/SKILL.md` 생성 (기존 `jpa.md` 내용)
- [ ] `commands/jpa/reference/JPA_CONVENTIONS.md` 생성
- [ ] `commands/jpa/policy/checklist.md` 생성
- [ ] 기존 `commands/jpa.md` 삭제

### Step 3: sdd 스킬체인 V2.0 생성
- [ ] `commands/sdd/SKILL.md` 생성 (체인 개요)
- [ ] `commands/sdd/requirements.md` 생성 (기존 sdd-requirements.md)
- [ ] `commands/sdd/read.md` 생성 (기존 sdd-read.md)
- [ ] `commands/sdd/skeleton.md` 생성 (기존 spec-to-skeleton.md)
- [ ] `commands/sdd/tests.md` 생성 (기존 skeleton-to-tests.md)
- [ ] `commands/sdd/review.md` 생성 (기존 sdd-review.md)
- [ ] `commands/sdd/reference/SDD_TEMPLATE_GUIDE.md` 생성
- [ ] `commands/sdd/policy/chain-rules.md` 생성
- [ ] 기존 sdd-*.md, spec-to-skeleton.md, skeleton-to-tests.md 5개 삭제

### Step 4: 나머지 스킬 V2.0 생성 (explain, patch, perf, docs, refactor, security)
- [ ] 각 `commands/{name}/SKILL.md` 생성 (기존 내용 이전)
- [ ] policy/, reference/ 필요한 것만 생성 (3.3~3.8 설계 기준)
- [ ] 기존 flat 파일 삭제

### Step 5: CLAUDE.md 업데이트
- [ ] Harness 섹션 명세 반영
- [ ] Agent Workflow 섹션 반영

### Step 6: 검증
- [ ] `/jpa` 호출 → SKILL.md 로딩 확인
- [ ] `/sdd:requirements` 호출 → requirements.md 로딩 확인
- [ ] `/patch` 호출 → 정상 동작 확인
- [ ] `commands/jpa/reference/JPA_CONVENTIONS.md` → 커맨드로 등록 안 됨 확인

---

## 7. 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| CC가 `reference/*.md`를 커맨드로 인식 | 파일 확장자를 `.ref`로 변경, 또는 내용을 SKILL.md에 inline |
| `/sdd-*` 구 명령어 사용 중인 팀원 혼란 | CLAUDE.md에 마이그레이션 노트 명시 |
| agents/ 삭제 후 `/sdlc` 체인 파괴 | `sdlc.md` 이미 삭제 예정, bkit PDCA로 대체 안내 |

---

## 8. Done Criteria (Design 관점)

- [ ] 목표 파일 트리 100% 구현
- [ ] 9개 삭제 대상 모두 제거
- [ ] 8개 스킬 V2.0 구조 완성
- [ ] CLAUDE.md 업데이트 완료
- [ ] `/jpa` `/sdd:requirements` `/patch` 3개 커맨드 동작 검증
