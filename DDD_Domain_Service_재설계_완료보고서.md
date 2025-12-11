# DDD Domain Service 재설계 완료 보고서

## 📅 작업 일자: 2025-12-11

---

## 🎯 작업 목표

**잘못 설계된 Domain Service를 올바른 DDD 관점으로 완전히 재설계**

### ❌ 이전 문제점
```java
// 잘못됨: 단순 팩토리 메서드
public Message createTextMessage(ChannelId channelId, UserId senderId, String text) {
    validateTextContent(text);
    MessageContent content = MessageContent.text(text);
    return Message.create(channelId, senderId, content, MessageType.TEXT);
}
```

**문제**:
- Domain Service가 단순 팩토리 역할만 수행
- **도메인 규칙 검증 없음** (채널 접근 권한, 사용자 상태 등)
- 여러 Aggregate 간의 협력이 없음
- ID만 받아서 실제 도메인 규칙을 검증할 수 없음

---

## ✅ 완료된 작업

### 1. User Aggregate 생성 (신규)

#### 1.1 User.java (Aggregate Root)
```java
@Getter
@Builder
public class User {
    public static final UserId SYSTEM_USER_ID = UserId.of("system");
    
    private final UserId id;
    private String username;
    private String email;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastActiveAt;

    // 도메인 메서드
    public boolean canSendMessage() {
        return this.status == UserStatus.ACTIVE;
    }
    
    public boolean isBanned() { ... }
    public boolean isSuspended() { ... }
    public void suspend() { ... }
    public void ban() { ... }
    public void activate() { ... }
}
```

#### 1.2 UserStatus.java (Enum)
```java
public enum UserStatus {
    ACTIVE,      // 활성 상태
    SUSPENDED,   // 정지 상태
    BANNED,      // 차단 상태
    WITHDRAWN    // 탈퇴 상태
}
```

#### 1.3 UserRepository.java (포트)
```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    boolean existsById(UserId id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

---

### 2. DomainException 생성

```java
/**
 * 도메인 규칙 위반 예외
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
```

---

### 3. MessageDomainService 완전 재설계 ⭐

#### ✅ 올바른 설계
```java
public class MessageDomainService {
    
    /**
     * 텍스트 메시지 생성
     * 
     * @param channel 메시지를 발송할 채널 (Aggregate)
     * @param sender 메시지를 발송하는 사용자 (Aggregate)
     * @param text 메시지 텍스트 내용
     */
    public Message createTextMessage(Channel channel, User sender, String text) {
        // Step 1: 도메인 규칙 검증 - 채널 접근 권한
        validateChannelAccess(channel, sender);
        
        // Step 2: 도메인 규칙 검증 - 메시지 발송 가능 여부
        validateMessageSendingCapability(channel, sender);
        
        // Step 3: 메시지 내용 검증
        validateTextContent(text);
        
        // Step 4: 메시지 생성
        MessageContent content = MessageContent.text(text);
        return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
    }
    
    // 동일하게 createImageMessage, createFileMessage, createSystemMessage 구현
}
```

#### 주요 도메인 규칙 검증 메서드

##### validateChannelAccess
```java
/**
 * 채널 접근 권한 검증
 * 
 * 도메인 규칙:
 * - 사용자는 채널의 멤버여야 함
 * - 채널이 활성화되어 있어야 함
 */
private void validateChannelAccess(Channel channel, User sender) {
    if (!channel.isActive()) {
        throw new DomainException("Channel is not active");
    }
    
    if (!channel.isMember(sender.getId())) {
        throw new DomainException("User is not a member of the channel");
    }
}
```

##### validateMessageSendingCapability
```java
/**
 * 메시지 발송 가능 여부 검증
 * 
 * 도메인 규칙:
 * - 사용자가 활성 상태여야 함
 * - 사용자가 차단되지 않았어야 함
 * - 사용자가 정지되지 않았어야 함
 */
private void validateMessageSendingCapability(Channel channel, User sender) {
    if (!sender.canSendMessage()) {
        throw new DomainException("User is not allowed to send messages (status: " + sender.getStatus() + ")");
    }
    
    if (sender.isBanned()) {
        throw new DomainException("User is banned and cannot send messages");
    }
    
    if (sender.isSuspended()) {
        throw new DomainException("User is suspended and cannot send messages");
    }
}
```

##### 파일 크기 제한 검증
```java
/**
 * 이미지 파일 크기 검증 (10MB 제한)
 */
private void validateImageFileSize(Long fileSize) {
    long maxImageSize = 10 * 1024 * 1024; // 10MB
    if (fileSize > maxImageSize) {
        throw new DomainException("Image file size exceeds maximum allowed size (10MB)");
    }
}

/**
 * 파일 크기 검증 (50MB 제한)
 */
