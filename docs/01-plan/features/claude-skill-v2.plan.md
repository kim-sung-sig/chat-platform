# Plan: .claude Skills V2.0 전환

## Executive Summary

| 항목 | 내용 |
|------|------|
| Feature | `.claude` Skills V2.0 전환 |
| 날짜 | 2026-03-20 |
| 유형 | 개발 환경 / 워크플로우 개선 |

### 4-Perspective Value Table

| 관점 | 내용 |
|------|------|
| **Problem** | bkit 도입 후 기존 `.claude/commands/`의 스킬들과 bkit 제공 기능이 중복되어 관리 부담 증가 |
| **Solution** | 중복 스킬 제거 + 남은 스킬을 `SKILL.md / reference/ / policy/` 계층 구조(V2.0)로 재편 |
| **Function UX Effect** | `/` 명령어 목록이 깔끔해지고, 각 스킬의 역할·정책·참조문서가 한 디렉토리에 모여 탐색 용이 |
| **Core Value** | bkit과 프로젝트 로컬 스킬의 역할 분리 → 유지보수 부담 최소화, AI 협업 품질 향상 |

---

## 1. 현재 상태 분석

### 1.1 현재 `.claude` 구조

```
.claude/
  agents/
    developer.md
    planner.md
    qa.md
    reviewer.md
  commands/           ← 현재 스킬 위치 (flat 구조)
    develop.md
    docs.md
    explain.md
    jpa.md
    patch.md
    perf.md
    plan.md
    qa.md
    refactor.md
    review.md
    sdd-read.md
    sdd-requirements.md
    sdd-review.md
    sdlc.md
    security.md
    skeleton-to-tests.md
    spec-to-skeleton.md
  hooks/
    bash-guard.sh
    file-guard.sh
    notify.sh
    on-compact.sh
  settings.json
  settings.local.json
```

### 1.2 bkit이 제공하는 기능 (중복 대상)

| bkit 기능 | 대응하는 현재 commands |
|-----------|----------------------|
| `/pdca plan` + `planner` agent | `plan.md`, `planner` agent |
| `/pdca do` + `developer` agent | `develop.md`, `developer` agent |
| `/pdca analyze` + `gap-detector` | (분석 기능) |
| `bkit:code-review` + `reviewer` agent | `review.md`, `reviewer` agent |
| `bkit:zero-script-qa` + `qa` agent | `qa.md`, `qa` agent |
| Full PDCA workflow (`/pdca` + `sdlc`) | `sdlc.md` |
| `bkit:security-architect` | `security.md` (부분 중복) |

---

## 2. 목표 (Goals)

1. **중복 제거**: bkit이 동등하거나 우수하게 커버하는 commands/agents를 삭제
2. **V2.0 구조 전환**: 남은 프로젝트 로컬 스킬을 아래 구조로 재편
   ```
   .claude/
     skills/
       {skillName}/
         SKILL.md      ← 스킬 본문 (기존 command 내용)
         reference/    ← 참조 문서 (conventions, examples)
         policy/       ← 제약·규칙 (do's and don'ts)
   ```
3. **bkit과의 역할 분리 명확화**: 프로젝트 고유 도메인 지식만 로컬 스킬로 유지

## 3. Non-Goals

- bkit 플러그인 자체 수정 (bkit은 그대로 사용)
- `agents/` 디렉토리 구조 변경 (CC 에이전트 로딩 방식 유지)
- `hooks/` 변경
- `settings.json` 대규모 수정

## 4. 스킬 분류 결정

### 4.1 삭제 대상 (bkit 중복)

| 파일 | 이유 | 대체 bkit 명령 |
|------|------|--------------|
| `commands/plan.md` | `/pdca plan` 과 동일 | `/pdca plan {feature}` |
| `commands/develop.md` | `/pdca do` + developer agent | `/pdca do {feature}` |
| `commands/qa.md` | bkit qa agent + zero-script-qa | `/pdca analyze` |
| `commands/review.md` | bkit reviewer + code-review | `bkit:code-review` |
| `commands/sdlc.md` | 전체 PDCA 워크플로우 | `/pdca` + `bkit:pdca` |

**Agents 삭제 대상:**

| 파일 | 이유 | 대체 |
|------|------|------|
| `agents/planner.md` | bkit planner agent | bkit `planner` agent |
| `agents/developer.md` | bkit developer agent | bkit `developer` agent |
| `agents/qa.md` | bkit qa agent | bkit `qa` agent |
| `agents/reviewer.md` | bkit reviewer agent | bkit `reviewer` agent |

> **참고**: agents/ 파일은 bkit 에이전트와 이름이 같으면 프로젝트 로컬이 우선 로딩될 수 있음.
> bkit 에이전트 정의를 덮어쓰지 않도록 삭제 필요.

### 4.2 유지 및 V2.0 마이그레이션 대상

| 현재 파일 | V2.0 스킬명 | 특이사항 |
|-----------|------------|---------|
| `commands/jpa.md` | `skills/jpa/` | 프로젝트 JPA 컨벤션 (고유) |
| `commands/sdd-requirements.md` `commands/sdd-read.md` `commands/sdd-review.md` `commands/skeleton-to-tests.md` `commands/spec-to-skeleton.md` | `skills/sdd/` | SDD 스킬체인 통합 → 서브커맨드로 |
| `commands/explain.md` | `skills/explain/` | 일반 유틸리티 |
| `commands/patch.md` | `skills/patch/` | 최소 변경 구현 |
| `commands/perf.md` | `skills/perf/` | 성능 분석 |
| `commands/docs.md` | `skills/docs/` | 문서화 |
| `commands/refactor.md` | `skills/refactor/` | 리팩터링 |
| `commands/security.md` | 선택적 유지 | bkit security-architect와 부분 중복 — 검토 필요 |

