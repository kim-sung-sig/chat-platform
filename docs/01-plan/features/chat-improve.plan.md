# Plan: chat-improve

> **Feature**: 채팅 애플리케이션 전면 고도화 (백엔드 + 프론트엔드)
> **Date**: 2026-03-24
> **Phase**: plan
> **Agent Team**: 3명 병렬 분석 (Backend Explorer / Frontend Explorer / Architecture Analyzer)

---

## Executive Summary

| Perspective | Description |
|-------------|-------------|
| **Problem** | 채팅 플랫폼의 핵심 기능(예약발송·파일업로드·승인워크플로우)이 미완성이고, 프론트엔드는 mock 데이터 의존·가상스크롤 미구현·접근성 미비로 프로덕션 품질에 미달한다 |
| **Solution** | P0~P3 우선순위로 분류된 23개 개선 항목을 SDD→TDD→구현 파이프라인으로 단계적 완성하고, 아키텍처 컨벤션 준수율을 100%로 끌어올린다 |
| **Function UX Effect** | 예약발송·타이핑 인디케이터·파일 업로드 진행률·오프라인 큐 구현으로 사용자 체감 품질을 Discord 수준으로 향상시키고, 가상스크롤로 대용량 채널에서도 60fps 유지 |
| **Core Value** | 완성도 높은 실시간 협업 채팅 플랫폼을 통해 팀 생산성을 높이고, DDD/CQRS 컨벤션 100% 준수로 유지보수 비용을 최소화한다 |

---

## 1. 현황 분석 요약

### 1.1 백엔드 (chat-platform)

**완성도 높은 영역:**
- ✅ 실시간 메시지 (WebSocket + Redis Pub/Sub): 100%
- ✅ 친구 관리 API (CQRS): 100%
- ✅ 채널 메타데이터 관리: 100%
- ✅ 읽음 처리 (Kafka 기반): 95%
- ✅ 푸시 알림 서비스 (SSE + Slack/Teams): 90%

**미완성 영역:**
- ⚠️ 예약 발송 (schedule_rules 테이블 존재, Quartz Job 없음): 30%
- ❌ 파일 업로드 API (endpoint 없음, 스키마만 존재): 10%
- ❌ 승인 시스템 (8개 endpoint 모두 UnsupportedOperationException): 5%
- ❌ 메시지 검색 (전문검색 API 없음): 0%
- ❌ 타이핑 인디케이터 (WebSocket endpoint 없음): 0%
- ❌ 메시지 리액션 (도메인 모델 없음): 0%

**아키텍처/컨벤션 이슈:**
- ⚠️ friendship·approval 모듈: domain 패키지 없음 (Value Object, Aggregate Root 미정의)
- ⚠️ auth-server·push-service·websocket-server: 테스트 파일 0개
- ⚠️ 도메인 이벤트 미발행: friendship, voice, approval 모듈
- ❌ Rate Limiting 없음 (메시지 전송, 친구 요청 endpoint)
- ❌ Audit Logging 없음 (승인·삭제 등 민감 작업)
- ⚠️ PushMessage 엔티티에 @Setter 사용 (컨벤션 위반)
- ⚠️ FriendshipController: @CurrentUser 대신 @RequestHeader 직접 사용

### 1.2 프론트엔드 (chat-view, Nuxt 3)

**완성도 높은 영역:**
- ✅ Discord 스타일 레이아웃: 90%
- ✅ PWA 설정 (서비스 워커, 매니페스트): 90%
- ✅ 테마 (Dark/Light/OLED): 100%
- ✅ 친구 목록 UI: 85%
- ✅ 이모지 피커: 80%

