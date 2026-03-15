# Skeleton: Approval System

## Inputs
- SDD: docs/specs/SDD_approval-system.md

## Package Layout (Generated)
- `com.example.chat.approval`
  - `domain.model`
    - `ApprovalDocument`
    - `ApprovalLine`
    - `Approver`
    - `ApproverType`
    - `ApprovalStatus`
  - `domain.repository`
    - `ApprovalDocumentRepository`
  - `application.service`
    - `ApprovalCommandService`
    - `ApprovalQueryService`
  - `rest.controller`
    - `ApprovalController`
  - `rest.dto.request`
    - `ApprovalCreateRequest`
  - `rest.dto.response`
    - `ApprovalResponse`
    - `ApprovalInboxResponse`
  - `shared`
    - `ApprovalErrorCode`

## API Skeleton (Draft)
- `POST /api/v1/approvals`
- `POST /api/v1/approvals/{id}/submit`
- `POST /api/v1/approvals/{id}/lines/{line}/approve`
- `POST /api/v1/approvals/{id}/lines/{line}/reject`
- `POST /api/v1/approvals/{id}/cancel`
- `GET /api/v1/approvals/{id}`
- `GET /api/v1/approvals/inbox?approverId=`

## SDD Coverage Notes
- Commands/Queries mirrored in services and controller stubs.
- Error codes included as `ApprovalErrorCode` enum.
- Data model fields included in `ApprovalDocument` and response DTOs.

## Open Questions
- Approver type mapping rules (AUTHORITY/POSITION/USER)
- Hiworks reference rules integration
- Duplicate approver across lines policy (TBD)

## Next
- Implement domain rules, validation, and persistence mapping.
- Add repository implementations (JPA) and DTO mapping.
