# AGENT_COMMANDS.md

Claude slash commands for the Chat Platform.
All commands live in [`.claude/commands/`](.claude/commands/) and are invoked with `/command-name <arguments>`.

---

## Full SDLC Loop (권장)

| Command | Purpose |
|---------|---------|
| `/sdlc <task>` | **전체 루프 자동 실행**: planner → developer → reviewer(점수≤80이면 developer 재투입, 최대 3회) → qa |

## 개별 Phase 커맨드

| Command | Purpose |
|---------|---------|
| `/plan <raw requirements>` | Planner: structure requirements into a planning doc |
| `/develop <task or question>` | Developer: implement with DDD/TDD discipline |
| `/review <scope>` | Reviewer: architect-level review, scored 0–100 |
| `/qa <scope>` | QA: functional test simulation, bug reporting |
| `/refactor <scope>` | Refactor structure without changing behavior |

## SDD Skill Chain

```
/sdd-requirements  →  /sdd-read  →  /spec-to-skeleton  →  /skeleton-to-tests  →  /sdd-review
```

| Command | Purpose |
|---------|---------|
| `/sdd-requirements <raw requirements>` | Convert raw input into an SDD document |
| `/sdd-read <SDD path>` | Produce a developer handoff brief from an SDD |
| `/spec-to-skeleton <SDD path>` | Generate compilable code stubs from an SDD |
| `/skeleton-to-tests <SDD path> <skeleton path>` | Generate TDD tests from SDD + skeleton |
| `/sdd-review <SDD path> <skeleton path> <test path>` | Review SDD, skeleton, and tests for compliance |

## Utility Commands

| Command | Purpose |
|---------|---------|
| `/explain <scope>` | Explain code or design with DDD/architectural context |
| `/patch <task>` | Implement a focused, minimal change |
| `/perf <scope>` | Analyze performance hotspots (N+1, caching, indexes) |
| `/security <scope>` | Review for authentication, authorization, and injection risks |
| `/docs <scope>` | Update or draft documentation (SDD, context README, API docs) |

---

## Usage Examples

```
/plan 채널 멤버 역할 권한 기능 추가
/develop apps/chat/chat-server/src/main/java/com/example/chat/approval 에 반려 기능 추가
/review apps/chat/chat-server/src/main/java/com/example/chat/voice
/qa docs/specs/SDD_approval-system.md
/sdd-requirements 메시지 예약 발송 기능 — 1회성 및 반복 발송 지원
/spec-to-skeleton docs/specs/SDD_scheduled-message.md
/skeleton-to-tests docs/specs/SDD_scheduled-message.md apps/chat/chat-server/src/main/java/com/example/chat/schedule
/perf apps/chat/chat-server/src/main/java/com/example/chat/channel/application
/security apps/chat/chat-server/src/main/java/com/example/chat/approval/rest
```
