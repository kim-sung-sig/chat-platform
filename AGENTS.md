# AGENTS.md

Harness-centric multi-agent governance for the Chat Platform.

Core coding and architecture conventions remain in [`docs/conventions/CONVENTIONS.md`](docs/conventions/CONVENTIONS.md).

---

## 1) Governance Model

- Single source of truth for agent governance is `/.harness`.
- Tool-specific directories (`.claude`, `.codex`) are adapter outputs generated from `/.harness`.
- Canonical command/skill IDs use kebab-case (for example `sdd-requirements`).
- Tool aliases are allowed only at adapter level, not in harness registries.

---

## 2) Fixed vs Mutable Boundaries

### Fixed (Harness-controlled)
- Policy files under `/.harness/registries/`
- Done gate rules under `/.harness/registries/done-gate.json`
- Generated adapter outputs:
  - `.claude/commands/<managed-command>/SKILL.md`
  - `.codex/skills/<managed-skill>/SKILL.md`

### Mutable (Team-owned extensions)
- Any skill not registered in `/.harness/registries/skills.json` is unmanaged.
- Unmanaged skills may be added/updated/removed freely.
- Optional managed skills may be edited at their harness source path when `mutable=true`.

---

## 3) Registries (Public Interfaces)

### Skill Registry
Path: `/.harness/registries/skills.json`

Skill entry fields:
- `id`: canonical skill ID (kebab-case)
- `source`: harness source markdown path
- `tier`: `core | optional`
- `mutable`: whether source is editable by team
- `tool_aliases`: aliases per tool runtime
- `adapters`: target output directory names for each tool

### Agent Registry
Path: `/.harness/registries/agents.json`

Defines:
- Core roles: `planner`, `developer`, `reviewer`, `qa`
- Specialist roles: add/remove rules
- Handoff contract: required artifacts and ownership transfer

### Done-Gate Registry
Path: `/.harness/registries/done-gate.json`

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

1. Edit harness sources/registries.
2. Generate adapters:
   - `pwsh -File scripts/sync-skills.ps1`
3. Validate drift only:
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
