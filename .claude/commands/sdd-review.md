Review the SDD, skeleton, and tests for convention compliance and spec alignment.

Input: $ARGUMENTS
(Provide: SDD path, skeleton path, test path — e.g. `docs/specs/SDD_approval-system.md apps/chat/chat-server/src/main/java/com/example/chat/approval apps/chat/chat-server/src/test/java/com/example/chat/approval`.)

## Review Checklist

**SDD Completeness**
- [ ] All required sections filled or explicitly `TBD:`
- [ ] Open Questions populated for blocking ambiguities
- [ ] Domain language consistent with other bounded contexts

**Skeleton Compliance**
- [ ] DDD boundaries and package structure match SDD
- [ ] Spring Boot and project conventions followed (`docs/conventions/CONVENTIONS.md`)
- [ ] No business logic in skeleton stubs
- [ ] No magic constants

**Test Coverage**
- [ ] Each SDD requirement maps to at least one test case
- [ ] Domain/service tests prioritized over API tests
- [ ] `@DisplayName` in Korean; Given/When/Then structure present
- [ ] Boundary and failure scenarios covered

**Consistency**
- [ ] Naming across SDD, skeleton, and tests is aligned
- [ ] Error codes and validation rules are reflected in tests

## Output Format

Produce a review report with:
- **Findings**: severity (BLOCKER / MAJOR / MINOR), location, description
- **Missing coverage** list
- **Recommendations**: concrete fixes
