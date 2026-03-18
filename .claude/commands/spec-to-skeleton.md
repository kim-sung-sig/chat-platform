Generate a compilable code skeleton from an SDD, aligned to DDD and project conventions.

Input: $ARGUMENTS
(Provide the SDD path, e.g. `docs/specs/SDD_approval-system.md`.)

## Instructions

1. Read `docs/conventions/CONVENTIONS.md` for layering rules and conventions.
2. Read `docs/specs/SDD_TEMPLATE.md` to understand the SDD structure.
3. Decompose by bounded context and aggregates; map to packages under `apps/chat/chat-server/src/main/java/com/example/chat/<context>/`.
4. Create minimal, compilable stubs for every SDD section that implies code:
   - REST controller interfaces and DTOs
   - Domain model: aggregates, value objects, status enums
   - Application services (command/query)
   - Repository port interfaces
   - Error codes / exception classes if specified
5. **Do not implement business logic** — keep methods as stubs (`throw new UnsupportedOperationException()` or return empty).
6. No magic strings or numbers — introduce constants or enums where necessary.
7. Output optional skeleton notes at `docs/design/skeletons/<slug>/README.md`.
8. List any open questions or missing spec details at the end.

## Skill Connection

Output skeleton → input for `/skeleton-to-tests` and `/sdd-review`.
