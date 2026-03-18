# CONVENTIONS.md

Project-wide development principles and conventions for the Chat Platform.

---

## Core Principles

- **SOLID**: single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion.
- **Interfaces first**: define explicit port interfaces before implementations; allow partial interfaces for incremental capabilities.
- **No magic constants**: centralize magic numbers/strings in constants, enums, or domain types — never inline them.
- **Model-based design (DDD)**: model domain concepts explicitly; keep domain logic in the domain layer, not in services or controllers.
- **TDD**: write tests first for domain logic and critical behavior; add regression tests for every bug fix.

---

## Design & Structure

- Favor small, composable units with clear single responsibilities.
- Keep I/O and side effects at system boundaries (controllers, Kafka consumers, Redis adapters); keep core domain logic pure.
- Use constructor injection for all external dependencies.
- **Early return** over nested if-else — keep cyclomatic complexity low.
- No business logic in REST controllers or persistence adapters.

---

## DDD Layering

Each bounded context follows this strict layering:

```
domain/
  model/        ← Aggregates, Value Objects, Domain Events
  service/      ← Domain Services (pure logic, no framework deps)
  repository/   ← Port interfaces only

application/
  service/      ← Command / Query use cases; orchestrate domain

infrastructure/
  kafka/        ← Kafka consumer/producer adapters
  redis/        ← Cache and Pub/Sub adapters
  datasource/   ← JPA repository adapters implementing domain ports

rest/
  controller/   ← REST endpoints (thin; delegate to application layer)
  dto/          ← Request/Response DTOs
```

Rules:
- `domain` layer has **zero** framework dependencies.
- `application` layer depends only on `domain`.
- `infrastructure` and `rest` depend on `application` and `domain`.
- Never let domain types leak into DTOs — map explicitly at the REST boundary.

---

## CQRS

- Write operations → `XxxCommandService`
- Read operations → `XxxQueryService`
- Read queries use **cursor-based pagination** (no offset).
- Write datasource: `source`; read datasource: `replica`.

---

## Testing Conventions

- **Structure**: JUnit 5 `@Nested` per method under test, with `HappyPath`, `Boundary`, `Failure` nested groups as needed.
- **Mocking**: use `@Mock` + `@InjectMocks` (Mockito); mock repositories and external adapters; never mock the domain itself.
- **Display names**: `@DisplayName` in **Korean** for all test classes and methods.
- **Pattern**: Given / When / Then in method bodies and inline comments.
- **Fixtures**: use builders or fixtures to avoid repetitive setup; tests must document intent and edge cases.
- Prefer domain/service unit tests over API tests; add API tests only for contract verification.
- Use TestContainers for integration tests requiring real PostgreSQL or Kafka.

---

## Code Style

- All source files and documents: **UTF-8** encoding.
- Language: Java 21 + Kotlin 1.9; virtual threads are enabled — avoid `ThreadLocal` patterns.
- Lombok (`@Getter`, `@Builder`, etc.) for boilerplate reduction; avoid `@Data` on JPA entities.
- SDD (`docs/specs/SDD_<slug>.md`) is the authoritative spec for every feature; keep domain language consistent with the SDD.

---

## Process

- If a change violates any principle above, call it out and propose an alternative.
- Ask before proceeding when requirements are ambiguous or could change data model boundaries.
- When implementing from an SDD, follow the skill chain: `sdd-requirements` → `spec-to-skeleton` → `skeleton-to-tests` → `sdd-review`.
