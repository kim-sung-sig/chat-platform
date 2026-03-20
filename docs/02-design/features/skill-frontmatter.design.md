# Design: SKILL.md 프론트매터 추가

**설계일**: 2026-03-20
**Plan 문서**: [skill-frontmatter.plan.md](../../../docs/01-plan/features/skill-frontmatter.plan.md)

---

## 1. 프론트매터 스펙

```yaml
---
name: {skill-name}        # CC 스킬 식별자
description: "{summary}.  # 첫 줄: 1줄 요약
  '{KR 키워드}', '{EN keyword}' 등의 요청에 반응합니다."
---
```

### 1.1 각 스킬별 프론트매터 정의

| 파일 | name | description 키워드 |
|------|------|-------------------|
| `jpa/SKILL.md` | `jpa` | 엔티티, 레포지토리, JPA, BaseEntity, entity, repository |
| `explain/SKILL.md` | `explain` | 설명해줘, 이해, 코드 분석, explain, how does, what is |
| `patch/SKILL.md` | `patch` | 수정, 구현, 버그, 추가, patch, fix, implement |
| `perf/SKILL.md` | `perf` | 성능, N+1, 느린, 최적화, performance, slow, optimize |
| `refactor/SKILL.md` | `refactor` | 리팩토링, 정리, 개선, refactor, clean up, restructure |
| `security/SKILL.md` | `security` | 보안, 취약점, 권한, security, vulnerability, auth check |
| `docs/SKILL.md` | `docs` | 문서, 문서화, README, docs, document, API 문서 |
| `sdd/SKILL.md` | `sdd` | SDD, 설계 문서, 스펙, software design document, spec |
| `sdd/requirements.md` | `sdd:requirements` | 요구사항, 기능 정의, requirements, what to build |
| `sdd/read.md` | `sdd:read` | SDD 읽기, 스펙 파악, read spec, understand design |
| `sdd/skeleton.md` | `sdd:skeleton` | 스켈레톤, 뼈대 코드, skeleton, scaffold, generate code |
| `sdd/tests.md` | `sdd:tests` | 테스트 작성, TDD, 단위 테스트, write tests, test cases |
| `sdd/review.md` | `sdd:review` | SDD 검토, 구현 검증, spec review, verify implementation |

## 2. 구현 규칙

- 프론트매터는 파일 **첫 번째 줄**에 위치
- 기존 instruction 내용은 **한 글자도 변경하지 않음**
- `name`에 콜론(`:`)이 있는 경우 (e.g., `sdd:requirements`) 따옴표로 감쌈

## 3. 완료 기준 (Done Criteria)

- [ ] 8개 SKILL.md 프론트매터 추가 완료
- [ ] 5개 sdd 서브커맨드 프론트매터 추가 완료
- [ ] 각 description에 KR 키워드 3개 이상 포함
- [ ] 기존 내용 무결성 확인 (내용 변경 없음)
