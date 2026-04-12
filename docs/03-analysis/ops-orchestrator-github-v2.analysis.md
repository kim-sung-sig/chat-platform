# Gap Analysis: ops-orchestrator-github-v2

**Date**: 2026-04-10
**Reference**:
- [ops-orchestrator-github-v1.plan.md](../01-plan/features/ops-orchestrator-github-v1.plan.md)
- [ops-orchestrator-github-v1.design.md](../02-design/features/ops-orchestrator-github-v1.design.md)
- [ops-orchestrator-github-v1.do.md](../03-do/features/ops-orchestrator-github-v1.do.md)

## 1. v2 Readiness Snapshot

| Area | Expected for v2 baseline | Current | Status |
|---|---|---|---|
| Foundation contracts | SPI/event/error/rbac | Implemented | Done |
| GitHub-only path | webhook + sync | Implemented | Done |
| Service split | MSA modules | Implemented | Done |
| Operational hardening | retry/backoff/DLQ/state-coupling | Partial | Gap |
| Realtime and E2E quality | websocket integration + p95 + full E2E | Partial | Gap |

## 2. Match Summary

| Group | Total | Done | Partial | Missing |
|---|---:|---:|---:|---:|
| Week 1 Foundation | 5 | 5 | 0 | 0 |
| Week 2 Connector Hardening | 3 | 0 | 1 | 2 |
| Week 3 Plan+Approval Slice | 3 | 1 | 1 | 1 |
| Week 4 Realtime+E2E | 3 | 0 | 1 | 2 |
| **Total** | **14** | **6** | **3** | **5** |

**Match Rate: 54%**

## 3. Main Gaps to Close
- Real GitHub API client and resilience policy (retry/backoff + DLQ)
- Plan state machine and approval-driven transitions
- Persisted append-only audit trail
- Notification -> websocket live publish bridge
- Full vertical E2E and p95 latency measurement
- JWT claim-based authorization boundary

## 4. v2 Execution Focus
1. Close Week2 hardening first.
2. Close Week3 state and governance coupling.
3. Close Week4 realtime and E2E acceptance.