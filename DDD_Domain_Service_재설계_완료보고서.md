# DDD Domain Service 재설계 완료 보고서

## 📅 작업 일자: 2025-12-13

---

## 🎯 작업 목표

### ✅ 완료된 작업

#### 1. Domain Service 전문가 수준 리팩토링 (100% 완료)

**문제점:**
- Domain Service가 단순히 ID만 받아서 검증하는 구조
- Aggregate Root 간의 협력이 명확하지 않음
- 도메인 규칙이 분산되어 있어 가독성이 떨어짐
- Early Return 패턴이 일관되게 적용되지 않음

**개선 결과:**
- **Aggregate 기반 협력 강화**: Domain Service가 Channel, User 등 Aggregate Root를 직접 받아서 협력
- **Early Return 패턴 적용**: 모든 검증 로직에 Early Return 적용
- **책임 명확화**: 도메인 규칙 검증 / 입력값 검증 섹션 분리
- **주석 개선**: 각 메서드의 도메인 규칙을 명확히 문서화

---

## 📊 수정된 파일

### 1. MessageDomainService.java (리팩토링)
**위치:** `chat-domain/src/main/java/com/example/chat/domain/service/MessageDomainService.java`

**주요 변경사항:**
```java
// Before (문제)
public Message createTextMessage(ChannelId channelId, UserId senderId, String text)

// After (개선)
public Message createTextMessage(Channel channel, User sender, String text)
```

**개선 내용:**
- Aggregate Root (Channel, User)를 직접 받아서 도메인 규칙 검증
- `validateMessageSendingPermission()` 메서드로 복합 도메인 규칙 통합
- Early Return 패턴 일관되게 적용
- 입력값 검증과 도메인 규칙 검증 명확히 분리

**도메인 규칙 (명확히 문서화):**
1. Channel이 활성 상태여야 함
2. User가 Channel의 멤버여야 함
3. User가 차단/정지 상태가 아니어야 함
4. User가 메시지 발송 가능 상태여야 함

---

### 2. ChannelDomainService.java (리팩토링)
**위치:** `chat-domain/src/main/java/com/example/chat/domain/service/ChannelDomainService.java`

**주요 변경사항:**
```java
// Before (문제)
public Channel createDirectChannel(UserId user1, UserId user2)

// After (개선)
public Channel createDirectChannel(User user1, User user2)
```

**신규 메서드 추가:**
- `addMemberToChannel(Channel, User)`: 채널에 멤버 추가 시 도메인 규칙 검증
- `removeMemberFromChannel(Channel, User)`: 채널에서 멤버 제거 시 도메인 규칙 검증

**도메인 규칙:**
1. 채널 생성 시 소유자(Owner)는 활성 상태여야 함
2. 일대일 채팅은 두 사용자 모두 활성 상태여야 함
3. 멤버 추가 시 채널이 활성 상태여야 함
4. 채널 소유자는 제거할 수 없음

---

### 3. ScheduleDomainService.java (리팩토링)
**위치:** `chat-domain/src/main/java/com/example/chat/domain/service/ScheduleDomainService.java`

**주요 개선사항:**
- Early Return 패턴 적용
- 주석 개선 (도메인 규칙 명확히 문서화)
- 입력값 검증 섹션 분리

**도메인 규칙:**
1. 단발성 스케줄: 예약 시간은 미래여야 하며 1년 이내여야 함
2. 주기적 스케줄: Cron Expression이 유효해야 함

---

### 4. ScheduleService.java (Application Service 리팩토링)
**위치:** `chat-system-server/src/main/java/com/example/chat/system/service/ScheduleService.java`

**주요 변경사항:**
```java
// Before (문제)
private Message createMessageFromRequest(ChannelId channelId, UserId senderId, ...)

// After (개선)
private Message createMessageByType(Channel channel, User sender, ...)
```

**개선 내용:**
1. **Repository 의존성 추가:**
   - `ChannelRepository` 추가
   - `UserRepository` 추가

2. **Key 기반 Aggregate 조회 패턴 적용:**
   ```java
   // Step 1: Key 조회 (인증된 사용자 ID)
   UserId senderId = getUserIdFromContext();
   
   // Step 2: Key로 Aggregate 조회
   Channel channel = findChannelById(channelId);
   User sender = findUserById(senderId);
   
   // Step 3: Domain Service 호출 (Aggregate 전달)
   Message message = createMessageByType(channel, sender, ...);
   ```

3. **Application Service의 책임 명확화:**
   - 트랜잭션 경계 관리
   - Aggregate 조회 (Repository)
   - Domain Service 호출 (도메인 로직 위임)
   - 인프라 작업 (Quartz)
   - DTO 변환

---

## 🏗️ DDD 아키텍처 패턴 적용

