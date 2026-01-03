# 🎯 Common 모듈 재구조화 계획

## 📊 현재 상태 분석

### 현재 구조의 문제점
```
common/
├── util/                    # 👎 너무 광범위 (상수, 예외, 유틸 혼재)
│   ├── constants/          # 시스템 상수
│   ├── exception/          # 예외 처리
│   └── util/               # 유틸리티
├── auth-security/          # 👎 auth 관련이 2개로 분리됨
├── auth-jwt/               # 👎 auth 관련이 2개로 분리됨
└── logging/                # ✅ OK
```

**문제점:**
1. `util`에 너무 많은 책임이 혼재 (상수, 예외, 유틸)
2. `auth-security`와 `auth-jwt`가 분리되어 의존성 관리 복잡
3. Web 계층(DTO, Controller 공통)과 도메인/인프라 분리 안 됨
4. 코드 컨벤션(상수, 규칙) vs 실제 구현체 분리 안 됨

---

## 🎯 목표: 관심사 분리 (SoC - Separation of Concerns)

### 핵심 분리 기준
1. **계층별 분리**: Web / Domain / Infrastructure
2. **용도별 분리**: 코드 컨벤션(상수, 규칙) / 실제 구현체
3. **기능별 분리**: 인증 / 로깅/ 예외처리 / 유틸

---

## ✅ 제안: 새로운 폴더 구조

```
common/
├── core/                           # 🎯 핵심 도메인 공통 (모든 모듈이 의존)
│   ├── constants/                  # 시스템 전역 상수
│   │   ├── HeaderConstants         # HTTP 헤더 상수
│   │   ├── SystemConstants         # 시스템 상수
│   │   └── ErrorCodes              # 에러 코드 상수
│   ├── exception/                  # 예외 처리 프레임워크
│   │   ├── BaseException
│   │   ├── ErrorCode
│   │   └── ErrorResponse
│   └── util/                       # 순수 유틸리티 (외부 의존성 없음)
│       ├── DateTimeUtil
│       ├── IdGenerator
│       └── StringUtil
│
├── web/                            # 🌐 Web 계층 공통 (Controller, DTO)
│   ├── dto/                        # 공통 DTO
│   │   ├── PageRequest
│   │   ├── PageResponse
│   │   └── CursorResponse
│   ├── filter/                     # 공통 필터
│   │   ├── RequestLoggingFilter
│   │   └── CorsFilter
│   ├── advice/                     # 전역 예외 핸들러
│   │   └── GlobalExceptionHandler
│   └── config/                     # Web 설정
│       ├── WebMvcConfig
│       └── OpenApiConfig
│
├── security/                       # 🔐 보안 통합 모듈
│   ├── core/                       # 보안 핵심
│   │   ├── model/
│   │   │   ├── AuthUser           # 인증 사용자 모델
│   │   │   └── UserId
│   │   └── exception/
│   │       ├── AuthException
│   │       └── AuthErrorCode
│   ├── jwt/                        # JWT 인증
│   │   ├── filter/
│   │   ├── provider/
│   │   └── config/
│   └── oauth2/                     # OAuth2 (미래 확장)
│
├── logging/                        # 📝 로깅 (현재 그대로 유지)
│   └── ...
│
└── infrastructure/                 # 🏗️ 인프라 공통
    ├── redis/                      # Redis 공통
    │   ├── config/
    │   └── util/
    ├── messaging/                  # 메시징 공통 (Kafka, RabbitMQ)
    │   ├── config/
    │   └── producer/
    └── cache/                      # 캐싱 전략
        └── config/
```

---

## 📦 모듈 의존성 그래프

```
┌─────────────────────────────────────────────────────────┐
│                    common:core                          │
│  (모든 모듈의 기반 - 순수 Java, 최소 의존성)            │
└────────────────────┬────────────────────────────────────┘
                     │ (의존)
        ┌────────────┼────────────┬────────────┐
        ▼            ▼            ▼            ▼
  common:web   common:security  common:logging  common:infrastructure
  (Web 계층)   (보안 통합)      (로깅)         (Redis, 메시징 등)
        │            │
        └────────────┴───────────► apps (실제 애플리케이션)
```

