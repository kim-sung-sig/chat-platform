# AGENTS.md

Project Agent Instructions (Chat Application)

Goals
- Maintain long-term maintainability and testability while shipping safely.

Core Principles
- SOLID: follow single responsibility, open/closed, Liskov, interface segregation, and dependency inversion.
- Interfaces first: prefer explicit interfaces/contracts; allow partial interfaces when modeling incremental capabilities.
- No hard-coded constants: avoid magic numbers/strings; centralize in constants, config, or domain types.
- Model-based design (DDD style): model domain concepts explicitly; keep domain logic in domain layer.
- TDD: write tests first for domain logic and critical behavior; add regression tests for fixes.

Design & Structure
- Favor small, composable units with clear responsibilities.
- Keep I/O and side effects at boundaries; keep core logic pure when possible.
- Use dependency injection for external services.

Testing
- Tests should document intent and edge cases.
- Use fixtures/builders to avoid repetitive setup.

Process
- If a change violates any principle above, call it out and propose an alternative.
- Ask when requirements are ambiguous or could change data model boundaries.
- All documents must be written in UTF-8 encoding.

Skills
- Use `spring-boot` skill for refactoring, architecture guidance, and testing support.

Automated Multi-Agent SDLC Workflow (DDD)
- Core concept: Establish an autonomous SDLC using a multi-agent system grounded in DDD.
- Context management: Maintain `summary.md` or `README.md` for each Bounded Context as the primary knowledge base.
- Agentic workflow & feedback loop:
  - Phase 1: Develop - Implement features.
  - Phase 2: Review - Score output 0-100. If score <= 80, return to Phase 1. If score > 80, proceed to QA.
  - Phase 3: QA (Tester) - Perform functional testing. If bugs found, create a task ticket and return to Phase 1.
  - Phase 4: Moderator - Oversee the process to prevent infinite loops and ensure convergence.

Sub-Agent Roles & Invocation (Keywords)
- Planner (기획자)
  - Role: Clarify inputs and produce a planning document in `.md`.
  - Scope: Turn ambiguous input into explicit goals, non-goals, constraints, risks, and open questions.
  - Knowledge: Deep domain understanding for this project.
  - Requirement: Provide a section in the output to store/update deep domain knowledge.
  - Output: Use the planning template at `docs/planning/PLANNING_TEMPLATE.md` unless otherwise specified.
  - Invoke: `/plan - "기획 raw 데이터"`
- Developer (개발자)
  - Role: Apply architecture and implementation with strong DDD/TDD discipline, model-first design, and clear boundaries.
  - Capability: Decompose concepts into higher/lower-level models (e.g., review-content vs review-target).
  - Responsibility: Critically analyze planner output for clarity, feasibility, and correctness before implementation.
  - Invoke: `/develop - "물어볼 사항"` or `/refactoring - "리펙터링 진행할 내용"`
- Reviewer (리뷰어)
  - Role: Architect-level review of changes, judging alternatives and design quality.
  - Invoke: `/review - "탐색 범위 등등"`
- QA (Tester)
  - Role: Execute or simulate functional tests to assure quality after development.
  - Invoke: `/qa - "탐색 범위 등등"`

Develop Instruction Template
- Use the following structure for `/develop` requests:
  - Goal: What to change (1-3 bullets)
  - Scope: Exact paths/modules to touch
  - Constraints: What is forbidden (e.g., no file rewrites, no BOM)
  - Done Criteria: Explicit checks for completion
  - Tests: Run or skip (and why)