**미완성/미구현 영역:**
- ❌ 가상 스크롤 (1000+ 메시지 렌더링 이슈): 미구현
- ❌ 예약 발송 UI: 미구현
- ❌ 타이핑 인디케이터 표시: 미구현
- ❌ 읽음 확인 UI: 미구현
- ❌ 파일 업로드 진행률: 미구현
- ❌ 오프라인 메시지 큐: 미구현
- ❌ 메시지 검색 UI: 미구현
- ❌ 접근성 (ARIA, 키보드 내비게이션): 미구현
- ⚠️ mock 데이터 스토어 (`data.ts`) vs 실 API 스토어 (`chat.ts`) 이중 구조
- ⚠️ `parseUserFromToken()` 함수 미정의 (런타임 에러)
- ⚠️ WebSocket 토큰 URL 노출 (보안 이슈)

---

## 2. Spec Checklist

### P0 — 즉시 수정 (프로덕션 블로커)

- [ ] **[BE-P0-1] parseUserFromToken 에러 수정**
  - 현상: auth.ts 스토어에서 undefined 함수 호출로 런타임 크래시
  - 수정: JWT payload 파싱 유틸리티 구현 또는 AuthService에서 응답 재사용

- [ ] **[BE-P0-2] PushMessage @Setter 제거 (컨벤션 위반)**
  - CONVENTIONS.md: JPA 엔티티에 @Setter 금지
  - 수정: `updateStatus(PushStatus status)` 메서드로 교체

- [ ] **[BE-P0-3] FriendshipController @CurrentUser 통일**
  - `@RequestHeader("X-User-Id")` → `@CurrentUser` 애노테이션으로 교체

- [ ] **[FE-P0-1] mock data 스토어 분리**
  - `store/data.ts` mock init을 feature flag로 분리
  - `store/chat.ts`를 완성하여 실 API만 사용하는 구조로 전환

- [ ] **[FE-P0-2] WebSocket 토큰 보안 개선**
  - 토큰을 URL 파라미터 대신 WebSocket 초기 프레임 (CONNECT frame)에 포함
  - 또는 쿠키 기반 인증으로 전환

- [ ] **[BE-P0-4] Rate Limiting 구현**
  - API: 메시지 전송 (10/s per user), 친구 요청 (5/min per user)
  - Spring Cloud Gateway 필터 또는 Bucket4j 라이브러리

- [ ] **[BE-P0-5] 글로벌 ExceptionHandler 통일**
  - 현재 각 모듈별 분산 — `@ControllerAdvice` 통합 응답 포맷 적용

### P1 — 미완성 핵심 기능

- [ ] **[BE-P1-1] 예약 발송 기능 완성**
  - HTTP API: `POST /api/messages/schedule`, `GET /api/messages/schedule/{channelId}`, `DELETE /api/messages/schedule/{id}`
  - Domain: `ScheduledMessage` aggregate root 생성
  - Command/Query: `ScheduledMessageCommandService`, `ScheduledMessageQueryService`
  - Job: Quartz Scheduler 의존성 추가 + `ScheduledMessageJob` 구현
  - 도메인 이벤트: `MessageScheduledEvent`, `ScheduledMessageExecutedEvent`

- [ ] **[BE-P1-2] 파일 업로드 API**
  - HTTP API: `POST /api/files/upload` (multipart/form-data)
  - 스토리지: S3 또는 MinIO 연동 (Pre-signed URL 방식)
  - Response: `{ fileUrl, fileName, fileSize, mimeType }`
  - 파일 타입/크기 검증 (이미지: 10MB, 파일: 50MB)

- [ ] **[BE-P1-3] 타이핑 인디케이터 WebSocket**
  - WebSocket endpoint: `/ws/typing/{channelId}`
  - 메시지 포맷: `{ type: TYPING_START | TYPING_STOP, userId, channelId }`
  - TTL: 3초 후 자동 만료 (Redis TTL 또는 Quartz 타이머)

- [ ] **[FE-P1-1] 예약 발송 UI**
  - MessageInput.vue에 "예약 발송" 버튼 추가
  - 날짜/시간 선택 다이얼로그
  - 예약된 메시지 목록 사이드패널
  - 예약 취소 기능

