# AGENTS.md

Harness-centric multi-agent governance for the Chat Platform.

Core coding and architecture conventions remain in [`docs/conventions/CONVENTIONS.md`](docs/conventions/CONVENTIONS.md).

---

## 1) Governance Model

- Single source of truth for agent governance is `.claude/.harness/`.
- Skills live directly in `.claude/skills/<id>/SKILL.md` — no separate adapter generation needed.
- Canonical command/skill IDs use kebab-case (for example `sdd-requirements`).
- Tool aliases are defined in `.claude/.harness/registries/skills.json`.

---

## 2) Fixed vs Mutable Boundaries

### Fixed (Harness-controlled)
- Policy files under `.claude/.harness/registries/`
- Done gate rules under `.claude/.harness/registries/done-gate.json`
- Skills with `"mutable": false` in the registry must not be manually edited.

### Mutable (Team-owned extensions)
- Any skill not registered in `.claude/.harness/registries/skills.json` is unmanaged.
- Unmanaged skills may be added/updated/removed freely.
- Optional managed skills (`"mutable": true`) may be edited directly in `.claude/skills/<id>/SKILL.md`.

---

## 3) Registries (Public Interfaces)

### Skill Registry
Path: `.claude/.harness/registries/skills.json`

Skill entry fields:
- `id`: canonical skill ID (kebab-case)
- `source`: skill SKILL.md path under `.claude/skills/`
- `tier`: `core | optional`
- `mutable`: whether source is editable by team
- `tool_aliases`: aliases per tool runtime
- `adapters`: target directory name for the skill

### Agent Registry
Path: `.claude/.harness/registries/agents.json`

Defines:
- Core roles: `planner`, `developer`, `reviewer`, `qa`
- Specialist roles: add/remove rules
- Handoff contract: required artifacts and ownership transfer

### Done-Gate Registry
Path: `.claude/.harness/registries/done-gate.json`

Defines:
- Mandatory checks and severity (`block | warn`)
- Failure thresholds (for example review score minimum)
- Protected path policy

---

## 4) Multi-Agent Team Operation

Core role chain:

```
planner -> developer -> reviewer -> qa
```

Specialists can join per task (security, perf, migration, infra, docs), but must:
- declare owner role,
- define input/output contract,
- return result back to core chain before `Done`.

Handoff minimum artifact set:
- Task intent and constraints
- Changed scope (paths/modules)
- Verification evidence (tests/check results)
- Open risks and fallback plan

---

## 5) Sync + Done Workflow

1. Edit skills in `.claude/skills/<id>/SKILL.md` directly.
2. Update registry if adding a new skill:
   - `.claude/.harness/registries/skills.json`
3. Validate registry drift:
   - `pwsh -File scripts/sync-skills.ps1 -Check`
4. Validate Done gate:
   - `pwsh -File scripts/done-gate.ps1`

If Done gate fails, do not declare completion until blockers are resolved or explicitly overridden by policy.

---

## 6) SDD Core Chain (Canonical IDs)

```
sdd-requirements -> sdd-read -> spec-to-skeleton -> skeleton-to-tests -> sdd-review
```

Legacy command names and tool-specific aliases are supported through adapter mappings in the skill registry.
