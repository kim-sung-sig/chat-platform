# 📘 Project Overview: Message Scheduler & Publisher Service

---

## 1️⃣ 서비스 개요

이 시스템은 **메시지 발행(Messaging Publish)** 과 **스케줄 관리(Scheduler Management)** 를 핵심으로 하는 **메시지 발행 API 서버**입니다.  
채널 담당자가 고객에게 메시지를 효율적으로 발행하고, **주기적 또는 단발성 발행을 자동화**하며, 발행 **이력과 상태를 관리**할 수 있도록 설계되었습니다.

---

## 2️⃣ 주요 목표

| 구분 | 설명 |
|------|------|
| 메시지 발행 자동화 | Quartz 및 Spring Batch 기반으로 **정기 발행, 단발 발행** 모두 지원 |
| 관리 기능 강화 | 발행 이력, 실패 로그, 상태 추적을 통한 **운영 가시성 확보** |
| 안정성과 확장성 | Java 21 Virtual Thread와 Spring 3.5.6의 비동기 처리 기반으로 **동시 처리 성능 극대화** |
| 사용 편의성 | API 기반 발행 요청 및 관리 UI 연동이 용이하도록 **RESTful 설계** |
| 도메인 명확화 | “채널 → 메시지 → 수신자 → 발행 이력”의 **명확한 관계 구조 제공** |

---

## 3️⃣ 기술 스펙

| 항목 | 기술 / 버전 | 설명 |
|------|--------------|------|
| Language | Java 21 | Virtual Thread 및 Record 지원 |
| Framework | Spring Boot 3.5.6 | Web, Scheduler, JPA, Batch, Quartz |
| ORM | Spring Data JPA (Hibernate 6.x) | 메시지 및 스케줄 데이터 영속화 |
| Database | PostgreSQL 16 | 메시지, 발행 이력, 스케줄 데이터 저장 |
| Scheduler | Quartz | 주기적 메시지 발행 스케줄링 |
| Batch Framework | Spring Batch | 대량 메시지 발행 및 이력 처리 |
| Build Tool | Gradle 8.x | 모듈 단위 빌드 및 CI/CD 용이 |
| REST Framework | Spring WebFlux (optional) | 높은 동시성 요청 처리 |
| Logging | Logback + JSON Logging | 구조적 로깅, 발행 상태 추적 |

---

## 4️⃣ 시스템 주요 구성도
┌────────────────────────────┐<br>
│ Message API Server │<br>
│────────────────────────────│<br>
│ Controller Layer │<br>
│ ├── MessagePublishApi │<br>
│ ├── MessageHistoryApi │<br>
│ │<br>
│ Service Layer │<br>
│ ├── MessageSchedulerService│<br>
│ ├── MessagePublisherService│<br>
│ ├── MessageHistoryService │<br>
│ │<br>
│ Infrastructure Layer │<br>
│ ├── QuartzSchedulerConfig │<br>
│ ├── BatchJobConfig │<br>
│ ├── Repository(JPA) │<br>
│ │<br>
│ Domain Layer │<br>
│ ├── Channel │<br>
│ ├── Message │<br>
│ ├── Customer │<br>
│ ├── MessageHistory │<br>
│ ├── ScheduleRule │<br>
└────────────────────────────┘<br>


## 5️⃣ 주요 도메인 개념

| 도메인 | 설명 |
|---------|------|
| Channel | 메시지를 발행할 권한을 가진 주체 (예: 마케팅, 공지, 이벤트 등) |
| Message | 채널 담당자가 작성한 발행 콘텐츠 |
| Customer | 채널 메시지 수신에 동의한 고객 |
| MessageHistory | 실제 발행된 메시지 및 발행 상태 (성공, 실패, 재시도 등) |
| ScheduleRule | 주기적/단발성 메시지 발행 규칙 (Cron, Delay 등) |

---

## 6️⃣ 주요 기능 흐름

### Step 1. 메시지 등록 및 스케줄 생성
- 채널 담당자가 메시지 작성 후 발행 유형 선택 (주기 / 1회)
- `SchedulerRule` 엔티티 생성
- Quartz Job 등록 → Batch Job 연결

### Step 2. 메시지 발행 처리 (Quartz → Batch)
- Quartz가 지정된 스케줄에 따라 Trigger 실행
- `MessageBatchJob` 실행하여 발행 대상 고객 목록 조회
- **Virtual Thread 기반 병렬 발행 처리**
- 발행 성공/실패 여부를 `MessageHistory`에 기록

### Step 3. 발행 이력 조회
- REST API를 통해 특정 채널/기간별 메시지 발행 결과 조회 가능
- `MessageHistoryRepository`를 통해 상태 기반 필터링 지원

---

## 7️⃣ 스케줄 및 배치 구상도

```mermaid
flowchart TD
    A[Message 등록] --> B[ScheduleRule 저장]
    B --> C[Quartz Scheduler 등록]
    C -->|Trigger| D[Spring Batch Job 실행]
    D --> E[고객 대상 메시지 조회]
    E --> F[비동기 병렬 발행 (Virtual Thread Pool)]
    F --> G[발행 성공/실패 기록]
    G --> H[MessageHistory 저장]
    H --> I[이력 조회 API]