### 4.3 `security.md` 처리 판단

bkit `security-architect` 에이전트는 아키텍처 수준 보안 분석에 초점.
현재 `security.md`는 코드 범위 즉시 리뷰에 초점 → **유지** (`skills/security/`).

---

## 5. 목표 구조

```
.claude/
  skills/                          ← 신규 (V2.0 스킬 홈)
    jpa/
      SKILL.md                     ← JPA 스킬 본문
      reference/
        JPA_CONVENTIONS.md         ← docs/conventions에서 발췌
      policy/
        entity-rules.md            ← BaseEntity, 네이밍 규칙
    sdd/
      SKILL.md                     ← SDD 스킬체인 통합
      reference/
        SDD_TEMPLATE.md
      policy/
        sdd-rules.md
    explain/
      SKILL.md
    patch/
      SKILL.md
      policy/
        patch-scope.md             ← 변경 범위 제약
    perf/
      SKILL.md
      reference/
        perf-checklist.md
    docs/
      SKILL.md
    refactor/
      SKILL.md
      policy/
        refactor-rules.md
    security/
      SKILL.md
      reference/
        owasp-checklist.md
  commands/                        ← 기존 (삭제 후 빈 디렉토리 또는 제거)
  agents/                          ← 유지 (bkit agents와 충돌 파일 삭제)
  hooks/                           ← 변경 없음
  settings.json
  settings.local.json
```

### CC에서 `.claude/skills/` 인식 방법

Claude Code는 현재 `.claude/commands/`만 슬래시 커맨드로 자동 인식.
`.claude/skills/`는 두 가지 방식으로 활용:

**Option A**: `commands/`에 thin wrapper 파일 유지
```
.claude/commands/jpa.md  → "@skills/jpa/SKILL.md" 참조
```

**Option B**: `skills/` → `commands/` 심볼릭 링크 또는 실제 이동
```
.claude/commands/jpa/ → .claude/skills/jpa/ (실제 파일 위치)
```

**Option C (권장)**: `commands/{skillName}/SKILL.md` 구조 사용
CC는 `commands/` 내 서브디렉토리를 지원: `/jpa` = `commands/jpa/SKILL.md`

```
.claude/
  commands/
    jpa/
      SKILL.md          ← /jpa 로 호출
      reference/        ← 슬래시 커맨드 아님, 참조 문서
      policy/           ← 슬래시 커맨드 아님, 정책 문서
```

> **결정**: Option C 채택. `commands/{skillName}/SKILL.md` 구조가 CC 네이티브 지원이면서
> 사용자가 원하는 `SKILL.md / reference/ / policy/` 계층을 그대로 구현 가능.

---

## 6. 구현 순서

### Phase 1: 삭제 (bkit 중복 제거)
1. `commands/` 중복 파일 삭제 (plan, develop, qa, review, sdlc)
2. `agents/` 중복 파일 삭제 (planner, developer, qa, reviewer)

### Phase 2: V2.0 구조 생성
1. `commands/jpa/SKILL.md` 생성 (기존 `jpa.md` 내용 이전)
2. `commands/sdd/SKILL.md` 생성 (sdd-* 5개 통합, 서브커맨드 방식)
3. 나머지 스킬 각각 `commands/{name}/SKILL.md`로 이전
4. reference/ policy/ 디렉토리에 관련 문서 구성

### Phase 3: 검증
1. 각 `/skillName` 커맨드 동작 확인
2. `CLAUDE.md` Harness 섹션 업데이트
3. `AGENT_COMMANDS.md` 명령 목록 업데이트

---

## 7. 제약 사항 (Constraints)

- `hooks/` 파일은 변경하지 않음
- `settings.json` 구조는 유지 (허용 권한, 훅 설정 등)
- bkit 플러그인 캐시 폴더 불변
- SDD 스킬 통합 시 기존 `/sdd-requirements`, `/sdd-read` 등 구 명령어는 제거됨 — CLAUDE.md 업데이트 필요

---

## 8. 리스크

| 리스크 | 가능성 | 대응 |
|--------|--------|------|
| CC가 `commands/` 서브디렉토리 인식 안 할 수 있음 | 중 | 먼저 `/jpa` 하나로 테스트 후 전체 마이그레이션 |
| agents/ 삭제 후 bkit agent 동작이 기대와 다를 수 있음 | 중 | 하나씩 삭제하며 검증 |
| SDD 통합 시 기존 docs/specs 워크플로우 영향 | 낮 | SDD 서브커맨드 방식으로 하위호환 유지 |

---

## 9. Open Questions

1. **CC 서브디렉토리 지원 확인**: `commands/jpa/SKILL.md`가 `/jpa`로 실제 로딩되는지 먼저 검증 필요
2. **SDD 통합 방식**: 5개 SDD 명령을 하나의 `skills/sdd/SKILL.md`에 서브커맨드(`/sdd requirements`, `/sdd read` 등)로 통합할지, 별도 파일로 유지할지
3. **security.md 완전 제거 여부**: bkit `security-architect`로 대체 가능한지 실사용 후 판단

---

## 10. Done Criteria

- [ ] `commands/`에서 bkit 중복 파일 5개 삭제
- [ ] `agents/`에서 bkit 중복 파일 4개 삭제
- [ ] 잔존 스킬 8개 → `commands/{name}/SKILL.md` 구조로 이전
- [ ] `CLAUDE.md` Harness 섹션 반영
- [ ] `AGENT_COMMANDS.md` 명령 목록 업데이트
- [ ] `/jpa`, `/sdd`, `/patch` 등 주요 스킬 동작 검증