- [ ] **[FE-P1-2] 파일 업로드 진행률 UI**
  - 업로드 progress bar (ProgressBar 컴포넌트)
  - 드래그앤드롭 지원
  - 이미지 미리보기 + 압축 옵션
  - 파일 크기 초과 에러 메시지

- [ ] **[FE-P1-3] 타이핑 인디케이터 UI**
  - `"홍길동님이 입력 중..."` 표시
  - 3초 이상 유지 시 사라짐
  - 여러 사용자 동시 타이핑: `"홍길동, 김철수님이 입력 중..."`

- [ ] **[FE-P1-4] 가상 스크롤 구현**
  - `vue-virtual-scroller` 또는 `@tanstack/vue-virtual` 도입
  - 1000+ 메시지에서 60fps 유지
  - 스크롤 위치 복원 (채널 전환 시)

- [ ] **[FE-P1-5] 오프라인 메시지 큐**
  - Service Worker Background Sync API
  - 오프라인 시 전송 실패 메시지를 IndexedDB에 저장
  - 온라인 복귀 시 자동 재전송

### P2 — 아키텍처 개선

- [ ] **[BE-P2-1] 도메인 이벤트 추가 (friendship·voice·approval)**
  - `FriendshipAcceptedEvent`, `FriendRequestSentEvent`
  - `VoiceRoomJoinedEvent`, `VoiceRoomLeftEvent`
  - `ApprovalDocumentSubmittedEvent`, `ApprovalGrantedEvent`
  - 각 모듈의 `domain/event/` 패키지에 생성

- [ ] **[BE-P2-2] Audit Logging**
  - `AuditLog` 엔티티 및 Flyway 마이그레이션 (V10)
  - `@AuditableOperation` AOP Aspect 구현
  - 대상: 승인 작업, 메시지 삭제, 채널 권한 변경, 친구 차단

- [ ] **[BE-P2-3] 테스트 커버리지 확보**
  - auth-server: JWT 발급·검증·갱신 단위 테스트 (목표 80%)
  - push-service: 알림 전송·실패·재시도 단위 테스트
  - websocket-server: 세션 관리·브로드캐스트 통합 테스트

- [ ] **[BE-P2-4] friendship 모듈 도메인 강화**
  - `Friendship` Aggregate Root 생성
  - Value Object: `FriendshipStatus`, `Nickname`
  - 도메인 서비스: `FriendshipDomainService`

- [ ] **[FE-P2-1] 에러 핸들링 전면 보완**
  - 전역 에러 바운더리 (Nuxt `error.vue`)
  - Toast 알림 컴포넌트 (성공/경고/에러)
  - 네트워크 오류 시 재시도 UI
  - 5xx 에러: "잠시 후 다시 시도해주세요" 메시지

- [ ] **[FE-P2-2] 접근성 (a11y) 기본 구현**
  - 모든 아이콘 버튼에 `aria-label` 추가
  - 모달 focus trap 구현
  - 키보드 내비게이션 (Tab, Escape, Enter)
  - 이모지 피커 키보드 지원

### P3 — 추가 기능

- [ ] **[BE-P3-1] 승인 시스템 완성 (ApprovalSystem)**
  - ApprovalCommandService: submit, approve, reject, cancel 구현
  - ApprovalQueryService: getDocumentStatus, listInbox 구현
  - 도메인 상태 전이: DRAFT → PENDING → APPROVED/REJECTED

- [ ] **[BE-P3-2] 메시지 전문 검색 (Full-text Search)**
  - PostgreSQL `tsvector` + GIN 인덱스
  - HTTP API: `GET /api/messages/search?keyword={q}&channelId={id}`
  - 결과 하이라이팅 지원

