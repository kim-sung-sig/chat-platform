# 📋 Phase 4 실행 계획

**날짜**: 2025-12-07  
**목표**: 고급 기능 구현 및 시스템 안정화  
**상태**: 🚀 시작

---

## 🎯 Phase 4 목표

### 완료된 Phase (1-3)

- ✅ Phase 1: common 모듈 세분화
- ✅ Phase 2: chat-storage 도메인 모델
- ✅ Phase 3: 실행 모듈 재구성
- ✅ Phase 3.5: 전문가 리팩토링

### 이번 Phase (4)

1. **예약 메시지 시스템** (Quartz Scheduler)
2. **파일/이미지 메시지 타입** 구현
3. **읽음 처리 (Read Receipt)** 시스템
4. **메시지 검색** 기능

---

## 📋 STEP 2: 예약 메시지 시스템 (우선순위 높음)

### 배경

- 채팅 플랫폼에서 예약 메시지는 필수 기능
- 단발성 예약 + 주기적 예약 모두 지원
- Quartz Scheduler 사용

### 요구사항

#### 1. 단발성 예약 메시지

```
- 특정 시간에 1회만 발송
- 예: 2025-12-25 09:00:00에 "메리 크리스마스!" 발송
- 발송 후 자동 완료 처리
```

#### 2. 주기적 예약 메시지

```
- Cron 표현식으로 주기 설정
- 예: 매일 09:00, 매주 월요일 10:00
- 최대 실행 횟수 설정 가능
- 수동으로 중지 가능
```

#### 3. 동시 실행 방지

```
- 같은 예약이 중복 실행되지 않도록 보장
- 분산 락 또는 낙관적 락 사용
```

---

## 🏗️ 설계

### 1. Domain Model

#### ScheduleRule (Aggregate Root)

```java
@Entity
public class ScheduleRule {
    @Id
    private Long scheduleId;
    
    private String roomId;
    private Long senderId;
    
    @Enumerated(EnumType.STRING)
    private ScheduleType type; // ONE_TIME, RECURRING
    
    private LocalDateTime executeAt;      // 단발성
    private String cronExpression;        // 주기적
    
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;        // ACTIVE, PAUSED, COMPLETED, CANCELLED
    
    private Integer maxExecutionCount;    // 최대 실행 횟수 (null = 무제한)
    private Integer executionCount;       // 현재 실행 횟수
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 도메인 로직
    public void execute() { }
    public void pause() { }
    public void resume() { }
    public void cancel() { }
}
```

#### ScheduleType

```java
public enum ScheduleType {
    ONE_TIME("one_time", "단발성"),
    RECURRING("recurring", "주기적");
}
```

#### ScheduleStatus

```java
public enum ScheduleStatus {
    ACTIVE("active", "활성"),
    PAUSED("paused", "일시중지"),
    COMPLETED("completed", "완료"),
    CANCELLED("cancelled", "취소됨");
}
```

### 2. Quartz Job

#### MessagePublishJob

```java
@Component
public class MessagePublishJob implements Job {
    
    @Override
    public void execute(JobExecutionContext context) {
        Long scheduleId = context.getJobDetail()
            .getJobDataMap()
            .getLong("scheduleId");
        
        // Step 1: ScheduleRule 조회 및 락 획득
        ScheduleRule rule = acquireLockAndFindRule(scheduleId);
        
        if (rule == null) {
            return; // 이미 다른 인스턴스가 처리 중
        }
        
        try {
            // Step 2: 메시지 발송
            publishMessage(rule);
            
            // Step 3: 실행 횟수 증가
            rule.incrementExecutionCount();
            
            // Step 4: 완료 조건 체크
            if (rule.shouldComplete()) {
                rule.complete();
            }
            
        } finally {
            // Step 5: 락 해제
            releaseLock(scheduleId);
        }
    }
}
```

### 3. Service Layer

#### ScheduleService