---

## 🔄 마이그레이션 계획

### Phase 1: Core 분리 (우선순위 ⭐⭐⭐)
```bash
common/util → common/core
- constants/    → common/core/constants/
- exception/    → common/core/exception/
- util/         → common/core/util/
```

### Phase 2: Web 계층 분리 (우선순위 ⭐⭐⭐)
```bash
새로 생성: common/web
- common/util/exception/GlobalExceptionHandler → common/web/advice/
- 공통 DTO, 필터, 설정 추가
```

### Phase 3: Security 통합 (우선순위 ⭐⭐)
```bash
common/auth-security + common/auth-jwt → common/security
- auth-security/common/auth/model → security/core/model
- auth-jwt → security/jwt
```

### Phase 4: Infrastructure 분리 (우선순위 ⭐)
```bash
새로 생성: common/infrastructure
- Redis, Kafka, 캐싱 등 인프라 관련 공통 모듈
```

---

## 📋 settings.gradle 변경안

### Before (현재)
```groovy
include("common:util")
include("common:logging")
include("common:auth-security")
include("common:auth-jwt")
```

### After (제안)
```groovy
// 🎯 Core - 모든 모듈의 기반
include("common:core")

// 🌐 Web - Web 계층 공통
include("common:web")

// 🔐 Security - 보안 통합
include("common:security")

// 📝 Logging - 로깅
include("common:logging")

// 🏗️ Infrastructure - 인프라 공통 (선택)
include("common:infrastructure")
```

---

## ✅ 장점

### 1. 명확한 관심사 분리
- **core**: 순수 Java, 도메인 핵심
- **web**: Web 계층 전용
- **security**: 보안 통합 (JWT, OAuth2 등)
- **infrastructure**: 인프라 기술 (Redis, Kafka 등)

### 2. 의존성 관리 단순화
```groovy
// 모든 모듈
implementation project(':common:core')

// Web 애플리케이션만
implementation project(':common:web')

// 인증이 필요한 서버만
implementation project(':common:security')
```

### 3. 확장성
- OAuth2 추가 → `common:security:oauth2`
- GraphQL 추가 → `common:web:graphql`
- gRPC 추가 → `common:grpc`

### 4. 재사용성
- 다른 프로젝트에서 `common:core`만 가져가기 쉬움
- MSA 전환 시 모듈 단위로 분리 가능

---

## 🚀 즉시 적용 가능한 액션 플랜

### Step 1: `common:core` 생성 (15분)
1. `common/core` 폴더 생성
2. `util/constants`, `util/exception`, `util/util` 이동
3. `settings.gradle` 업데이트
4. 빌드 확인

### Step 2: `common:web` 생성 (10분)
1. `common/web` 폴더 생성
2. `GlobalExceptionHandler` 이동
3. 공통 DTO 추가
4. 빌드 확인

### Step 3: `common:security` 통합 (20분)
1. `common/security` 폴더 생성
2. `auth-security` + `auth-jwt` 통합
3. `security/core`, `security/jwt` 구조 생성
4. 빌드 확인

---

## 💡 추천: 즉시 시작

**가장 효과적인 순서:**
1. ✅ `common:core` 분리 (가장 중요, 모든 모듈이 의존)
2. ✅ `common:web` 생성 (Web 계층 정리)
3. ✅ `common:security` 통합 (auth 관련 통합)
4. ⏳ `common:infrastructure` (나중에 필요할 때)

**예상 소요 시간:** 1시간 이내

---

이제 실제로 리팩터링을 진행할까요? 원하시는 단계를 선택해주세요:
- A) 전체 자동 리팩터링 (모든 단계 한 번에)
- B) Phase 1만 먼저 (common:core 분리)
- C) 추가 질문/의견 후 진행

