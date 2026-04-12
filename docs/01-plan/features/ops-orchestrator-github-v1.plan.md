# Plan: ops-orchestrator-github-v1

> Feature: Ops Orchestrator v1 (GitHub-only, plugin-ready)
> Date: 2026-04-10
> Phase: plan

## Executive Summary

| Perspective | Description |
|---|---|
| Problem | Ticket, approval, notification, and governance flows are fragmented, causing missing approvals and weak traceability. |
| Solution | Deliver a GitHub-only vertical slice first, with a plugin-ready SPI (`TicketConnector`) for future connectors. |
| Function UX Effect | Developers can track ticket -> plan -> approval -> notification in one operational flow. |
| Core Value | Approval compliance target 100%, direct policy apply 0, and end-to-end trace continuity. |

## Scope (v1)
- Include: GitHub webhook/sync, plugin registry, plan create/get, approval actions, notification publish, governance audit/policy proposal, RBAC matrix.
- Exclude: Jira/Linear/Notion production connectors, full alert dedup pipeline, agent control runtime.

## Spec Checklist
- [x] API endpoints (GitHub webhook/sync, plugins, plans, approvals, notifications, audits/policies)
- [x] Domain entities / value objects (`TicketRef`, `EventEnvelope`, `PlanDraft`, `ApprovalRecord`, `PolicyProposal`)
- [x] Command / Query split (service layer create/read)
- [x] Event model (`EventEnvelope`)
- [x] Exception and error cases (`OpsErrorCode`, signature invalid, role forbidden)
- [x] Test scenarios (SPI contract, signature verification, role-based API access)

## Chapter 1 Goals (2026-04-13 ~ 2026-05-10)
- Week 1: Foundation contracts + RBAC + webhook signature spec
- Week 2: GitHub sync hardening (retry/backoff/DLQ hooks)
- Week 3: Plan/Approval vertical slice stabilization
- Week 4: Notification + E2E stitch and release candidate