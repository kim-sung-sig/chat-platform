# Plan: SKILL.md 프론트매터 추가 — 커맨드에서 스킬로

## Executive Summary

| 항목 | 내용 |
|------|------|
| Feature | `skill-frontmatter` |
| 날짜 | 2026-03-20 |
| 유형 | 개발 환경 / 워크플로우 개선 |

### 4-Perspective Value Table

| 관점 | 내용 |
|------|------|
| **Problem** | 현재 SKILL.md 파일들은 `---name/description---` 프론트매터가 없어 명시적 `/command` 호출만 가능. 키워드 기반 암묵적 매칭 불가 |
| **Solution** | 13개 SKILL.md 파일 전체에 `name`+`description`(트리거 키워드 포함) 프론트매터 추가 |
| **Function UX Effect** | 팀원이 "JPA 엔티티 만들어줘"처럼 자연어로 요청해도 Claude가 자동으로 `/jpa` 스킬을 적용 |
| **Core Value** | 명령어 암기 의존 제거 → 자연어로 협업, 스킬이 수동 도구에서 지능형 어시스턴트로 전환 |

---

## 1. 현재 상태 (As-Is)

```
.claude/commands/
  docs/SKILL.md         ← 프론트매터 없음
  explain/SKILL.md      ← 프론트매터 없음
  jpa/SKILL.md          ← 프론트매터 없음
  patch/SKILL.md        ← 프론트매터 없음
  perf/SKILL.md         ← 프론트매터 없음
  refactor/SKILL.md     ← 프론트매터 없음
  security/SKILL.md     ← 프론트매터 없음
  sdd/SKILL.md          ← 프론트매터 없음
  sdd/requirements.md   ← 프론트매터 없음
  sdd/read.md           ← 프론트매터 없음
  sdd/skeleton.md       ← 프론트매터 없음
  sdd/tests.md          ← 프론트매터 없음
  sdd/review.md         ← 프론트매터 없음
```

**문제점**:
- 팀원이 `/jpa` 명령어를 직접 입력해야만 스킬 실행 가능
- `description`이 없어 암묵적 트리거 키워드 정의 불가
- 스킬이 "지능형 매칭"이 아닌 "단순 명령어 단축키"로만 동작

---

## 2. 목표 상태 (To-Be)

### SKILL.md 프론트매터 형식

```markdown
---
name: jpa
description: "JPA Entity와 Repository를 프로젝트 컨벤션에 맞게 작성합니다.
  '엔티티 만들어줘', 'JPA 컨벤션', 'Entity 작성', 'Repository 구현',
  'jpa entity', 'jpa repository', '도메인 엔티티' 등의 키워드에 반응합니다."
---
```

### 암묵적 트리거 동작

| 사용자 입력 | 매칭 스킬 |
|-------------|-----------|
| "채널 Entity 만들어줘" | `/jpa` |
| "이 코드 설명해줘" | `/explain` |
| "N+1 쿼리 있는지 봐줘" | `/perf` |
| "보안 취약점 검토해줘" | `/security` |
| "이 클래스 리팩토링 해줘" | `/refactor` |
| "SDD 요구사항 분석해줘" | `/sdd:requirements` |

---

## 3. 구현 범위

### 3.1 대상 파일 (13개)

| 파일 | 스킬명 | 주요 트리거 키워드 |
|------|--------|-------------------|
| `jpa/SKILL.md` | `jpa` | Entity, Repository, JPA, 엔티티, 레포지토리 |
| `explain/SKILL.md` | `explain` | 설명, 분석, explain, 코드 설명, 이해 |
| `patch/SKILL.md` | `patch` | 수정, 구현, 버그, patch, fix, 변경 |
| `perf/SKILL.md` | `perf` | 성능, N+1, 캐시, 인덱스, performance, 최적화 |
| `refactor/SKILL.md` | `refactor` | 리팩토링, 개선, 정리, refactor, 구조 개선 |
| `security/SKILL.md` | `security` | 보안, 취약점, security, IDOR, XSS, 인증 |
| `docs/SKILL.md` | `docs` | 문서, 문서화, docs, README, SDD 업데이트 |
| `sdd/SKILL.md` | `sdd` | SDD, 설계 문서, 스펙, spec, design document |
| `sdd/requirements.md` | `sdd:requirements` | 요구사항 분석, requirements, 기능 정의 |
| `sdd/read.md` | `sdd:read` | SDD 읽기, spec 파악, 설계 이해 |
| `sdd/skeleton.md` | `sdd:skeleton` | 스켈레톤, 뼈대 코드, skeleton, 코드 구조 생성 |
| `sdd/tests.md` | `sdd:tests` | 테스트 작성, TDD, test, 테스트 코드 |
| `sdd/review.md` | `sdd:review` | SDD 검토, 설계 리뷰, spec review |

### 3.2 프론트매터 작성 기준

1. `name`: 디렉토리/파일명과 일치 (소문자, 하이픈)
2. `description` 필수 포함 요소:
   - 1줄 요약 (스킬이 하는 일)
   - 한국어 트리거 키워드 (팀원이 자연어로 쓸 표현)
   - 영어 트리거 키워드 (영문 입력 케이스)
3. 기존 instruction 내용은 **변경하지 않음** — 프론트매터만 추가

---

## 4. 구현 전략

**전략**: 각 SKILL.md 파일 선두에 frontmatter 블록만 삽입. 기존 내용 무수정.

```markdown
---
name: {skill-name}
description: "{1줄 요약}.
  '{KR 키워드1}', '{KR 키워드2}', '{EN keyword1}', '{EN keyword2}' 등에 반응합니다."
---

{기존 내용 그대로}
```

**주의**: `sdd/requirements.md` 등 서브커맨드는 name을 `sdd:requirements` 형식으로 지정

---

## 5. 완료 기준 (Done Criteria)

- [ ] 8개 SKILL.md 파일 전체에 프론트매터 추가
- [ ] 5개 sdd 서브커맨드 파일에 프론트매터 추가
- [ ] 각 `description`에 한국어 + 영어 키워드 최소 3개 이상 포함
- [ ] 기존 instruction 내용 무변경 확인
- [ ] 프론트매터 추가 후 CC에서 스킬 목록 정상 표시 확인

---

## 6. 제외 범위

- `sdd/SKILL.md`의 instruction 내용 변경 — 스코프 아님
- 새 스킬 추가 — 스코프 아님
- hooks/settings 변경 — 스코프 아님
