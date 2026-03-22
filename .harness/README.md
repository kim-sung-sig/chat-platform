# Harness

Tool-neutral governance source for agent workflow, policy, and managed skills.

## Directory Layout

- `registries/skills.json`: skill registry and adapter mapping
- `registries/agents.json`: role model and handoff contract
- `registries/done-gate.json`: completion validation policy
- `skills/<id>/SKILL.md`: canonical skill sources
- `state/generated-files.json`: generated adapter manifest

## Operational Rules

- Edit skills/policies in `/.harness` first.
- Generate adapters with `scripts/sync-skills.ps1`.
- Never manually edit generated adapter files in `.claude` or `.codex`.
- Use `scripts/done-gate.ps1` before marking a task as Done.

## Canonical IDs

- Use kebab-case IDs (for example `sdd-requirements`).
- Keep aliases only in registry (`tool_aliases`), not in source filenames.
