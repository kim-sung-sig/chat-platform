# Spring Cloud 마이그레이션 완료 보고서

> **작업 날짜**: 2026-02-16  
> **작업자**: AI Assistant  
> **목표**: Kotlin 기반 Spring Cloud Netflix 인프라 구축

---

## ✅ 완료된 작업

### 1. Infrastructure 모듈 생성 (Kotlin)

#### 1.1 Config Server (Port: 8888)

- ✅ **경로**: `infrastructure/config-server`
- ✅ **언어**: Kotlin
- ✅ **역할**: 중앙 집중식 설정 관리
- ✅ **설정 저장소**: `src/main/resources/config-repo/`
	- `application.yml` - 공통 설정
	- `chat-message-server.yml` - Message Server 전용
	- `chat-system-server.yml` - System Server 전용
	- `chat-websocket-server.yml` - WebSocket Server 전용

**주요 코드**:

```kotlin
@EnableConfigServer
@SpringBootApplication
class ConfigServerApplication

fun main(args: Array<String>) {
	runApplication<ConfigServerApplication>(*args)
}
```

#### 1.2 Eureka Server (Port: 8761)

- ✅ **경로**: `infrastructure/eureka-server`
- ✅ **언어**: Kotlin
- ✅ **역할**: 서비스 디스커버리 & 레지스트리
- ✅ **대시보드**: http://localhost:8761

**주요 코드**:

```kotlin
@EnableEurekaServer
@SpringBootApplication
class EurekaServerApplication

fun main(args: Array<String>) {
	runApplication<EurekaServerApplication>(*args)
}
```

#### 1.3 API Gateway (Port: 8000)

- ✅ **경로**: `infrastructure/api-gateway`
- ✅ **언어**: Kotlin
- ✅ **역할**: 단일 진입점, 라우팅, CORS
- ✅ **글로벌 필터**: LoggingFilter.kt

**주요 라우팅 규칙**:

```yaml
routes:
  - id: chat-message-server
    uri: lb://chat-message-server
    predicates:
      - Path=/api/messages/**

  - id: chat-system-server
    uri: lb://chat-system-server
    predicates:
      - Path=/api/channels/**,/api/schedules/**

  - id: chat-websocket-server
    uri: lb:ws://chat-websocket-server
    predicates:
      - Path=/ws/**
```

---

### 2. 기존 서버 Spring Cloud 연동

#### 2.1 의존성 추가

**message-server/build.gradle.kts**

```kotlin
dependencies {
	// Spring Cloud
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	// ...existing dependencies...
}
```

**system-server/build.gradle.kts** (동일)
**websocket-server/build.gradle.kts** (동일)

#### 2.2 Bootstrap 설정 파일 생성

**각 서버의 `src/main/resources/bootstrap.yml`**:

```yaml
spring:
  application:
    name: chat-{server-name}
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 5
```

---

### 3. 빌드 설정 업데이트

#### 3.1 루트 `build.gradle`

```groovy
dependencyManagement {
	imports {
		mavenBom "org.springframework.boot:spring-boot-dependencies:3.5.6"
		mavenBom "org.springframework.cloud:spring-cloud-dependencies:2024.0.0"
	}
}
```

#### 3.2 `settings.gradle`

```groovy
// Infrastructure 모듈 추가
include("infrastructure:config-server")
include("infrastructure:eureka-server")
include("infrastructure:api-gateway")

project(":infrastructure:config-server").buildFileName = "build.gradle.kts"
project(":infrastructure:eureka-server").buildFileName = "build.gradle.kts"
project(":infrastructure:api-gateway").buildFileName = "build.gradle.kts"
```

---

## 📊 파일 생성 현황

### 신규 생성 파일 (총 15개)

#### Config Server (5개)

```
infrastructure/config-server/
├── build.gradle.kts
├── src/main/kotlin/com/example/chat/config/
│   └── ConfigServerApplication.kt
└── src/main/resources/
    ├── application.yml
    └── config-repo/
        ├── application.yml
        ├── chat-message-server.yml
        ├── chat-system-server.yml
        └── chat-websocket-server.yml
```

#### Eureka Server (3개)

```
infrastructure/eureka-server/
├── build.gradle.kts
├── src/main/kotlin/com/example/chat/eureka/
│   └── EurekaServerApplication.kt
└── src/main/resources/
    └── application.yml
```

#### API Gateway (4개)

```
infrastructure/api-gateway/
├── build.gradle.kts
├── src/main/kotlin/com/example/chat/gateway/
│   ├── ApiGatewayApplication.kt
│   └── filter/
│       └── LoggingFilter.kt
└── src/main/resources/
    └── application.yml
```

#### Bootstrap 설정 (3개)

