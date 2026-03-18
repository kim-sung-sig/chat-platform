# AGENTS.md

Multi-agent SDLC workflow for the Chat Platform.

> Development principles and coding conventions live in [`docs/conventions/CONVENTIONS.md`](docs/conventions/CONVENTIONS.md).
> Slash commands for each phase live in [`.claude/commands/`](.claude/commands/).

---

## Agentic Feedback Loop

`/sdlc "<task>"` 커맨드로 전체 루프를 자동 실행합니다.

```
/sdlc "task"
    │
    ▼
[planner] → docs/planning/<slug>_plan.md 작성
    │
    ▼
[developer] → 구현 (iteration 1~3)
    │
    ▼
[reviewer] → REVIEW_SCORE: 0-100 출력
    │
    ├── score ≤ 80 → findings와 함께 [developer] 재투입 (최대 3회)
    │
    └── score > 80 → [qa]
                        │
                        ├── FAIL (CRITICAL/MAJOR) → [developer] 재투입
                        └── PASS → 완료 리포트
```

최대 반복 횟수(3회) 초과 시 루프를 중단하고 사용자에게 판단을 요청합니다.

---

## Sub-Agent Roles

| Role | Responsibility | Invoke |
|------|---------------|--------|
| **Planner** (기획자) | Turn ambiguous input into explicit goals, non-goals, constraints, risks, open questions. Output to `docs/planning/` using `PLANNING_TEMPLATE.md`. | `/plan` |
| **Developer** (개발자) | Implement with DDD/TDD discipline. Critically analyze planner output before coding. | `/develop` |
| **Reviewer** (리뷰어) | Architect-level design quality review, scored 0–100. | `/review` |
| **QA** (테스터) | Functional test simulation; bugs become task tickets. | `/qa` |

---

## Skill Chain (SDD → Code)

```
/sdd-requirements  →  /spec-to-skeleton  →  /skeleton-to-tests  →  /sdd-review
```

Use the same `<slug>` across all documents to keep them linked.

---

## Context Management

Maintain `summary.md` or `README.md` per bounded context as the primary knowledge base for that context.
