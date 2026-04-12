# Design: ops-orchestrator-github-v1

> Feature: Ops Orchestrator v1 (GitHub-only, plugin-ready)
> Date: 2026-04-10
> Phase: design
> Plan: [ops-orchestrator-github-v1.plan.md](../../01-plan/features/ops-orchestrator-github-v1.plan.md)

## 1. MSA Service Layout
- `ops-contract`: SPI + shared contracts + RBAC matrix + error model
- `plugin-registry-service`: plugin registration and listing
- `github-connector-service`: webhook verification and ticket sync
- `plan-service`: ticket-based plan draft create/get
- `approval-service`: approve/reject/request-change
- `notification-service`: notification event publishing
- `governance-service`: policy proposal(PR) and audit read

## 2. Interface Contracts
- TicketConnector: `createTicket`, `updateTicket`, `assign`, `comment`, `transition`, `findByExternalId`
- Common types:
  - `TicketRef {source, externalId, projectId}`
  - `EventEnvelope {eventId, traceId, projectId, occurredAt, eventType, payload}`
  - `OpsFailureCategory {AUTH,RATE_LIMIT,VALIDATION,TRANSIENT}`

## 3. Security / Governance
- Header-based RBAC (`X-Project-Role`)
- Role matrix:
  - OWNER: all
  - OPERATOR: sync/plan/agent/alert/notify/audit
  - REVIEWER: plan approve/audit
  - VIEWER: audit only
- Policy update endpoint is proposal-only (`/policies/proposals`), no direct apply.

## 4. Data Flow (Chapter 1)
1. GitHub webhook -> signature verify -> ack
2. GitHub ticket sync -> TicketRef create -> event envelope return
3. Plan create -> plan id create
4. Approval action -> approval record store
5. Notification publish -> notification event envelope return
6. Governance proposal -> audit append

## 5. Failure Handling
- Invalid role -> `OPS-AUTH-001`
- Forbidden action -> `OPS-AUTH-002`
- Invalid GitHub signature -> `OPS-GH-001`
- Missing plan -> `OPS-PLAN-001`