# Test Plan: Approval System

## Inputs
- SDD: docs/specs/SDD_approval-system.md
- Skeleton: docs/design/skeletons/approval-system/README.md

## Requirement -> Test Matrix
- T1 (FR1): ApprovalDocument 생성 시 ApprovalLine 포함 검증
- T2 (FR2): lineNumber 1..6 범위 검증 (0,7 실패)
- T3 (FR3): 한 라인에 결재자 1명만 허용
- T4 (FR4): Approver type 3종 수용 및 직렬화
- T5 (FR5): 승인/반려/취소 상태 전이 검증

## Domain Unit Tests (Priority)
- ApprovalLine
  - lineNumber 범위 검증
  - approver 단일성 검증
- ApprovalDocument
  - submit/approve/reject/cancel 전이 규칙
  - 최종 상태 이후 변경 불가

## Application Service Tests
- ApprovalCommandService
  - create: line range, approver type validation
  - approve/reject: 상태 전이 및 이벤트 발행

## API Tests (Supplemental)
- create 승인문서 성공/실패 케이스
- approve/reject 권한 없음 시 실패 (TBD: 권한 규칙)

## Open Questions
- Approver type별 실제 사용자 매핑 규칙
- Hiworks 규칙 확정 후 추가 시나리오 필요