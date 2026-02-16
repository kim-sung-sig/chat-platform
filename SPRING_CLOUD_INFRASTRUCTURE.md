# Spring Cloud 인프라 구축 가이드

> **작업 날짜**: 2026-02-16  
> **목표**: Spring Cloud Netflix 기반 마이크로서비스 인프라 구축 (Kotlin)

---

## 📋 목차

1. [개요](#개요)
2. [아키텍처](#아키텍처)
3. [구성 요소](#구성-요소)
4. [실행 순서](#실행-순서)
5. [엔드포인트](#엔드포인트)
6. [다음 단계](#다음-단계)

---

## 개요

### 구축 완료된 인프라

```
infrastructure/
├── config-server/       # Spring Cloud Config Server (Port: 8888)
├── eureka-server/       # Service Discovery (Port: 8761)
└── api-gateway/         # API Gateway (Port: 8000)
```

### 기술 스택

- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2024.0.0
- **언어**: Kotlin 1.9.25
- **JDK**: Temurin 21

---

## 아키텍처

### 전체 구조

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│      API Gateway (Port: 8000)       │
│  - Routing                          │
│  - Load Balancing                   │
│  - CORS                             │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│   Eureka Server (Port: 8761)        │
│  - Service Registry                 │
│  - Service Discovery                │
└──────┬──────────────────────────────┘
       │
       ├──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Message      │ │ System       │ │ WebSocket    │
│ Server       │ │ Server       │ │ Server       │
│ (Port: 8081) │ │ (Port: 8082) │ │ (Port: 20002)│
└──────────────┘ └──────────────┘ └──────────────┘
       │              │              │
       └──────────────┴──────────────┘
                      │
                      ▼
          ┌────────────────────────┐
          │ Config Server          │
          │ (Port: 8888)           │
          │ - Centralized Config   │
          └────────────────────────┘
```

---

## 구성 요소

### 1. Config Server (Port: 8888)

#### 역할

- 모든 마이크로서비스의 설정을 중앙에서 관리
- Native 프로필 사용 (로컬 파일 시스템 기반)
- 프로덕션 환경에서는 Git Repository 연동 가능

#### 설정 파일 위치

```
infrastructure/config-server/src/main/resources/config-repo/
├── application.yml                   # 공통 설정
├── chat-message-server.yml           # Message Server 전용 설정
├── chat-system-server.yml            # System Server 전용 설정
└── chat-websocket-server.yml         # WebSocket Server 전용 설정
```

#### 주요 설정 내용

**application.yml** (공통 설정)

```yaml
# Database 공통 설정
db:
  source:
    driver-class-name: org.postgresql.Driver
  replica:
    driver-class-name: org.postgresql.Driver

# Redis 공통 설정
redis:
  host: localhost
  port: 16379
  password: dev_password

# Logging 공통 설정
logging:
  level:
    root: INFO
    com.example.chat: DEBUG
```

**chat-message-server.yml**

```yaml
server:
  port: 8081

spring:
  application:
    name: chat-message-server

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

db:
  source:
    jdbc-url: jdbc:postgresql://localhost:15432/chat_db
    username: chat_user
    password: chat_password
```

#### 실행 방법

```bash
cd C:\git\chat-platform
$env:JAVA_HOME="C:\Users\kimsungsig\.jdks\temurin-21.0.7"
.\gradlew :infrastructure:config-server:bootRun
```

#### 확인

```bash
# Config Server Health Check
curl http://localhost:8888/actuator/health

# Message Server 설정 조회
curl http://localhost:8888/chat-message-server/default
```

---

### 2. Eureka Server (Port: 8761)

#### 역할

- 서비스 레지스트리: 모든 마이크로서비스 등록
- 서비스 디스커버리: 서비스 간 통신 지원
- Health Check: 서비스 상태 모니터링

#### 주요 설정

```yaml
eureka:
  client:
    register-with-eureka: false  # 자신은 등록하지 않음
    fetch-registry: false
  server:
    enable-self-preservation: false  # 개발 환경에서 비활성화
    eviction-interval-timer-in-ms: 5000
```

#### 실행 방법

```bash
.\gradlew :infrastructure:eureka-server:bootRun
```

#### 대시보드

- URL: http://localhost:8761
- 등록된 모든 서비스 목록 확인 가능

---

### 3. API Gateway (Port: 8000)

#### 역할

- 모든 마이크로서비스의 **단일 진입점**
- 라우팅 & 로드 밸런싱
- CORS 설정
- 글로벌 필터 (로깅, 인증 등)

#### 라우팅 규칙

| 경로 패턴               | 대상 서비스                            | 실제 포트 |
|---------------------|-----------------------------------|-------|
| `/api/messages/**`  | chat-message-server               | 8081  |
| `/api/channels/**`  | chat-system-server                | 8082  |
| `/api/schedules/**` | chat-system-server                | 8082  |
| `/ws/**`            | chat-websocket-server (WebSocket) | 20002 |

#### CORS 설정

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins:
        - "http://localhost:3000"
        - "http://localhost:8080"
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
```

#### 글로벌 필터

**LoggingFilter.kt**

```kotlin
@Component
class LoggingFilter : GlobalFilter, Ordered {
	override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
		// 모든 요청/응답 로깅
		logger.info("[Gateway Request] {} {}", request.method, request.uri.path)
		return chain.filter(exchange).then(
			Mono.fromRunnable {
				logger.info("[Gateway Response] - Status: {}", exchange.response.statusCode)
			}
		)
	}
}
```

#### 실행 방법

```bash
.\gradlew :infrastructure:api-gateway:bootRun
```

#### 테스트

```bash
# Gateway를 통한 Message Server 호출
curl http://localhost:8000/api/messages/health

# Gateway를 통한 System Server 호출
curl http://localhost:8000/api/channels
```

---

## 실행 순서

### 1단계: 인프라 서버 시작 (순서 중요!)

```bash
# Java Home 설정
$env:JAVA_HOME="C:\Users\kimsungsig\.jdks\temurin-21.0.7"

# 1. Config Server 시작 (가장 먼저!)
.\gradlew :infrastructure:config-server:bootRun

# 2. Eureka Server 시작 (2번째)
.\gradlew :infrastructure:eureka-server:bootRun

# 3. API Gateway 시작 (3번째)
.\gradlew :infrastructure:api-gateway:bootRun
```

### 2단계: 애플리케이션 서버 시작

```bash
# Message Server
.\gradlew :apps:chat:message-server:bootRun

# System Server
.\gradlew :apps:chat:system-server:bootRun

# WebSocket Server
.\gradlew :apps:chat:websocket-server:bootRun
```

### 3단계: 확인

1. **Eureka Dashboard**: http://localhost:8761
	- 모든 서비스가 등록되었는지 확인

2. **Config Server**: http://localhost:8888/actuator/health

3. **Gateway Health**: http://localhost:8000/actuator/health

4. **Gateway Routes**: http://localhost:8000/actuator/gateway/routes

---

## 엔드포인트

### Config Server (8888)

| 엔드포인트                              | 설명                |
|------------------------------------|-------------------|
| `GET /actuator/health`             | Health Check      |
| `GET /{application}/{profile}`     | 설정 조회             |
| `GET /chat-message-server/default` | Message Server 설정 |
| `GET /chat-system-server/default`  | System Server 설정  |

### Eureka Server (8761)

| 엔드포인트                  | 설명                    |
|------------------------|-----------------------|
| `GET /`                | Eureka Dashboard (UI) |
| `GET /eureka/apps`     | 등록된 모든 앱 목록 (XML)     |
| `GET /actuator/health` | Health Check          |

### API Gateway (8000)

| 엔드포인트                          | 설명                 |
|--------------------------------|--------------------|
| `GET /actuator/health`         | Health Check       |
| `GET /actuator/gateway/routes` | 라우팅 규칙 목록          |
| `GET /api/messages/**`         | → Message Server   |
| `GET /api/channels/**`         | → System Server    |
| `WS /ws/**`                    | → WebSocket Server |

---

## 클라이언트 서버 연동

### Bootstrap 설정

각 애플리케이션 서버는 `bootstrap.yml`로 Config Server에 연결:

```yaml
spring:
  application:
    name: chat-message-server
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
```

### Eureka Client 설정

Config Server에서 제공하는 설정:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}
```

---

## 다음 단계

### Phase 1: 현재 완료 ✅

- [x] Config Server 구축
- [x] Eureka Server 구축
- [x] API Gateway 구축
- [x] 애플리케이션 서버 연동 (build.gradle.kts 의존성 추가)
- [x] Bootstrap 설정 파일 생성

### Phase 2: 추가 작업 (진행 필요)

- [ ] **Auth Server 통합**: JWT 인증 필터 Gateway에 추가
- [ ] **Rate Limiting**: Gateway에 요청 제한 필터 추가
- [ ] **Circuit Breaker**: Resilience4j 적용
- [ ] **Distributed Tracing**: Spring Cloud Sleuth + Zipkin
- [ ] **Config Encryption**: 민감한 설정 암호화
- [ ] **Docker Compose**: 전체 인프라 컨테이너화

### Phase 3: Kubernetes 마이그레이션 (3개월 후)

- [ ] Dockerfile 작성
- [ ] K8s Deployment/Service YAML
- [ ] ConfigMap/Secret으로 전환
- [ ] Ingress 설정
- [ ] Helm Chart 작성

---

## 트러블슈팅

### 문제 1: Config Server에서 설정을 가져오지 못함

**증상**:

```
Could not locate PropertySource: 404 from config server
```

**해결**:

1. Config Server가 먼저 실행되었는지 확인
2. `bootstrap.yml`의 `spring.cloud.config.uri` 확인
3. Config Server 로그 확인

### 문제 2: Eureka에 서비스가 등록되지 않음

**해결**:

1. Eureka Server가 실행 중인지 확인
2. `eureka.client.service-url.defaultZone` 설정 확인
3. 네트워크 방화벽 확인

### 문제 3: Gateway 라우팅 실패

**해결**:

```bash
# 라우팅 규칙 확인
curl http://localhost:8000/actuator/gateway/routes

# 특정 서비스가 Eureka에 등록되었는지 확인
curl http://localhost:8761/eureka/apps
```

---

## 참고 자료

- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Cloud 2024.0.0 Release Notes](https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2024.0-Release-Notes)

---

**작성자**: AI Assistant  
**최종 수정일**: 2026-02-16
