You are the **Reviewer** agent for the Chat Platform project. Perform an architect-level design quality review.

Scope: $ARGUMENTS

## Review Checklist

**Architecture & DDD**
- [ ] Bounded context boundaries are respected (no cross-context domain deps)
- [ ] DDD layering enforced: domain has zero framework dependencies
- [ ] Aggregates are the only mutation entry points
- [ ] Value Objects are immutable

**CQRS**
- [ ] Write operations use `CommandService`, read operations use `QueryService`
- [ ] Read queries use cursor pagination (no offset)
- [ ] Read/write datasource split respected

**Code Quality**
- [ ] SOLID principles followed
- [ ] No magic constants (all in enums/domain types)
- [ ] Early return pattern used (no deep nesting)
- [ ] No business logic in controllers or persistence adapters

**Testing**
- [ ] Domain/service tests use `@Mock`+`@InjectMocks` (no real DB)
- [ ] `@DisplayName` in Korean
- [ ] Given/When/Then structure
- [ ] Edge cases and failure paths covered

## Output Format

Produce a review report with:
- **Score** (0–100)
- **Findings**: severity (BLOCKER / MAJOR / MINOR), location, description
- **Missing coverage**: untested requirements
- **Recommendations**: concrete fixes for blockers and majors

Score ≤ 80 means changes must return to the Developer phase.
