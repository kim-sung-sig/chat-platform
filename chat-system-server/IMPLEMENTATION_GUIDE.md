# Chat System Server - 구현 완료 가이드

## 📋 구현 완료 사항

### ✅ Step 1: Domain Layer (도메인 엔티티)
- **BaseEntity**: 공통 엔티티 (생성일시, 수정일시 자동 관리)
- **Channel**: 채널 엔티티 (메시지 발행 권한 주체)
- **Customer**: 고객 엔티티 (메시지 수신자)
- **Message**: 메시지 엔티티 (발행 콘텐츠)
- **MessageHistory**: 메시지 발행 이력 (커서 기반 페이징 지원)
- **ScheduleRule**: 스케줄 규칙 (주기적/단발성 발행)
- **ChannelSubscription**: 채널 구독 (고객-채널 다대다 관계)

### ✅ Step 2: Domain Enums
- **MessageType**: TEXT, IMAGE, MIXED
- **MessageStatus**: DRAFT, SCHEDULED, PUBLISHED, CANCELLED
- **PublishStatus**: PENDING, SUCCESS, FAILED, RETRY
- **ScheduleType**: ONCE, RECURRING

### ✅ Step 3: Repository Layer (데이터 접근)
- **ChannelRepository**: 채널 CRUD 및 검색
- **CustomerRepository**: 고객 CRUD, 채널 구독자 조회
- **MessageRepository**: 메시지 CRUD, 발행 예정 메시지 조회
- **MessageHistoryRepository**: 발행 이력 조회 (커서 기반 페이징)
- **ScheduleRuleRepository**: 스케줄 CRUD, 실행 대상 스케줄 조회
- **ChannelSubscriptionRepository**: 구독 관리

### ✅ Step 4: DTO Layer (요청/응답)
**Request DTOs:**
- MessageCreateRequest
- MessageUpdateRequest
- ScheduleCreateRequest
- ChannelCreateRequest

**Response DTOs:**
- MessageResponse
- MessageHistoryResponse (커서 기반)
- ScheduleRuleResponse
- CursorPageResponse (커서 페이징 공통)
- ApiResponse (공통 API 응답)

### ✅ Step 5: Service Layer (비즈니스 로직)
- **MessageService**: 메시지 CRUD, 상태 관리
- **MessageHistoryService**: 발행 이력 조회/관리 (커서 기반 페이징)
- **ScheduleRuleService**: 스케줄 CRUD, 활성화/비활성화
- **MessagePublisherService**: 실제 메시지 발행 (Virtual Thread 기반)

### ✅ Step 6: Controller Layer (API 엔드포인트)
- **MessageController**: 메시지 관리 API
- **MessageHistoryController**: 발행 이력 조회 API (커서 기반)
- **ScheduleController**: 스케줄 관리 API

### ✅ Step 7: Infrastructure Layer
- **QuartzConfig**: Quartz 스케줄러 설정
- **JpaConfig**: JPA Auditing 활성화
- **SecurityConfig**: Spring Security 설정 (임시로 모든 요청 허용)
- **MessagePublishJob**: Quartz Job (메시지 발행 작업)
- **QuartzSchedulerService**: Quartz 스케줄러 관리

### ✅ Step 8: Exception Handling
- **GlobalExceptionHandler**: 전역 예외 처리
- **ResourceNotFoundException**: 리소스 미발견 예외
- **BusinessException**: 비즈니스 로직 예외
- **SchedulingException**: 스케줄링 예외

### ✅ Step 9: Database Schema
- **schema.sql**: 애플리케이션 테이블 DDL
- **schema-quartz.sql**: Quartz 스케줄러 테이블 DDL

---

## 🏗️ 아키텍처 설계

