# Ops Orchestrator v1 계획 (운영자동화 중심)

## Summary
- 목표: Jira/GitHub Issue, 에이전트 실행, 승인 게이트(PDCA), 관측(Alert/로그), 실시간 알림(WebSocket)을 하나의 오케스트레이션 서비스로 통합한다.
- v1 성공 기준:
1. Alert/에러 반복 시 자동 티켓 생성 또는 기존 티켓 재오픈
2. 티켓 기반 Plan 자동 생성 후 사용자 필수 승인
3. 에이전트 진행/중단/수정 요청을 웹 콘솔에서 실시간 제어
4. 프로젝트+역할 기반 권한 및 감사로그 제공
- 배포: 구축형 기본, Grafana/로그 저장소는 클라우드 옵션 연결 가능.

## Key Changes
- 서비스 아키텍처
  - 신규 `orchestrator-server` 추가(독립 앱, gateway 뒤 배치).
  - 기존 `websocket-server`는 실시간 이벤트 전달 계층으로 재사용.
  - 이벤트 백본은 Kafka 고정.
- 핵심 서브시스템
  - `Connector Hub`: `TicketConnector` SPI로 Jira/GitHub 우선 구현, Linear/Notion/DB는 후속 플러그인.
  - `Plan Engine`: 티켓 본문/메타데이터 기반 PDCA Plan 초안 생성.
  - `Approval Gateway`: Plan 승인, Plan 변경 승인, 고위험 액션 승인.
  - `Agent Runner Gateway`: 플러그인형 런타임 추상화(Codex/Claude/내부 런타임 확장).
  - `Policy Proposal`: 하네스/스킬/hooks 변경은 직접 반영 금지, 제안 PR 생성 후 승인 반영.
  - `Alert Processor`: Prometheus/Grafana/ELK 이벤트 수집, fingerprint dedup, 자동 티켓화.
- 도메인/흐름
  - 상태머신: `PLAN_DRAFT -> PLAN_PENDING_APPROVAL -> EXECUTING -> REVIEW_PENDING -> DONE|STOPPED`.
  - dedup 정책: fingerprint 동일 시 신규 생성 억제, 기존 티켓 코멘트/재오픈.
  - 권한모델: `Owner`, `Operator`, `Reviewer`, `Viewer`(프로젝트 단위).
- 공개 인터페이스
  - REST: 계획 생성/승인/수정요청, 에이전트 제어, 티켓 동기화, 운영 대시보드 조회.
  - WebSocket 이벤트: `plan.approval.required`, `agent.status.changed`, `ticket.updated`, `alert.triggered`.
  - Kafka 토픽: `ticket.events`, `agent.events`, `approval.events`, `alert.events`, `notification.events`.

## Test Plan
- E2E 플로우
  - Jira/GitHub 티켓 동기화 -> Plan 생성 -> 승인 -> 에이전트 실행 -> 리뷰 대기로 전이 검증.
- 자동 티켓화
  - SLO 위반/API 지연 증가/반복 예외에서 우선순위 매핑 및 티켓 생성 검증.
  - 동일 fingerprint 반복 시 재오픈/업데이트만 수행되는지 검증.
- 실시간/복원력
  - WebSocket 재연결 시 상태 동기화와 이벤트 유실 방지 검증.
  - Kafka 소비자 재시작 시 중복 처리 방지(idempotency) 검증.
- 권한/거버넌스
  - 역할별 승인/제어 API 접근 통제 검증.
  - 정책 변경이 PR 승인 없이 배포되지 않는지 검증.
- 관측성
  - 메트릭-로그-티켓 간 trace 연계 및 대시보드 가시성 검증.

## Assumptions
- 기존 Spring 멀티모듈, API Gateway, WebSocket 서버, 인증 체계를 재사용한다.
- v1 공식 커넥터는 Jira/GitHub만 포함한다.
- 기본 알림은 인앱 + WebSocket이며 이메일/Slack은 후속 확장으로 둔다.
- SLO 자동화 우선 대상은 API 오류율/지연이며, WebSocket 안정성 SLO는 v1.1에서 확장한다.

## 브레인스토밍 확장 백로그
- 티켓 본문/로그 문맥 기반 자동 담당자 추천.
- 리뷰 총괄 에이전트(변경 위험도 점수 + 리뷰 체크리스트 자동 생성).
- 경보 fingerprint별 표준 복구 플레이북 자동 제안.
- 테넌트별 비용/보존 정책 자동 튜닝.
- 커넥터 마켓플레이스(팀별 커스텀 DB/내부 이슈시스템 플러그인 배포).
