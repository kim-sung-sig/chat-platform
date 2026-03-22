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