```
apps/chat/message-server/src/main/resources/bootstrap.yml
apps/chat/system-server/src/main/resources/bootstrap.yml
apps/chat/websocket-server/src/main/resources/bootstrap.yml
```

---

## 🚀 실행 방법

### 필수 환경 변수 설정

```powershell
$env:JAVA_HOME="C:\Users\kimsungsig\.jdks\temurin-21.0.7"
```

### 1단계: 인프라 서버 시작 (순서 중요!)

```powershell
# 1. Config Server 시작 (가장 먼저!)
.\gradlew :infrastructure:config-server:bootRun

# 2. Eureka Server 시작 (2번째)
.\gradlew :infrastructure:eureka-server:bootRun

# 3. API Gateway 시작 (3번째)
.\gradlew :infrastructure:api-gateway:bootRun
```

### 2단계: 애플리케이션 서버 시작

```powershell
# Message Server
.\gradlew :apps:chat:message-server:bootRun

# System Server
.\gradlew :apps:chat:system-server:bootRun

# WebSocket Server
.\gradlew :apps:chat:websocket-server:bootRun
```

### 3단계: 확인

```powershell
# Eureka Dashboard
Start http://localhost:8761

# Config Server Health
curl http://localhost:8888/actuator/health

# Gateway Routes
curl http://localhost:8000/actuator/gateway/routes

# Gateway를 통한 API 호출
curl http://localhost:8000/api/messages/health
```

---

## 📝 주요 엔드포인트

| 서비스                  | 포트    | 엔드포인트                    | 설명                 |
|----------------------|-------|--------------------------|--------------------|
| **Config Server**    | 8888  | `/actuator/health`       | Health Check       |
|                      |       | `/{application}/default` | 설정 조회              |
| **Eureka Server**    | 8761  | `/`                      | Dashboard (UI)     |
|                      |       | `/eureka/apps`           | 등록된 앱 목록           |
| **API Gateway**      | 8000  | `/api/messages/**`       | → Message Server   |
|                      |       | `/api/channels/**`       | → System Server    |
|                      |       | `/ws/**`                 | → WebSocket Server |
| **Message Server**   | 8081  | `/api/messages`          | 메시지 발송             |
| **System Server**    | 8082  | `/api/channels`          | 채널 관리              |
| **WebSocket Server** | 20002 | `/ws`                    | WebSocket 연결       |

---

## 🎯 아키텍처 다이어그램

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│   API Gateway (Port: 8000)          │
│   - Routing                         │
│   - Load Balancing (Eureka)         │
│   - CORS                            │
│   - Global Filters (Logging)        │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│   Eureka Server (Port: 8761)        │
│   - Service Registry               │
│   - Service Discovery              │
│   - Health Monitoring              │
└──────┬──────────────────────────────┘
       │
       ├─────────────┬─────────────┬─────────────┐
       ▼             ▼             ▼             ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Message      │ │ System       │ │ WebSocket    │
│ Server       │ │ Server       │ │ Server       │
│ (8081)       │ │ (8082)       │ │ (20002)      │
│ Kotlin       │ │ Java         │ │ Kotlin       │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┴────────────────┘
                        │
                        ▼
          ┌────────────────────────────┐
          │  Config Server (8888)      │
          │  - Centralized Config      │
          │  - Native File System      │
          │  - Git Support (Optional)  │
          └────────────────────────────┘
```

---

## 🔧 설정 우선순위

Spring Cloud Config 적용 시 설정 우선순위:

1. **애플리케이션 로컬** `application.yml` (우선순위 낮음)
2. **Config Server** `{application}.yml` (우선순위 중간)
3. **Config Server** `application.yml` (공통 설정)
4. **환경 변수** / **CLI 인자** (우선순위 높음)

---

## 📈 성능 고려사항

### 1. Config Server 캐싱

- 각 애플리케이션은 시작 시 Config Server에서 설정을 가져와 캐싱
- `/actuator/refresh` 엔드포인트로 런타임 설정 갱신 가능

### 2. Eureka 클라이언트 캐싱

- 서비스 목록을 로컬 캐시에 저장 (기본 30초 갱신)
- 네트워크 부하 최소화

### 3. Gateway 로드 밸런싱

- Ribbon 기반 클라이언트 사이드 로드 밸런싱
- Eureka에서 서비스 인스턴스 목록 가져옴

---

## 🐛 트러블슈팅

### 문제 1: Config Server 연결 실패

```
Could not locate PropertySource: 404 from config server
```

**해결**:

1. Config Server가 먼저 실행되었는지 확인
2. `bootstrap.yml`의 `spring.cloud.config.uri` 확인
3. Config Server 로그에서 설정 파일 로드 확인

### 문제 2: Eureka 등록 실패

```
Cannot execute request on any known server
```

**해결**:

1. Eureka Server 실행 확인
2. `eureka.client.service-url.defaultZone` 확인
3. 방화벽 확인

### 문제 3: Gateway 라우팅 실패 (404)

```
404 NOT_FOUND
```

**해결**:

```powershell
# 라우팅 규칙 확인
curl http://localhost:8000/actuator/gateway/routes | ConvertFrom-Json