private void validateFileSize(Long fileSize) {
    long maxFileSize = 50 * 1024 * 1024; // 50MB
    if (fileSize > maxFileSize) {
        throw new DomainException("File size exceeds maximum allowed size (50MB)");
    }
}
```

---

### 4. MessageApplicationService 수정

#### 올바른 Application Service 패턴
```java
@Transactional
public MessageResponse sendMessage(SendMessageRequest request) {
    // Step 1: 인증 확인
    UserId senderId = getUserIdFromContext();
    
    // Step 2: 필수 파라미터 검증
    if (request.getChannelId() == null || request.getChannelId().isBlank()) {
        throw new IllegalArgumentException("Channel ID is required");
    }
    
    // Step 3: Aggregate 조회 - Channel
    Channel channel = findChannelById(request.getChannelId());
    
    // Step 4: Aggregate 조회 - User
    User sender = findUserById(senderId);
    
    // Step 5: Domain Service 호출 - 메시지 생성 (도메인 규칙 검증 포함)
    Message message = createMessageByType(channel, sender, request);
    
    // Step 6: 저장
    Message savedMessage = messageRepository.save(message);
    
    // Step 7: 이벤트 발행
    publishMessageEvent(savedMessage);
    
    // Step 8: Response 변환
    return convertToResponse(savedMessage);
}
```

**핵심 변경사항**:
- ❌ `ChannelId`, `UserId`를 Domain Service에 전달 (이전)
- ✅ `Channel`, `User` Aggregate를 Domain Service에 전달 (현재)

---

### 5. chat-storage 계층 추가 구현

#### 5.1 UserEntity.java
```java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String id;
    private String username;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastActiveAt;
}
```

#### 5.2 JpaUserRepository.java
```java
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### 5.3 UserMapper.java
```java
@Component
public class UserMapper {
    public UserEntity toEntity(User user) { ... }
    public User toDomain(UserEntity entity) { ... }
}
```

#### 5.4 UserRepositoryAdapter.java
```java
@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository jpaRepository;
    private final UserMapper mapper;
    
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    // ...
}
```

---

## 📊 아키텍처 비교

### ❌ 이전 아키텍처 (잘못됨)
```
Application Service
    ↓ (ChannelId, UserId 전달)
Domain Service
    ↓ (ID로 팩토리 메서드만 호출)
Aggregate (Message)
```
**문제**: 도메인 규칙 검증 없음

---

### ✅ 현재 아키텍처 (올바름)
```
Application Service
    ↓ Repository에서 Aggregate 조회
    ↓ (Channel, User Aggregate 전달)
Domain Service
    ↓ 도메인 규칙 검증 (채널 접근 권한, 사용자 상태 등)
    ↓ 여러 Aggregate 간 협력
Aggregate (Message) 생성
```
**장점**: 
- 도메인 규칙이 Domain Service에 집중
- 비즈니스 로직 재사용 가능
- 테스트 용이

---

## 🎯 DDD 원칙 준수 확인

### ✅ Domain Service의 올바른 역할
1. **여러 Aggregate 간의 협력 조율** ✅
   - Channel, User, Message 간의 상호작용
   
2. **복잡한 도메인 규칙 검증** ✅
   - 채널 접근 권한 검증
   - 사용자 메시지 발송 가능 여부 검증
   - 파일 크기 제한 검증
   
3. **도메인 불변식(Invariants) 보장** ✅
   - 차단된 사용자는 메시지 발송 불가
   - 비활성 채널에는 메시지 발송 불가
   - 멤버가 아닌 사용자는 메시지 발송 불가

### ✅ Application Service의 올바른 역할
1. **트랜잭션 경계 관리** ✅
2. **Aggregate 조회 및 조율** ✅
3. **Domain Service 호출** ✅
4. **이벤트 발행** ✅
5. **DTO 변환** ✅

---

## 📝 핵심 학습 사항

### 1. Domain Service는 팩토리가 아니다
❌ **잘못됨**: ID만 받아서 객체 생성
```java
public Message createTextMessage(ChannelId channelId, UserId senderId, String text)
```

✅ **올바름**: Aggregate를 받아서 도메인 규칙 검증 후 객체 생성
```java
public Message createTextMessage(Channel channel, User sender, String text)
```

### 2. 도메인 규칙은 Domain Service에
- 채널 접근 권한 검증
- 사용자 상태 검증
- 파일 크기 제한 검증
- 여러 Aggregate 간의 비즈니스 규칙

### 3. Application Service는 조율자
- Repository에서 Aggregate 조회
- Domain Service에 Aggregate 전달
- 트랜잭션 관리
- 이벤트 발행

---

## 🚀 다음 단계

### 1. ScheduleDomainService 재설계 (필요)
현재 ScheduleDomainService도 동일한 문제가 있을 가능성이 높음
- Channel, User, Message Aggregate를 받도록 수정
- 스케줄 생성 도메인 규칙 검증 추가

### 2. ChannelDomainService 재설계 (필요)
- 채널 생성/삭제 권한 검증
- 멤버 추가/제거 도메인 규칙

### 3. DB 마이그레이션
users 테이블 생성:
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_active_at TIMESTAMP
);
```

---

## 💡 결론

**이제 올바른 DDD Domain Service 설계**:
- ✅ 여러 Aggregate 간의 협력
- ✅ 복잡한 도메인 규칙 검증
- ✅ 비즈니스 로직 중앙화
- ✅ 테스트 가능한 도메인 로직
- ✅ 재사용 가능한 도메인 서비스

이전처럼 단순 팩토리가 아닌, **진정한 Domain Service**가 되었습니다!
