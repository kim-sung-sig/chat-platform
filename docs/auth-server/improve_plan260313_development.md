# improve_plan260313 Development Report

## Overview
This document describes what was designed and implemented for `auth-server` based on `improve_plan260313.md`.

## What Was Designed
- Token model separation by intent: Access vs Refresh vs MFA tokens
- Asymmetric JWT signing (ES256) aligned with JWK exposure
- Safer OTP code generation using cryptographic randomness
- Fail-closed WebAuthn flow until real verification is implemented
- Access token enforcement for request principal resolution

## What Was Implemented
### 1. Token Model Separation
- Added `TokenType.REFRESH` to represent refresh tokens distinctly.
- Access tokens are issued as `FULL_ACCESS`, refresh tokens as `REFRESH`.
- Introduced explicit token type verification:
  - `verifyAccessToken(...)`
  - `verifyRefreshToken(...)`
  - `verifyMfaToken(...)`
- Enforced access-token-only usage in `CurrentPrincipalResolver`.

### 2. JWT Algorithm Consistency (ES256)
- `TokenService` now signs and verifies JWTs using ES256 with `ECKey` from `JwtConfig`.
- JWK exposure and signing algorithm are aligned.
- Added persisted JWK file loading and storage via `jwt.jwk-file` to keep tokens valid across restarts.

### 3. OTP Security
- OTP generation now uses `SecureRandom` rather than `Math.random`.

### 4. WebAuthn Safety
- Added guard to reject empty challenge/clientData/attestationObject.
- `verifySignature(...)` fails closed (returns `false`) until a real WebAuthn verification flow is implemented.
- Added feature flag `auth.webauthn.enabled` to avoid accidental rollout.

## Key Files Changed
- `apps/auth-server/src/main/java/com/example/chat/auth/server/core/domain/TokenType.java`
- `apps/auth-server/src/main/java/com/example/chat/auth/server/core/service/TokenService.java`
- `apps/auth-server/src/main/java/com/example/chat/auth/server/common/security/CurrentPrincipalResolver.java`
- `apps/auth-server/src/main/java/com/example/chat/auth/server/application/service/MfaApplicationService.java`
- `apps/auth-server/src/main/java/com/example/chat/auth/server/core/service/OtpService.java`
- `apps/auth-server/src/main/java/com/example/chat/auth/server/core/service/WebAuthnService.java`

## Design Decisions
- **Token verification is type-aware** so refresh tokens cannot be used as access tokens.
- **Asymmetric signing (ES256)** avoids sharing secrets with resource servers and matches JWK output.
- **Fail-closed WebAuthn** prevents silent auth bypass while the verification pipeline is incomplete.

## Remaining Work / Gaps
- CSRF protection for cookie-based refresh/logout endpoints
- Refresh token reuse detection and device/session invalidation
- Key rotation strategy and `kid` history (persistence now supported via `jwt.jwk-file`)
- Real WebAuthn validation (attestation, client data, signature)
- OTP expiration and retry-limit enforcement

## Tests
- Not run (no automated test changes executed).

## How It Was Developed
- Scoped changes to security-critical flows first (token issuance/verification and OTP generation).
- Kept API contracts stable while enforcing stricter token-type checks.
- Minimized surface area of changes to reduce risk in unrelated modules.
