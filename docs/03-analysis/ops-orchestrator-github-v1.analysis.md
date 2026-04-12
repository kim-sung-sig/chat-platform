# Gap Analysis: ops-orchestrator-github-v1

**Date**: 2026-04-10
**Baseline Documents**:
- [ops-orchestrator-github-v1.plan.md](../01-plan/features/ops-orchestrator-github-v1.plan.md)
- [ops-orchestrator-github-v1.design.md](../02-design/features/ops-orchestrator-github-v1.design.md)
- [ops-orchestrator-github-v1.do.md](../03-do/features/ops-orchestrator-github-v1.do.md)

## 1. Change Coverage

| Item | Target | Current | Status |
|---|---|---|---|
| GitHub-only | Jira excluded, GitHub first | GitHub webhook/sync implemented, no Jira | Done |
| Plugin-ready | TicketConnector SPI | SPI and shared contracts implemented | Done |
| MSA split | Ops services separated | `apps/ops/*` services created | Done |
| PDCA artifacts | plan/design/do/check records | plan/design/do/check docs exist | Done |

## 2. Match Summary

| Group | Total | Done | Partial | Missing |
|---|---:|---:|---:|---:|
| Week 1 Foundation | 5 | 5 | 0 | 0 |
| Week 2 Connector Hardening | 3 | 0 | 1 | 2 |
| Week 3 Plan+Approval Slice | 3 | 1 | 1 | 1 |
| Week 4 Realtime+E2E | 3 | 0 | 1 | 2 |
| **Total** | **14** | **6** | **3** | **5** |

**Match Rate: 54%**

## 3. Key Gaps

### GAP-01: GitHub hardening not complete
- Missing: real GitHub REST integration, retry/backoff, DLQ.
- Impact: low resilience under API failure or rate limit.

### GAP-02: Plan-Approval state coupling incomplete
- Missing: enforced state transitions and plan status updates from approval decisions.
- Impact: approval gate can be bypassed.

### GAP-03: Audit append-only persistence incomplete
- Current: in-memory audit list.
- Impact: weak governance reliability.

### GAP-04: Realtime integration incomplete
- Missing: notification-service to websocket-server publish adapter.
- Impact: realtime UX target not met.

### GAP-05: Vertical E2E and performance checks incomplete
- Missing: full E2E (ticket -> plan -> approval -> notify), p95 latency measurement.
- Impact: Chapter 1 release criteria not met.

### GAP-06: Auth trust boundary needs improvement
- Current: direct trust of `X-Project-Role` header.
- Required: JWT claim-based authorization.

## 4. Next Priority
1. Week2 hardening: real GitHub integration + retry/backoff + DLQ.
2. Week3 coupling: Plan state machine + approval transaction integration.
3. Week3 governance: append-only persisted audit.
4. Week4 quality: websocket publish integration + E2E + p95 measurement.

## 5. Decision
- Foundation is complete.
- Week2-4 remain in progress.
- Next check target: **54% -> 80%+**.