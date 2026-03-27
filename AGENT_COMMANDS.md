# AGENT_COMMANDS.md

Canonical command catalog for harness-managed skills.

Source of truth:
- `.claude/.harness/registries/skills.json`

Skills directory:
- `.claude/skills/<id>/SKILL.md`

---

## Core SDD Chain

| Canonical ID | Purpose | Legacy / Alias Examples |
|---|---|---|
| `sdd-requirements` | Raw requirements -> SDD document | `/sdd:requirements`, `/sdd-requirements` |
| `sdd-read` | SDD -> developer brief | `/sdd:read`, `/sdd-read` |
| `spec-to-skeleton` | SDD -> compilable skeleton | `/sdd:skeleton`, `/spec-to-skeleton` |
| `skeleton-to-tests` | SDD + skeleton -> TDD tests | `/sdd:tests`, `/skeleton-to-tests` |
| `sdd-review` | SDD/skeleton/tests compliance review | `/sdd:review`, `/sdd-review` |

## Core Workflow Roles

| Role | Responsibility |
|---|---|
| `planner` | Clarify goals/non-goals/constraints/risks |
| `developer` | Implement with DDD/TDD discipline |
| `reviewer` | Design/code quality review with score |
| `qa` | Functional validation and ticketization |

## Optional Utility Skills

`docs`, `explain`, `jpa`, `patch`, `perf`, `refactor`, `security`, `spring-boot`, `done-gate`

---

## Validation Commands

```bash
pwsh -File scripts/sync-skills.ps1
pwsh -File scripts/sync-skills.ps1 -Check
pwsh -File scripts/done-gate.ps1
```