```java
@Service
public class ScheduleService {
    
    private final ScheduleRuleRepository repository;
    private final Scheduler quartzScheduler;
    
    /**
     * 단발성 예약 메시지 등록
     */
    public ScheduleRule createOneTimeSchedule(
        String roomId,
        Long senderId,
        MessageType messageType,
        Map<String, Object> payload,
        LocalDateTime executeAt
    ) {
        // Step 1: ScheduleRule 생성
        ScheduleRule rule = ScheduleRule.createOneTime(
            roomId, senderId, messageType, payload, executeAt
        );
        
        // Step 2: DB 저장
        rule = repository.save(rule);
        
        // Step 3: Quartz Job 등록
        registerQuartzJob(rule);
        
        return rule;
    }
    
    /**
     * 주기적 예약 메시지 등록
     */
    public ScheduleRule createRecurringSchedule(
        String roomId,
        Long senderId,
        MessageType messageType,
        Map<String, Object> payload,
        String cronExpression,
        Integer maxExecutionCount
    ) {
        // 유사한 로직
    }
    
    private void registerQuartzJob(ScheduleRule rule) {
        JobDetail jobDetail = JobBuilder.newJob(MessagePublishJob.class)
            .withIdentity("schedule-" + rule.getScheduleId())
            .usingJobData("scheduleId", rule.getScheduleId())
            .build();
        
        Trigger trigger;
        if (rule.getType() == ScheduleType.ONE_TIME) {
            trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(rule.getExecuteAt()
                    .atZone(ZoneId.systemDefault()).toInstant()))
                .build();
        } else {
            trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder
                    .cronSchedule(rule.getCronExpression()))
                .build();
        }
        
        quartzScheduler.scheduleJob(jobDetail, trigger);
    }
}
```

---

## 🔒 동시 실행 방지 전략

### 방안 1: 낙관적 락 (Optimistic Locking)

```java
@Entity
public class ScheduleRule {
    @Version
    private Long version;
    
    public void execute() {
        // 실행 전 version 체크
        // 다른 인스턴스가 먼저 업데이트하면 예외 발생
    }
}
```

### 방안 2: Redis 분산 락

```java
public class DistributedLockService {
    
    public boolean tryLock(String key, Duration timeout) {
        return redisTemplate.opsForValue()
            .setIfAbsent(key, "locked", timeout);
    }
    
    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}

// 사용
if (lockService.tryLock("schedule:" + scheduleId, Duration.ofMinutes(5))) {
    try {
        // 작업 수행
    } finally {
        lockService.unlock("schedule:" + scheduleId);
    }
}
```

### 방안 3: Quartz의 JobStore 활용

```properties
# application.yml
spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: always
    properties:
      org.quartz.jobStore.isClustered: true
      org.quartz.jobStore.clusterCheckinInterval: 20000
```

**권장**: 방안 2 (Redis 분산 락) + 방안 3 (Quartz Cluster)

---

## 📋 구현 체크리스트

### Domain Layer

- [ ] `ScheduleRule` Entity
- [ ] `ScheduleType` Enum
- [ ] `ScheduleStatus` Enum
- [ ] `ScheduleRuleRepository`

### Application Layer

- [ ] `ScheduleService`
- [ ] `ScheduleQueryService`

### Infrastructure Layer

- [ ] `MessagePublishJob` (Quartz Job)
- [ ] `DistributedLockService` (Redis)
- [ ] Quartz 설정 (`QuartzConfig`)

### API Layer

- [ ] `ScheduleController`
	- `POST /api/schedules/one-time`
	- `POST /api/schedules/recurring`
	- `PUT /api/schedules/{id}/pause`
	- `PUT /api/schedules/{id}/resume`
	- `DELETE /api/schedules/{id}`
	- `GET /api/schedules`

---

## 🧪 테스트 계획

### 단위 테스트

```java
@Test
void 단발성_예약_메시지_생성() {
    ScheduleRule rule = ScheduleRule.createOneTime(
        "room-123", 100L, MessageType.TEXT, 
        Map.of("text", "Test"), 
        LocalDateTime.now().plusHours(1)
    );
    
    assertThat(rule.getType()).isEqualTo(ScheduleType.ONE_TIME);
    assertThat(rule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
}
```

### 통합 테스트

```java
@SpringBootTest
class ScheduleServiceIntegrationTest {
    
    @Test
    void 예약_메시지_발송_테스트() {
        // 1분 후 발송 예약
        ScheduleRule rule = scheduleService.createOneTimeSchedule(
            "room-123", 100L, MessageType.TEXT,
            Map.of("text", "Test"),
            LocalDateTime.now().plusMinutes(1)
        );
        
        // 2분 대기
        Thread.sleep(120000);
        
        // 메시지 발송 확인
        List<Message> messages = messageRepository
            .findByRoomId("room-123");
        
        assertThat(messages).isNotEmpty();
    }
}
```

---

## ⏱️ 예상 소요 시간

- Domain Model: 2시간
- Service Layer: 4시간
- Quartz 통합: 3시간
- 분산 락: 2시간
- API Layer: 2시간
- 테스트: 3시간

**총**: 약 16시간 (2일)

---

## 📚 참고 자료

- Quartz Scheduler 공식 문서
- Spring Boot Quartz 통합 가이드
- Redis 분산 락 패턴

---

**작성일**: 2025-12-07  
**다음 작업**: Domain Model 구현 시작

**🚀 Phase 4 시작!**