# Eureka에 서비스 등록 확인
curl http://localhost:8761/eureka/apps
```

---

## 🎓 학습 포인트

### 1. Spring Cloud Config의 장점

- ✅ 중앙 집중식 설정 관리
- ✅ 환경별 설정 분리 (dev, staging, prod)
- ✅ Git 기반 버전 관리 가능
- ✅ 런타임 설정 변경 가능 (`@RefreshScope`)

### 2. Service Discovery의 장점

- ✅ 동적 서비스 등록/해제
- ✅ 클라이언트 사이드 로드 밸런싱
- ✅ 장애 감지 (Health Check)
- ✅ 서비스 메타데이터 관리

### 3. API Gateway의 장점

- ✅ 단일 진입점 (Single Entry Point)
- ✅ 인증/인가 중앙 관리 가능
- ✅ Rate Limiting 적용 가능
- ✅ 글로벌 필터 (로깅, 모니터링)

---

## 📚 다음 단계

### Phase 2: 고급 기능 추가

- [ ] **Circuit Breaker**: Resilience4j 적용
	- 서비스 장애 시 Fallback 처리
	- 서킷 브레이커 패턴

- [ ] **Distributed Tracing**: Spring Cloud Sleuth + Zipkin
	- 요청 추적 (Trace ID, Span ID)
	- 성능 병목 지점 파악

- [ ] **Config Encryption**: Jasypt 적용
	- DB 비밀번호 암호화
	- API Key 암호화

- [ ] **API Rate Limiting**: Gateway Filter
	- 사용자별/IP별 요청 제한
	- Redis 기반 Rate Limiter

- [ ] **JWT 인증 통합**: Auth Server + Gateway
	- Gateway에서 JWT 검증
	- 인증 필터 체인 구성

### Phase 3: Kubernetes 마이그레이션 (3개월 후)

- [ ] Dockerfile 작성
- [ ] K8s Deployment/Service YAML
- [ ] ConfigMap/Secret으로 전환
- [ ] Ingress 설정
- [ ] Helm Chart 작성

---

## 📖 참고 문서

- [SPRING_CLOUD_INFRASTRUCTURE.md](./SPRING_CLOUD_INFRASTRUCTURE.md) - 상세 가이드
- [README.md](./README.md) - 프로젝트 개요
- [SESSION_PROGRESS_REPORT.md](./SESSION_PROGRESS_REPORT.md) - 이전 작업 내역

---

## ✅ 체크리스트

### 인프라 구축

- [x] Config Server 생성 (Kotlin)
- [x] Eureka Server 생성 (Kotlin)
- [x] API Gateway 생성 (Kotlin)
- [x] 설정 파일 작성 (application.yml, bootstrap.yml)
- [x] 라우팅 규칙 설정
- [x] CORS 설정
- [x] 글로벌 필터 구현

### 기존 서버 연동

- [x] message-server 의존성 추가
- [x] system-server 의존성 추가
- [x] websocket-server 의존성 추가
- [x] bootstrap.yml 생성 (3개 서버)

### 빌드 & 테스트

- [x] 루트 build.gradle 업데이트
- [x] settings.gradle 업데이트
- [x] Config Server 빌드 성공
- [x] Eureka Server 빌드 성공
- [x] API Gateway 빌드 성공

### 문서화

- [x] SPRING_CLOUD_INFRASTRUCTURE.md 작성
- [x] README.md 업데이트
- [x] 마이그레이션 완료 보고서 작성

---

**작업 완료 시각**: 2026-02-16  
**총 작업 시간**: 약 2시간  
**작성자**: AI Assistant

---

## 🎉 결론

Spring Cloud Netflix 기반 마이크로서비스 인프라가 **Kotlin**으로 성공적으로 구축되었습니다!

- ✅ **Config Server**: 중앙 설정 관리
- ✅ **Eureka Server**: 서비스 디스커버리
- ✅ **API Gateway**: 단일 진입점 & 라우팅
- ✅ **기존 서버 연동**: Bootstrap 설정 완료

이제 다음 단계로 **Resilience4j, Distributed Tracing, Rate Limiting** 등을 추가하여 더욱 견고한 시스템을 만들 수 있습니다!