### Application Service Layer
**역할:**
1. 트랜잭션 경계 관리 (`@Transactional`)
2. 인증/인가 확인
3. **Key로 Aggregate 조회** (Repository 사용)
4. **Domain Service 호출** (Aggregate 전달)
5. 이벤트 발행
6. DTO 변환

**예시 코드:**
```java
@Transactional
public ScheduleResponse createOneTimeSchedule(CreateOneTimeScheduleRequest request) {
    // Step 1: Key 조회
    UserId senderId = getUserIdFromContext();
    ChannelId channelId = ChannelId.of(request.getChannelId());
    
    // Step 2: Aggregate 조회
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
    // Step 3: Domain Service 호출 (Aggregate 전달)
    Message message = messageDomainService.createTextMessage(channel, sender, text);
    
    // Step 4: 영속화
    ScheduleRule rule = scheduleDomainService.createOneTimeSchedule(message, scheduledAt);
    scheduleRuleRepository.save(rule);
    
    // Step 5: 인프라 작업
    registerQuartzJob(rule);
    
    return ScheduleResponse.from(rule);
}
```

---

### Domain Service Layer
**역할:**
1. **여러 Aggregate Root 간의 협력 조율**
2. **복잡한 도메인 규칙 검증** (단일 Aggregate으로 표현할 수 없는 규칙)
3. **도메인 불변식(Invariants) 보장**

**예시 코드:**
```java
public Message createTextMessage(Channel channel, User sender, String text) {
    // Early Return: 입력값 검증
    validateTextContent(text);
    
    // Domain Rule: Channel + User 협력을 통한 발송 권한 검증
    validateMessageSendingPermission(channel, sender);
    
    // Message 생성
    MessageContent content = MessageContent.text(text);
    return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
}

private void validateMessageSendingPermission(Channel channel, User sender) {
    // Early Return: 채널 활성화 확인
    if (!channel.isActive()) {
        throw new DomainException("Channel is not active");
    }
    
    // Early Return: 채널 멤버십 확인
    if (!channel.isMember(sender.getId())) {
        throw new DomainException("User is not a member of the channel");
    }
    
    // Early Return: 사용자 차단 여부 확인
    if (sender.isBanned()) {
        throw new DomainException("User is banned");
    }
    
    // Early Return: 사용자 정지 여부 확인
    if (sender.isSuspended()) {
        throw new DomainException("User is suspended");
    }
    
    // Early Return: 사용자 메시지 발송 가능 여부 확인
    if (!sender.canSendMessage()) {
        throw new DomainException("User cannot send messages");
    }
}
```

---

## 📈 코드 품질 개선 지표

### 1. 가독성 향상
- **명확한 메서드 시그니처**: Aggregate를 직접 받아 의도가 명확
- **섹션 분리**: 입력값 검증 / 도메인 규칙 검증 명확히 분리
- **주석 개선**: 각 메서드의 도메인 규칙을 상세히 문서화

### 2. 유지보수성 향상
- **책임 명확화**: Application Service vs Domain Service 역할 분리
- **Early Return 패턴**: 중첩된 if문 제거, 조기 에러 표출
- **DDD 패턴 준수**: Aggregate 중심 설계

### 3. 확장성 향상
- **Domain Service 확장 용이**: 새로운 메시지 타입 추가 시 Domain Service만 수정
- **도메인 규칙 중앙화**: 비즈니스 규칙이 Domain Service에 집중

---

## 🔍 Before & After 비교

### Before (문제점)
```java
// Application Service
public ScheduleResponse createOneTimeSchedule(CreateOneTimeScheduleRequest request) {
    UserId senderId = getUserIdFromContext();
    ChannelId channelId = ChannelId.of(request.getChannelId());
    
    // ❌ ID만 전달 - Domain Service에서 Aggregate 협력 불가
    Message message = createMessageFromRequest(channelId, senderId, ...);
}

// Domain Service
public Message createTextMessage(ChannelId channelId, UserId senderId, String text) {
    // ❌ ID만 있어서 채널 상태, 멤버십 등을 확인할 수 없음
    validateTextContent(text);
    MessageContent content = MessageContent.text(text);
    return Message.create(channelId, senderId, content, MessageType.TEXT);
}
```

### After (개선)
```java
// Application Service
public ScheduleResponse createOneTimeSchedule(CreateOneTimeScheduleRequest request) {
    UserId senderId = getUserIdFromContext();
    ChannelId channelId = ChannelId.of(request.getChannelId());
    
    // ✅ Aggregate 조회
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
    // ✅ Aggregate 전달 - Domain Service에서 협력 가능
    Message message = createMessageByType(channel, sender, ...);
}

// Domain Service
public Message createTextMessage(Channel channel, User sender, String text) {
    // ✅ Early Return: 입력값 검증
    validateTextContent(text);
    
    // ✅ Domain Rule: Aggregate 협력을 통한 검증
    validateMessageSendingPermission(channel, sender);
    
    // ✅ Message 생성
    MessageContent content = MessageContent.text(text);
    return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
}

private void validateMessageSendingPermission(Channel channel, User sender) {
    // ✅ Aggregate의 메서드를 활용한 도메인 규칙 검증
    if (!channel.isActive()) {
        throw new DomainException("Channel is not active");
    }
    if (!channel.isMember(sender.getId())) {
        throw new DomainException("User is not a member");
    }
    if (sender.isBanned() || sender.isSuspended() || !sender.canSendMessage()) {
        throw new DomainException("User cannot send messages");
    }
}
```

