# Do: ops-orchestrator-github-v1

> Feature: Ops Orchestrator v1 (GitHub-only, plugin-ready)
> Date: 2026-04-10
> Phase: do
> Design: [ops-orchestrator-github-v1.design.md](../../02-design/features/ops-orchestrator-github-v1.design.md)

## Implementation Order
1. Wire new Gradle modules in `settings.gradle`
2. Build `ops-contract` module (SPI, envelope, error, RBAC)
3. Implement service skeletons:
   - plugin-registry
   - github-connector
   - plan
   - approval
   - notification
   - governance
4. Add chapter-1 test set:
   - SPI contract unit test
   - GitHub signature verification test
   - role-based API access test
5. Build and test modules

## Chapter 1 Checklist Status
- [x] Week 1 - SPI draft (`TicketConnector`)
- [x] Week 1 - GitHub webhook signature spec + verifier
- [x] Week 1 - common envelope/error model
- [x] Week 1 - RBAC minimal matrix
- [x] Week 1 - foundational test topics implemented
- [ ] Week 2 - retry/backoff + DLQ integration
- [ ] Week 3 - approval E2E with plan-state coupling
- [ ] Week 4 - websocket notification integration and p95 measurement

## Evidence
- New modules added under `apps/ops/*`
- PDCA artifacts created in docs/01-plan, docs/02-design, docs/03-do
- Test targets defined and executable by gradle module
