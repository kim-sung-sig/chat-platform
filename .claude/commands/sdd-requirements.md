Convert raw requirements into a structured SDD document.

Input: $ARGUMENTS

## Instructions

1. Read `docs/specs/SDD_TEMPLATE.md` and follow its headings exactly.
2. Derive a short kebab-case `<slug>` from the feature name; output to `docs/specs/SDD_<slug>.md`.
3. If a planning document exists at `docs/planning/<slug>_plan.md`, read it and map its sections into the SDD.
4. Keep every requirement explicit and testable — no vague statements.
5. If information is missing, mark the field `TBD:` with a note on what is needed.
6. Keep domain language consistent with existing bounded contexts and `docs/conventions/CONVENTIONS.md`.
7. Populate the `Open Questions` section with any blocking ambiguities.
8. Add `Related Docs` links to planning, skeleton, and test plan documents when known.
9. Output must be UTF-8 Markdown.

## Skill Connection

This SDD is the input for:
- `/sdd-read` — developer handoff brief
- `/spec-to-skeleton` — code skeleton generation
- `/skeleton-to-tests` — TDD test generation
- `/sdd-review` — compliance review
