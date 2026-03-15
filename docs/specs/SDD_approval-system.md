# SDD: Approval System

## 1. Title / Version / Status / Owners
- Title: Approval System
- Version: 0.1
- Status: Draft
- Owners: TBD
- Related Docs (Planning/Skeleton/Test):
  - Skeleton: docs/design/skeletons/approval-system/README.md
  - Tests: docs/tests/approval-system_testplan.md

## 2. Problem Statement
- 결재 가능한 시스템이 필요하다.
- 조직 내부 결재 프로세스를 표준화하고 추적 가능하게 만든다.

## 3. Goals / Non-Goals
### Goals
- 결재문서, 결재라인(1~6차), 결재자 모델을 제공한다.
- 결재라인별 1인 결재 규칙을 보장한다.
- 결재자 타입(권한/직책/직접사용자)을 지원한다.

### Non-Goals
- 외부 전자결재 연동 구현은 범위 밖.
- 문서 편집기/템플릿 제공은 범위 밖.

## 4. Stakeholders / Target Users
- 일반 사용자(결재 기안)
- 결재자(승인/반려)
- 관리자(권한/직책 관리)

## 5. Requirements
### Functional Requirements
- FR1: 결재문서는 결재라인을 가진다.
- FR2: 결재라인은 1차부터 최대 6차까지 가능하다.
- FR3: 각 결재라인에는 결재자 1명만 매핑된다.
- FR4: 결재자는 권한, 직책, 직접사용자 타입 중 하나이다.
- FR5: 결재문서의 승인/반려/취소 상태를 추적한다.

### Non-Functional Requirements
- NFR1: 결재 상태 변경은 감사 로그로 남는다.
- NFR2: 결재 조회는 2초 이내 응답을 목표로 한다.

## 6. Domain Knowledge
- Glossary:
  - 결재문서: 승인 대상 문서
  - 결재라인: 순차 승인 단계
  - 결재자: 승인 권한을 가진 주체
- Invariants:
  - 결재라인은 1~6차 범위
  - 각 결재라인은 단일 결재자
- Domain rules and edge cases:
  - 중간 단계 반려 시 이후 라인은 비활성
  - 결재자 타입별 실제 사용자 해석 규칙은 TBD

## 7. Domain Model & Boundaries
- Bounded Context: Approval
- Aggregates:
  - ApprovalDocument (aggregate root)
- Entities / Value Objects:
  - ApprovalLine
  - Approver (type + reference)
  - ApprovalStatus

## 8. Interfaces
- Commands:
  - CreateApprovalDocument
  - SubmitApprovalDocument
  - ApproveLine
  - RejectLine
  - CancelDocument
- Queries:
  - GetDocumentStatus
  - ListInbox
- Error codes (draft):
  - APPROVAL-DOC-NOT-FOUND
  - APPROVAL-LINE-OUT-OF-RANGE
  - APPROVAL-ALREADY-FINALIZED
- External systems / integrations:
  - TBD (e.g., Hiworks rule reference)

## 9. Data Model
- ApprovalDocument
  - id, title, content, status, createdBy, createdAt
- ApprovalLine
  - lineNumber(1..6), approver, status, decidedAt
- Approver
  - type: AUTHORITY | POSITION | USER
  - refId

## 10. Workflow / State Transitions
- Draft -> Submitted -> InReview -> Approved / Rejected
- Cancel is allowed before final approval

## 11. Validation Rules & Edge Cases
- 라인 번호는 1..6 범위
- 중복 라인 번호 금지
- 동일 결재자가 다중 라인에 중복 배치 여부 TBD

## 12. Security / Privacy / Compliance
- 결재 문서 접근은 작성자/결재자만 허용
- 감사 로그는 수정 불가

## 13. Observability
- 승인/반려/취소 이벤트 로그
- 결재 대기 큐 길이 메트릭

## 14. Test Strategy
- 라인 범위/중복 검증 테스트
- 승인/반려 상태 전이 테스트
- 결재자 타입별 해석 규칙 테스트(TBD는 스킵)

## 15. Risks & Assumptions
- Hiworks 규칙 상세가 불명확함
- 결재자 타입 해석 방식 미정

## 16. Open Questions
- Hiworks 기준의 구체 룰을 어떤 문서로 확정할까?
- 권한/직책 타입을 실제 사용자로 매핑하는 규칙은?

## 17. Traceability
- FR1 -> T1
- FR2 -> T2
- FR3 -> T3
- FR4 -> T4
- FR5 -> T5

## 18. References
- Hiworks 결재 규칙 (TBD)