### 레이어드 아키텍처
```
┌─────────────────────────────────────┐
│      Controller Layer               │  ← REST API 엔드포인트
│  (MessageController, etc.)          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Service Layer                 │  ← 비즈니스 로직
│  (MessageService, etc.)             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Repository Layer               │  ← 데이터 접근
│  (MessageRepository, etc.)          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Domain Layer                  │  ← 도메인 모델
│  (Message, Channel, etc.)           │
└─────────────────────────────────────┘
```

### 책임과 역할 분리
- **Domain**: 비즈니스 규칙과 상태 관리 (예: Message.prepareForPublish())
- **Repository**: 데이터 접근 로직만 담당
- **Service**: 트랜잭션과 비즈니스 흐름 제어
- **Controller**: HTTP 요청/응답 처리

---

## 🚀 다음 단계 (TODO)

### 1. 데이터베이스 설정
```properties
# application.properties 수정
spring.datasource.url=jdbc:postgresql://localhost:5432/chat_system
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. 데이터베이스 초기화
```sql
-- PostgreSQL 데이터베이스 생성
CREATE DATABASE chat_system;

-- schema.sql 실행
\i src/main/resources/schema.sql

-- schema-quartz.sql 실행
\i src/main/resources/schema-quartz.sql
```

### 3. Quartz Scheduler 통합
현재 `QuartzSchedulerService`와 `ScheduleRuleService`를 연결해야 합니다.

**ScheduleRuleService.java 수정 필요:**
```java
@Transactional
public ScheduleRuleResponse createSchedule(ScheduleCreateRequest request) {
    // ... 기존 코드 ...
    
    // Quartz 스케줄러에 Job 등록
    if (request.getScheduleType() == ScheduleType.ONCE) {
        quartzSchedulerService.scheduleOnceJob(
            jobName, jobGroup, 
            message.getId(), 
            savedSchedule.getId(), 
            request.getExecutionTime()
        );
    } else {
        quartzSchedulerService.scheduleRecurringJob(
            jobName, jobGroup, 
            message.getId(), 
            savedSchedule.getId(), 
            request.getCronExpression()
        );
    }
    
    return ScheduleRuleResponse.from(savedSchedule);
}
```

### 4. MessagePublishJob 완성
```java
@Override
public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
        Long messageId = context.getJobDetail().getJobDataMap().getLong("messageId");
        Long scheduleRuleId = context.getJobDetail().getJobDataMap().getLong("scheduleRuleId");
        
        // Message 조회
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        
        // 메시지 발행
        messagePublisherService.publishMessage(message, scheduleRuleId);
        
        // 스케줄 실행 완료 처리
        scheduleRuleService.markAsExecuted(scheduleRuleId, calculateNextTime(/*...*/));
        
    } catch (Exception e) {
        log.error("Error executing MessagePublishJob", e);
        throw new JobExecutionException(e);
    }
}
```

### 5. 외부 메시징 시스템 연동
**MessagePublisherService.sendMessage() 구현:**
- Kafka Producer
- RabbitMQ
- 또는 외부 메시징 API 호출

### 6. Spring Batch 통합 (대량 발행)
```java
@Configuration
public class BatchConfig {
    @Bean
    public Job messagePublishJob() {
        return jobBuilderFactory.get("messagePublishJob")
            .start(messagePublishStep())
            .build();
    }
    