- [ ] **[BE-P3-3] 메시지 리액션**
  - DB 스키마: `message_reactions` 테이블 (Flyway V11)
  - API: `POST /api/messages/{id}/reactions`, `DELETE /api/messages/{id}/reactions/{emoji}`
  - WebSocket: 리액션 실시간 브로드캐스트

- [ ] **[FE-P3-1] 메시지 검색 UI**
  - 검색 바 (Ctrl+F 단축키)
  - 검색 결과 패널 (인라인 하이라이팅)
  - 날짜 범위·작성자 필터

- [ ] **[FE-P3-2] 읽음 확인 UI**
  - 메시지 하단 아바타 목록 (읽은 사용자)
  - 채널 사이드바: 읽지 않은 수 배지

- [ ] **[FE-P3-3] 성능 모니터링 UI**
  - WebSocket 연결 상태 표시 (상태 바)
  - 오프라인 배너
  - 연결 재시도 상태

---

## 3. 개선 항목 우선순위 매트릭스

| ID | 항목 | 영역 | 우선순위 | 예상 공수 | 비고 |
|----|------|------|----------|-----------|------|
| BE-P0-1 | parseUserFromToken 수정 | FE | P0 | 0.5d | 즉시 수정 |
| BE-P0-2 | PushMessage @Setter 제거 | BE | P0 | 0.5d | 컨벤션 위반 |
| BE-P0-3 | @CurrentUser 통일 | BE | P0 | 0.5d | 컨벤션 위반 |
| FE-P0-1 | mock data 스토어 분리 | FE | P0 | 1d | 필수 |
| FE-P0-2 | WebSocket 토큰 보안 | FE+BE | P0 | 1d | 보안 |
| BE-P0-4 | Rate Limiting | BE | P0 | 1.5d | 보안 |
| BE-P0-5 | 글로벌 ExceptionHandler | BE | P0 | 1d | 품질 |
| BE-P1-1 | 예약 발송 기능 | BE | P1 | 4d | 핵심 기능 |
| BE-P1-2 | 파일 업로드 API | BE | P1 | 3d | 핵심 기능 |
| BE-P1-3 | 타이핑 인디케이터 WS | BE | P1 | 1d | UX |
| FE-P1-1 | 예약 발송 UI | FE | P1 | 3d | 핵심 기능 |
| FE-P1-2 | 파일 업로드 UI | FE | P1 | 2d | 핵심 기능 |
| FE-P1-3 | 타이핑 인디케이터 UI | FE | P1 | 1d | UX |
| FE-P1-4 | 가상 스크롤 | FE | P1 | 2d | 성능 |
| FE-P1-5 | 오프라인 메시지 큐 | FE | P1 | 2d | 신뢰성 |
| BE-P2-1 | 도메인 이벤트 추가 | BE | P2 | 2d | 아키텍처 |
| BE-P2-2 | Audit Logging | BE | P2 | 2d | 컴플라이언스 |
| BE-P2-3 | 테스트 커버리지 | BE | P2 | 4d | 품질 |
| BE-P2-4 | friendship 도메인 강화 | BE | P2 | 2d | 아키텍처 |
| FE-P2-1 | 에러 핸들링 전면 보완 | FE | P2 | 2d | 품질 |
| FE-P2-2 | 접근성 기본 구현 | FE | P2 | 2d | 품질 |
| BE-P3-1 | 승인 시스템 완성 | BE | P3 | 5d | 미구현 |
| BE-P3-2 | 메시지 전문 검색 | BE | P3 | 3d | 추가 기능 |
| BE-P3-3 | 메시지 리액션 | BE+FE | P3 | 3d | 추가 기능 |
| FE-P3-1 | 메시지 검색 UI | FE | P3 | 2d | 추가 기능 |
| FE-P3-2 | 읽음 확인 UI | FE | P3 | 1d | 추가 기능 |
| FE-P3-3 | 성능 모니터링 UI | FE | P3 | 1d | 추가 기능 |

---

## 4. 아키텍처 영향 범위

