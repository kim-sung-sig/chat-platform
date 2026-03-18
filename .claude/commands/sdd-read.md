Translate an SDD into a developer handoff brief that is decision-complete for implementation.

Input: $ARGUMENTS
(Provide the SDD path, e.g. `docs/specs/SDD_approval-system.md`, and optionally the planning doc path.)

## Instructions

1. Read the SDD and extract: goals, constraints, required outputs, API/interface changes, test expectations.
2. If a planning document is provided, merge its goals/non-goals and risks.
3. Produce a developer brief at `docs/briefs/<slug>_dev-brief.md` with:
   - **Goal** (1–3 bullets)
   - **Scope** (paths/modules to touch)
   - **Constraints** (must/forbidden)
   - **Done Criteria** (explicit checks)
   - **Tests** (run or skip + reason)
   - **Inputs** (links to SDD and planning doc)
4. Do not make implementation decisions unless already specified in the SDD.
5. The brief format must be compatible with the `/develop` command.

## Skill Connection

Output brief → used as input for `/develop` or `/spec-to-skeleton`.
