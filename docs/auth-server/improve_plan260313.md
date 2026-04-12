# Planning Template (Planner)

## 1. Problem Statement
- auth-server는 기본 인증 기능을 제공하지만, 토큰/키 관리, MFA/OTP, WebAuthn, CSRF 등 핵심 보안 영역에 미완성/취약 지점이 존재한다.
- 보안/운영 관점의 고도화 요구(토큰 수명/회전, 리프레시 토큰 보호, 감사/모니터링, 계정 보호)가 충족되지 않는다.

## 2. Goals / Non-Goals
### Goals
- 토큰 모델을 명확히 분리(Access/Refresh/MFA)하고 재사용/탈취 대응을 강화한다.
- 키 관리(JWK/키 회전/환경별 설정)와 서명 알고리즘의 일관성을 확보한다.
- MFA/OTP/WebAuthn의 보안성을 상용 수준으로 끌어올린다.
- 인증 API에 대한 CSRF, rate limit, 계정 보호 정책을 도입한다.

### Non-Goals
- 새로운 IdP/SSO 전환(외부 IdP 완전 이관)은 범위 밖
- 대규모 스키마 재설계(사용자/권한 모델 재정의)까지는 포함하지 않음

## 3. Target Users / Stakeholders
- 모바일/웹 클라이언트 사용자
- 보안 운영 담당자/백엔드 개발자
- SRE/플랫폼 운영팀

## 4. Requirements
### Functional Requirements
- Access/Refresh/MFA 토큰 구분 및 검증 로직
- Refresh Token 회전 및 재사용 탐지(Reuse Detection)
- MFA 단계별 플로우 안정화(OTP/TOTP/WebAuthn)
- 세션/디바이스 관리(디바이스 등록/차단/회수)
- 인증/보안 이벤트 감사 로그

### Non-Functional Requirements
- 보안성: OWASP ASVS 주요 항목 준수
- 가용성: 토큰 검증 경로 단일 장애 지점 제거
- 관측성: 인증 실패/락/의심 이벤트 지표화

## 5. Domain Knowledge (Deep Knowledge)
- 인증은 단일 이벤트가 아닌 상태 전이(Anonymous -> Authenticated -> MFA Pending -> Full Access)
- Refresh Token은 장기 세션 수단이며 탈취 시 피해가 크므로 별도 보호가 필요
- MFA는 인증 신뢰도를 높이지만 UX/지연/복구 플로우 설계가 중요
- WebAuthn은 서버단 검증이 핵심이며 클라이언트 검증만으로는 불충분

## 6. Domain Model & Boundaries
- Bounded Context: Auth
- 핵심 Aggregate
- Principal(계정), Credential(자격증명), RefreshToken(세션), MfaSession(인증 단계)
- Ubiquitous Language
- Access Token / Refresh Token / MFA Token
- Device Trust / Token Rotation / Reuse Detection / Risk Signal

## 7. Data & Interfaces
- Inputs: 로그인 요청, MFA 완료, 토큰 갱신, 로그아웃
- Outputs: Access/Refresh Token, 인증 상태, MFA 요구 여부
- External: SMS/Email Provider, WebAuthn lib, Redis(토큰 상태/락)

## 8. Risks & Assumptions
- 리프레시 토큰 탈취/재사용 공격
- CSRF로 인한 세션 갱신/로그아웃 악용
- WebAuthn 검증 미구현 상태
- OTP 난수/전송 채널 보안 미흡

## 9. Success Metrics
- Refresh Token 재사용 탐지율
- 인증 실패율/락 발생률 감소
- MFA 성공률/복구 성공률
- 보안 사고/취약점 제로 유지

## 10. Open Questions
- 토큰 저장소(Refresh/Session)를 Redis로 이전할지?
- MFA 정책(항상 vs 위험 기반) 기준은?
- 디바이스 신뢰 정책(화이트리스트/블랙리스트) 범위는?

## 11. Validation Plan
- 사용자 시나리오 테스트(로그인, MFA, 갱신, 로그아웃)
- CSRF/Replay/Reuse 공격 시나리오 테스트
- WebAuthn/TOTP 검증 통합 테스트
- 모니터링 알람/로그 검증

## 12. Proposed Work Items (Derived from Review)
1. Token Model 분리 및 검증
- Refresh 토큰에 별도 TokenType 도입
- Access/Refresh 검증 로직 분리
- Reuse detection 및 회전 로직 강화

2. Key Management 정비
- HS256/ES256 혼용 제거
- JWK 기반 서명 키 고정/회전
- 키 길이/환경변수 검증 및 부팅 시 실패

3. CSRF/쿠키 보안
- Refresh/Logout에 CSRF 방어 또는 Double-Submit 적용
- SameSite 정책 재검토(Strict/Lax)

4. MFA/OTP/WebAuthn 강화
- OTP SecureRandom 적용 및 유효기간/재시도 제한
- WebAuthn 서명 검증 실제 구현
- MFA 실패/락/재시도 정책 정의

5. Observability & Audit
- 인증 이벤트 감사 로그 도입
- 의심 이벤트(재사용/락/실패) 지표화