### 4.1 백엔드 신규/변경 컴포넌트

```
apps/chat/chat-server/
├── message/
│   ├── domain/event/                    [NEW] MessageScheduledEvent 등
│   └── application/
│       └── ScheduledMessageCommandService   [NEW]
├── channel/
│   └── api/controller/
│       └── TypingIndicatorController    [NEW]
├── friendship/
│   └── domain/                          [NEW] Friendship Aggregate Root
├── approval/
│   └── application/service/             [완성] submit/approve/reject 구현
└── file/                                [NEW] 파일 업로드 컨텍스트
    ├── domain/
    ├── application/
    └── api/

apps/chat/libs/chat-storage/
└── migration/
    ├── V10__audit_log.sql               [NEW]
    └── V11__message_reactions.sql       [NEW]

infrastructure/api-gateway/
└── filter/
    └── RateLimitGlobalFilter            [NEW]
```

### 4.2 프론트엔드 신규/변경 컴포넌트

```
chat-view/
├── composables/
│   ├── useTypingIndicator.ts            [NEW]
│   ├── useOfflineQueue.ts               [NEW]
│   └── useVirtualScroll.ts             [NEW]
├── components/
│   ├── ScheduledMessageModal.vue        [NEW]
│   ├── FileUploadProgress.vue           [NEW]
│   ├── TypingIndicator.vue             [NEW]
│   ├── ToastNotification.vue           [NEW]
│   └── MessageSearchPanel.vue          [NEW]
├── store/
│   └── chat.ts                          [완성] mock 스토어 분리
└── services/
    ├── file.service.ts                  [NEW]
    └── scheduled-message.service.ts    [NEW]
```

---

## 5. 기술 스택 추가 사항

### 백엔드 신규 의존성
```groovy
// Quartz Scheduler (예약 발송)
implementation 'org.springframework.boot:spring-boot-starter-quartz'

// Bucket4j (Rate Limiting)
implementation 'com.bucket4j:bucket4j-core:8.10.1'
implementation 'com.bucket4j:bucket4j-redis:8.10.1'

// AWS S3 (파일 업로드)
implementation 'software.amazon.awssdk:s3:2.25.0'
```

### 프론트엔드 신규 의존성
```json
{
  "@tanstack/vue-virtual": "^3.x",    // 가상 스크롤
  "idb": "^8.x",                       // IndexedDB (오프라인 큐)
  "date-fns": "^3.x"                   // 날짜 처리 (예약 발송)
}
```

---

## 6. 컨벤션 준수 체크리스트

이 Plan의 모든 구현은 다음 컨벤션을 준수해야 한다:

- [ ] DDD 레이어 분리: domain → application → infrastructure → api 순서
- [ ] CQRS 명명: `*CommandService` (쓰기), `*QueryService` (읽기)
- [ ] Record DTO 사용: 모든 API request/response는 Java record
- [ ] Factory method: `Response.from(entity)` 패턴 준수
- [ ] @CurrentUser: userId 추출은 반드시 @CurrentUser 사용
- [ ] 도메인 이벤트: 상태 변경 시 domain/event/ 패키지에 이벤트 정의 후 발행
- [ ] 테스트: @Nested + @DisplayName(한국어) 패턴 준수
- [ ] 컴파일 게이트: 구현 후 `./gradlew compileJava compileTestJava --no-daemon` 통과 필수

---

## 7. 다음 단계

이 Plan이 완성되면 다음 순서로 진행한다:

1. **P0 수정 (즉시)**: 컨벤션 위반 및 프로덕션 블로커 5.5일 작업
2. **SDD 작성**: `/pdca design chat-improve` 또는 기능별 SDD (`/sdd-requirements scheduled-message`)
3. **구현 시작**: `/pdca do chat-improve`
4. **Gap 분석**: `/pdca analyze chat-improve`
5. **완료 보고**: `/pdca report chat-improve`
