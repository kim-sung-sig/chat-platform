# CLAUDE.md

This file is an index for Claude in this repository.

Primary governance and workflow:
- [`AGENTS.md`](AGENTS.md)
- [`docs/conventions/CONVENTIONS.md`](docs/conventions/CONVENTIONS.md)
- [`/.harness/README.md`](.harness/README.md)

---

## Claude Adapter Rules

- `.claude/commands/*/SKILL.md` for managed skills are generated artifacts.
- Do not manually edit generated command skill files.
- Update harness sources first, then run:
  - `pwsh -File scripts/sync-skills.ps1`

---

## Command Index Source

Canonical command IDs and aliases are defined in:
- `/.harness/registries/skills.json`
- [`AGENT_COMMANDS.md`](AGENT_COMMANDS.md)

---

## Context Reminder

- Use DDD layering and CQRS rules from `CONVENTIONS.md`.
- Keep changes minimal and scoped.
- Treat Done as valid only after Done gate passes:
  - `pwsh -File scripts/done-gate.ps1`

---

## bkit PDCA Policy

### plan 단계
- 기능 요구사항을 반드시 **Spec Checklist** 형태로 명세할 것:
  ```
  ## Spec Checklist
  - [ ] API 엔드포인트 (HTTP method, path, request/response body)
  - [ ] Domain Entity / Value Object 변경사항
  - [ ] Command / Query 분리 (CQRS)
  - [ ] 이벤트 발행 여부 (도메인 이벤트)
  - [ ] 예외 및 에러 케이스
  - [ ] 테스트 시나리오 (단위 / 통합)
  ```
- Spec Checklist 항목은 do 단계의 완료 기준으로 사용됨
- Plan 문서 없이 바로 구현 금지

### design 단계
- `/sdd-requirements` 스킬로 SDD 문서(`docs/specs/SDD_<slug>.md`) 작성 필수
- SDD 문서 없이 코드 뼈대 및 구현 진행 금지
- DDD 레이어(domain / application / event / infrastructure / api) 구조를 SDD에 명시

### do 단계
- 반드시 다음 순서를 준수:
  1. `/sdd-requirements` → SDD 문서 작성
  2. `/spec-to-skeleton` → DDD 뼈대(패키지, 인터페이스, 스텁) 생성
  3. `/skeleton-to-tests` → TDD 테스트 작성 (테스트 먼저)
  4. 실제 구현 (테스트 Green 목표)
- Plan의 Spec Checklist 항목을 하나씩 체크하며 진행

### analyze 단계 (Check)
- **선행 필수**: `./gradlew compileJava compileTestJava --no-daemon` 실행
  - 컴파일 오류 존재 시 → 오류 목록 출력 후 Gap analysis 중단, 수정 요청
  - 컴파일 통과 시 → Gap analysis 진행
- Gap analysis 시 Plan의 Spec Checklist 미체크 항목도 함께 검증

---

## SDD/TDD + PDCA 파이프라인

| PDCA 단계 | 사용 스킬 | 완료 조건 |
|-----------|-----------|-----------|
| plan      | `/pdca plan` | Spec Checklist 포함된 Plan 문서 생성 |
| design    | `/sdd-requirements` → `/pdca design` | SDD 문서 존재 (`docs/specs/SDD_*.md`) |
| do        | `/spec-to-skeleton` → `/skeleton-to-tests` → 구현 | 테스트 Green + 컴파일 통과 |
| analyze   | `./gradlew compileJava` 선행 → `/pdca analyze` | Gap rate ≥ 90% |
| report    | `/pdca report` | 완료 보고서 생성 |

**참조 스킬 경로**: `.harness/skills/` (sdd-requirements, spec-to-skeleton, skeleton-to-tests)