    @Bean
    public Step messagePublishStep() {
        return stepBuilderFactory.get("messagePublishStep")
            .<Customer, MessageHistory>chunk(100)
            .reader(customerReader())
            .processor(messageProcessor())
            .writer(messageWriter())
            .build();
    }
}
```

### 7. 스케줄 재시도 메커니즘
```java
@Scheduled(fixedDelay = 60000) // 1분마다
public void retryFailedMessages() {
    messagePublisherService.retryFailedMessages();
}
```

### 8. 모니터링 및 로깅
- 발행 성공률 모니터링
- 실패 메시지 알림
- Actuator 엔드포인트 추가

---

## 📊 API 엔드포인트

### 메시지 관리
- `POST /api/v1/messages` - 메시지 생성
- `GET /api/v1/messages/{messageId}` - 메시지 조회
- `GET /api/v1/messages/channel/{channelId}` - 채널별 메시지 목록
- `PUT /api/v1/messages/{messageId}` - 메시지 수정
- `POST /api/v1/messages/{messageId}/cancel` - 메시지 취소
- `DELETE /api/v1/messages/{messageId}` - 메시지 삭제

### 스케줄 관리
- `POST /api/v1/schedules` - 스케줄 생성
- `GET /api/v1/schedules/{scheduleId}` - 스케줄 조회
- `GET /api/v1/schedules/message/{messageId}` - 메시지별 스케줄 목록
- `GET /api/v1/schedules/active` - 활성 스케줄 목록
- `POST /api/v1/schedules/{scheduleId}/activate` - 스케줄 활성화
- `POST /api/v1/schedules/{scheduleId}/deactivate` - 스케줄 비활성화
- `DELETE /api/v1/schedules/{scheduleId}` - 스케줄 삭제

### 발행 이력 조회 (커서 기반 페이징)
- `GET /api/v1/message-histories/message/{messageId}?cursor={cursor}&size={size}` - 메시지별 이력
- `GET /api/v1/message-histories/customer/{customerId}?cursor={cursor}&size={size}` - 고객별 이력
- `GET /api/v1/message-histories/schedule/{scheduleRuleId}?cursor={cursor}&size={size}` - 스케줄별 이력

---

## 🔧 빌드 및 실행

### 빌드
```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### 실행
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 테스트
```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

---

## 📝 주요 특징

### 1. 커서 기반 페이징 (MessageHistory)
- Offset 페이징의 성능 문제 해결
- 대량 데이터에서도 일관된 성능
- `cursor` 파라미터로 다음 페이지 조회

### 2. Virtual Thread 활용 (Java 21)
- `Executors.newVirtualThreadPerTaskExecutor()` 사용
- 동시 메시지 발행 처리 성능 극대화
- 블로킹 I/O 작업에 효율적

### 3. 도메인 주도 설계
- 엔티티에 비즈니스 로직 배치
- 풍부한 도메인 모델 (Rich Domain Model)
- 명확한 상태 전이 관리

### 4. 트랜잭션 관리
- `@Transactional(readOnly = true)` 기본 설정
- 쓰기 작업에만 `@Transactional` 적용
- 최적의 성능과 안정성

---

## 🎯 핵심 비즈니스 플로우

### 1. 메시지 생성 및 스케줄 등록
```
1. 사용자가 메시지 작성 (DRAFT 상태)
2. 스케줄 규칙 생성 (ONCE 또는 RECURRING)
3. Quartz Scheduler에 Job 등록
4. 메시지 상태를 SCHEDULED로 변경
```

### 2. 메시지 자동 발행
```
1. Quartz Trigger 실행
2. MessagePublishJob 실행
3. 채널 구독자 목록 조회
4. Virtual Thread로 병렬 발행
5. MessageHistory 기록
6. 성공/실패 상태 업데이트
```

### 3. 발행 실패 재시도
```
1. 스케줄러가 재시도 대상 조회
2. retryCount < maxRetryCount 확인
3. 메시지 재발행 시도
4. 성공 시 SUCCESS, 실패 시 RETRY 또는 FAILED
```

---

## 🔐 보안 고려사항

현재 `SecurityConfig`는 모든 요청을 허용하도록 설정되어 있습니다.  
실제 운영 환경에서는 다음 사항을 구현해야 합니다:

1. JWT 기반 인증
2. 채널별 권한 관리
3. API Rate Limiting
4. CORS 설정

---

## 📚 참고 자료

- [Spring Boot 3.5.x Documentation](https://spring.io/projects/spring-boot)
- [Quartz Scheduler](https://www.quartz-scheduler.org/)
- [Spring Batch](https://spring.io/projects/spring-batch)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)

---

**구현 완료: 2025-10-26**
**작성자: 15년차 백엔드 개발자**