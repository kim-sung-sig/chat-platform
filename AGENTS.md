# AGENTS.md

Harness-centric multi-agent governance for the Chat Platform.

Core coding and architecture conventions remain in [`docs/conventions/CONVENTIONS.md`](docs/conventions/CONVENTIONS.md).

---

## 1) Governance Model

- Single source of truth for agent governance is `.claude/.harness/`.
- Skills live directly in `.claude/skills/<id>/SKILL.md` — no separate adapter generation needed.
- Canonical command/skill IDs use kebab-case (for example `sdd-requirements`).
- Tool aliases are defined in `.claude/.harness/registries/skills.json`.
- Architecture and coding rules index: `.claude/RULES_INDEX.md` → rules live in `.claude/rules/`.

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

### Optional Skills (registered, team-mutable)

Skills beyond the SDD core chain that are registered in `skills.json`:

| ID | Purpose |
|----|---------|
| `arch-policy` | DDD layer boundary decisions |
| `tdd-cycle` | TDD red→green→refactor loop |
| `sdd-craft` | SDD authoring with domain language |
| `jpa` | JPA entity + repository scaffolding |
| `security` | Security vulnerability review |
| `docs` | Document writing / update |
| `explain` | Code/design explanation |

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

---

## 7) Module Map

Active Gradle modules (`settings.gradle`):

### Infrastructure
| Module | Port | Notes |
|--------|------|-------|
| `infrastructure:config-server` | 8888 | Spring Cloud Config |
| `infrastructure:eureka-server` | 8761 | Service Discovery |
| `infrastructure:api-gateway` | 8000 | API Gateway |

### Apps — Chat
| Module | Port | Notes |
|--------|------|-------|
| `apps:chat:chat-server` | 8081/8082 | Consolidated from message-server + system-server |
| `apps:chat:websocket-server` | 20002 | STOMP WebSocket |
| `apps:auth-server` | — | JWT, OAuth2 |
| `apps:push-service` | — | FCM/APNs push |

`apps:chat:chat-server` bounded contexts:

```
channel/   message/   friendship/   file/   scheduled/   voice/   shared/   storage/
```

Each context uses: `application/` → `domain/` → `infrastructure/` → `rest/` (presentation).

### Apps — Ops Orchestrator (`apps/ops/`)
| Module | Responsibility |
|--------|---------------|
| `apps:ops:ops-contract` | Shared types: `TicketConnector`, `OpsErrorCode`, RBAC (`ProjectRole`, `OpsAction`) |
| `apps:ops:plugin-registry-service` | Plugin lifecycle registry |
| `apps:ops:github-connector-service` | GitHub webhook intake + ticket sync |
| `apps:ops:plan-service` | Feature plan drafts (`PlanDraft`, `PlanStatus`) |
| `apps:ops:approval-service` | Approval flow (`ApprovalRecord`) |
| `apps:ops:notification-service` | Ops-internal notifications |
| `apps:ops:governance-service` | Policy proposals + audit entries |

### Common Libraries
| Module | Purpose |
|--------|---------|
| `common:core` | Exceptions, value objects, events |
| `common:security` | JWT, BCrypt, AES |
| `common:web` | CORS, error handler, JWT filter |
| `common:logging` | MDC, Micrometer Tracing |

### Deprecated (do NOT recreate)
```
apps:chat:message-server       ← merged into chat-server
apps:chat:system-server        ← merged into chat-server
apps:chat:libs:chat-domain     ← migrated to chat-server
apps:chat:libs:chat-storage    ← migrated to chat-server
```

### Build Commands
```bash
# Full build
./gradlew clean build

# Compile check before analyze phase (required by PDCA analyze)
./gradlew :apps:chat:chat-server:compileJava :apps:chat:chat-server:compileTestJava --no-daemon
./gradlew :apps:auth-server:compileJava :apps:auth-server:compileTestJava --no-daemon
./gradlew :apps:ops:approval-service:compileJava --no-daemon   # example ops module

# Run tests per module
./gradlew :apps:chat:chat-server:test
./gradlew :apps:auth-server:test

# Done gate
pwsh -File scripts/done-gate.ps1
```

## Encoding Policy (Codex)

- All text files MUST be UTF-8.
- UTF-8 BOM is prohibited.
- Unless explicitly requested otherwise, every new/updated file must follow UTF-8 (no BOM).