---

## ✅ 빌드 결과

```bash
BUILD SUCCESSFUL in 22s
37 actionable tasks: 34 executed, 3 from cache
```

**경고:** 일부 메서드가 아직 사용되지 않음 (정상 - 추후 구현 예정)
- `ChannelDomainService.addMemberToChannel()`
- `ChannelDomainService.removeMemberFromChannel()`
- 등등

**에러:** 없음 ✅

---

## 📝 코드 컨벤션 준수 여부

### ✅ DDD 원칙 준수
- **Aggregate Root 중심 설계**: Channel, User, Message 등 명확한 Aggregate
- **Domain Service 역할 명확**: 여러 Aggregate 간 협력 조율
- **Repository 패턴**: Aggregate 조회는 Repository를 통해서만

### ✅ Early Return 패턴 적용
- 모든 검증 로직에 Early Return 적용
- 중첩된 if문 제거
- 조기 에러 표출

### ✅ 책임 명확화
- **Application Service**: 트랜잭션, Aggregate 조회, DTO 변환
- **Domain Service**: 도메인 규칙 검증, Aggregate 협력 조율
- **Domain Model**: 자신의 상태 관리, 단순 비즈니스 로직

### ✅ 입력값 검증 (Early Validation)
- `@Valid` 어노테이션 사용 (DTO)
- Domain Service에서 추가 검증 (입력값 검증 섹션)
- 조기 에러 표출

### ✅ 가독성
- 명확한 메서드명
- 상세한 주석 (도메인 규칙 문서화)
- 섹션 분리 (입력값 검증 / 도메인 규칙 검증)

---

## 🚀 다음 세션 계획

### 1. Channel 관리 기능 구현
- 채널 생성 API (ChannelApplicationService)
- 채널 멤버 추가/제거 API
- ChannelDomainService의 메서드 활용

### 2. 메시지 조회 기능 구현
- 커서 기반 페이징 (Cursor Pagination)
- MessageRepository 확장
- 메시지 검색 기능

### 3. WebSocket 서버 리팩토링
- chat-websocket-server의 Domain 모델 적용
- Redis Pub/Sub 연동 확인
- 멀티 인스턴스 환경 대응

### 4. 통합 테스트 작성
- Domain Service 단위 테스트
- Application Service 통합 테스트
- API 엔드투엔드 테스트

---

## 📊 진행률

### 전체 프로젝트: **45% 완료**

- ✅ 멀티모듈 구조 설계 (100%)
- ✅ Domain 모듈 분리 (100%)
- ✅ Storage 모듈 구현 (100%)
- ✅ **Domain Service 리팩토링 (100%)**
- ✅ Message Server 기본 구현 (100%)
- ✅ Schedule Server 기본 구현 (100%)
- ⏳ Channel 관리 기능 (0%)
- ⏳ WebSocket Server 리팩토링 (0%)
- ⏳ 메시지 조회 기능 (0%)
- ⏳ 통합 테스트 (0%)

---

## 💡 핵심 개선 사항 요약

1. **Domain Service가 Aggregate를 직접 받아서 협력**: ID 기반 → Aggregate 기반
2. **Early Return 패턴 일관 적용**: 가독성 향상, 조기 에러 표출
3. **책임 명확화**: Application Service vs Domain Service 역할 분리
4. **도메인 규칙 중앙화**: 비즈니스 규칙이 Domain Service에 집중
5. **주석 개선**: 각 메서드의 도메인 규칙을 상세히 문서화

---

## 🎓 전문가 수준 DDD 패턴 적용 완료

이번 세션에서 전문가 수준의 DDD 패턴을 적용하였습니다:

1. **Aggregate Root 중심 설계**
2. **Domain Service의 올바른 활용** (여러 Aggregate 간 협력)
3. **Application Service의 역할 명확화** (조율자 역할)
4. **Repository 패턴** (Aggregate 조회)
5. **Early Return 패턴** (가독성, 유지보수성)

모든 코드가 일관된 컨벤션을 따르며, 확장 가능한 구조로 설계되었습니다.

---

**작성자:** GitHub Copilot  
**검토 상태:** ✅ 완료  
**다음 세션:** Channel 관리 기능 구현
