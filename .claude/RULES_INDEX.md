# Rules Index

Rule set for Chat Platform. Each rule has a core `.md` + `/docs/` with detailed examples.

**Structure**:
```
.claude/rules/
├── ddd-layered-architecture.md     (root, single file)
├── record-dto-pattern/
│   ├── record-dto-pattern.md       (core rule)
│   └── docs/examples.md
├── cqrs-pattern/
│   ├── cqrs-pattern.md
│   └── docs/examples.md
├── ...
```

---

## Quick Navigation

### Architecture Rules

| Rule | File | Scope |
|------|------|-------|
| DDD Layered | `ddd-layered-architecture.md` | 4-layer structure (api→app→domain→infra) |
| Record DTO | `record-dto-pattern/record-dto-pattern.md` | Immutable DTOs |
| CQRS | `cqrs-pattern/cqrs-pattern.md` | Command/Query separation |
| Events | `event-driven-architecture/event-driven-architecture.md` | Domain events & handlers |

### Practice Rules

| Rule | File | Scope |
|------|------|-------|
| Testing | `testing-conventions/testing-conventions.md` | JUnit 5, @Nested, Korean @DisplayName |
| Repository | `repository-pattern/repository-pattern.md` | Port interface & JPA adapter |
| Code Style | `lombok-and-code-style/lombok-and-code-style.md` | Lombok, Java 21 |
| Build | `build-and-compile/build-and-compile.md` | Gradle, compile checks |
| SDD | `sdd-driven-development/sdd-driven-development.md` | 8-section design doc |

---

## By PDCA Phase

| Phase | Rules |
|-------|-------|
| **plan** | SDD (section 1) |
| **design** | SDD (all 8 sections) + DDD Layered |
| **do** | All rules |
| **analyze** | Build (compile first) + Testing (coverage) |
| **report** | SDD (completeness) |

---

## Key Decisions per Rule

| Rule | Key Decision |
|------|-------------|
| **DDD Layered** | JPA allowed in domain (Layered, not Clean Arch) |
| **Record DTO** | `record` only — no class; `from()` factory on response |
| **CQRS** | `<Entity>CommandService` / `<Entity>QueryService` + cursor pagination |
| **Events** | Publish AFTER `save()`; handlers log errors, never propagate |
| **Testing** | Mock only externals; never mock domain |
| **Repository** | Interface in `domain/`, impl in `infrastructure/` |
| **Code Style** | `@RequiredArgsConstructor` on services; `@Getter @Builder` on entities |
| **Build** | `./gradlew` always; compile passes before analyze phase |
| **SDD** | Code must match SDD; domain language consistent everywhere |

---

## Code Review Checklist

- [ ] DDD Layered: layer responsibilities correct?
- [ ] Record DTO: DTOs are `record`, factory methods exist?
- [ ] CQRS: CommandService vs QueryService split?
- [ ] Events: published after `save()`?
- [ ] Testing: @Nested, Korean @DisplayName, no domain mocks?
- [ ] Repository: interface in domain, impl in infrastructure?
- [ ] Code Style: constructor injection, no magic constants?
- [ ] Build: compilation passes?
- [ ] SDD: code matches SDD?

---

**Last Updated**: 2026-04-08
