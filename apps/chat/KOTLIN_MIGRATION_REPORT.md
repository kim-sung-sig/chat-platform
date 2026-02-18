# Kotlin Migration Report - chat-domain & chat-storage 모듈

## 마이그레이션 개요

**날짜**: 2026-02-16  
**대상 모듈**: 
- `apps/chat/libs/chat-domain`
- `apps/chat/libs/chat-storage`

## 마이그레이션 완료 항목

### 1. chat-domain 모듈

#### 도메인 모델 (Domain Models)
- ✅ `Channel` - 채널 Aggregate Root
- ✅ `ChannelId` - 채널 ID Value Object
- ✅ `ChannelType` - 채널 타입 Enum
- ✅ `User` - 사용자 Aggregate Root
- ✅ `UserId` - 사용자 ID Value Object
- ✅ `UserStatus` - 사용자 상태 Enum
- ✅ `Message` - 메시지 Aggregate Root
- ✅ `MessageId` - 메시지 ID Value Object
- ✅ `MessageType` - 메시지 타입 Enum
- ✅ `MessageStatus` - 메시지 상태 Enum
- ✅ `MessageContent` - 메시지 내용 Value Object

#### 공통 모델
- ✅ `Cursor` - 커서 기반 페이징 Value Object

#### Repository 인터페이스
- ✅ `ChannelRepository` - 채널 저장소 인터페이스
- ✅ `UserRepository` - 사용자 저장소 인터페이스
- ✅ `MessageRepository` - 메시지 저장소 인터페이스

#### 도메인 서비스
- ✅ `ChannelDomainService` - 채널 도메인 서비스
- ✅ `MessageDomainService` - 메시지 도메인 서비스
- ✅ `DomainException` - 도메인 예외

### 2. chat-storage 모듈

#### JPA 엔티티
- ✅ `ChatChannelEntity` - 채널 엔티티
- ✅ `ChatChannelMemberEntity` - 채널 멤버 엔티티
- ✅ `ChatMessageEntity` - 메시지 엔티티
- ✅ `UserEntity` - 사용자 엔티티

#### 매퍼 (Mappers)
- ✅ `ChannelMapper` - Channel Domain ↔ Entity 변환
- ✅ `MessageMapper` - Message Domain ↔ Entity 변환
- ✅ `UserMapper` - User Domain ↔ Entity 변환

## 기술적 변경 사항

### build.gradle.kts 변경

#### chat-domain
```kotlin
plugins {
    kotlin("jvm")  // Java 플러그인 제거, Kotlin만 사용
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(emptyList<String>())  // Java 소스 제외
        }
        kotlin {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}

dependencies {
    // Lombok 제거
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.mockk:mockk:1.13.9")  // MockK 추가
}
```

#### chat-storage
```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")     // JPA 플러그인 추가
    kotlin("plugin.spring")  // Spring 플러그인 추가
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Lombok 제거
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.mockk:mockk:1.13.9")
}
```

### 코드 변환 특징

#### 1. Value Objects
**Before (Java)**:
```java
@Getter
@EqualsAndHashCode
@ToString
public class ChannelId {
    @NonNull
    private final String value;
    
    private ChannelId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("...");
        }
        this.value = value;
    }
}
```

**After (Kotlin)**:
```kotlin
data class ChannelId(val value: String) {
    init {
        require(value.isNotBlank()) { "ChannelId cannot be null or blank" }
    }
    
    companion object {
        fun of(value: String): ChannelId = ChannelId(value)
        fun generate(): ChannelId = ChannelId(UUID.randomUUID().toString())
    }
}
```

#### 2. Aggregate Roots
**변경 포인트**:
- Lombok `@Builder` → Kotlin private constructor + companion object factory method
- Lombok `@Getter` → Kotlin property (backing field 사용)
- 불변성 강화: `private var _field`로 내부 상태 보호

#### 3. JPA Entities
**Kotlin JPA 특징**:
- `kotlin("plugin.jpa")` 플러그인 사용
- No-arg constructor를 `protected constructor()` 로 명시
- `@Entity` 클래스는 `open class` 또는 `all-open` 플러그인 사용

#### 4. Repository 인터페이스
**Before (Java)**:
```java
Optional<Channel> findById(ChannelId id);
List<Channel> findByMemberId(UserId userId);
```

**After (Kotlin)**:
```kotlin
fun findById(id: ChannelId): Channel?
fun findByMemberId(userId: UserId): List<Channel>
```

#### 5. Domain Services
**변경 포인트**:
- `IllegalArgumentException` → `require()` 함수
- `IllegalStateException` → `check()` 함수
- Early Return 패턴 유지

## 빌드 결과

```bash
✅ :apps:chat:libs:chat-domain:build SUCCESS
✅ :apps:chat:libs:chat-storage:build SUCCESS
```

## 삭제된 항목

- ❌ `src/main/java` 디렉토리 전체 (Java 소스)
- ❌ Lombok 의존성
- ❌ Java 플러그인

## 남은 작업

### 다음 단계
1. **message-server Kotlin 마이그레이션**
   - Application Service Layer
   - REST Controllers
   - Event Publishers (Redis)
   - Kafka Producer 추가

2. **websocket-server Kotlin 마이그레이션**
   - WebSocket Handlers
   - Redis Subscriber
   - Session Management

3. **system-server Kotlin 마이그레이션**
   - Admin API
   - System Management Services

4. **테스트 코드 작성**
   - JUnit 5 + MockK
   - Domain Service 테스트
   - Repository 테스트

## 코딩 컨벤션 준수 사항

### SOLID 원칙
- ✅ **SRP**: 각 도메인 객체는 단일 책임
- ✅ **OCP**: 다형성 활용 (MessageType, ChannelType enum)
- ✅ **DIP**: Repository 인터페이스로 추상화

### DDD 패턴
- ✅ Aggregate Root 패턴 적용 (Channel, User, Message)
- ✅ Value Object 불변성 유지
- ✅ Domain Service로 복잡한 비즈니스 규칙 분리
- ✅ Repository 패턴 (Port)
- ✅ Entity는 데이터 컨테이너가 아닌 행위 중심 설계

### Kotlin 관용구
- ✅ `data class` 활용 (Value Objects)
- ✅ `require()`, `check()` 사용
- ✅ Null-safety (`?` 연산자)
- ✅ `companion object` 활용
- ✅ Property 사용 (getter/setter 대신)

## 참고 사항

### Reflection 사용
Mapper에서 도메인 객체의 private 생성자를 호출하기 위해 Reflection을 사용했습니다. 
이는 JPA Entity → Domain Model 변환 시 불가피한 선택입니다.

```kotlin
val constructor = Channel::class.java.getDeclaredConstructor(...)
constructor.isAccessible = true
return constructor.newInstance(...)
```

**개선 방안**: 
- Factory method를 public으로 노출
- 또는 별도의 Builder 패턴 도입

### JPA + Kotlin 주의사항
1. `all-open` 플러그인 또는 `open` 키워드 필요
2. No-arg constructor 필수
3. Lazy loading을 위한 proxy 생성 가능해야 함

## 결론

chat-domain과 chat-storage 모듈의 Kotlin 마이그레이션이 성공적으로 완료되었습니다.
- ✅ 모든 도메인 모델 변환 완료
- ✅ 빌드 성공
- ✅ 코딩 컨벤션 준수
- ✅ DDD 패턴 유지
- ✅ SOLID 원칙 준수

다음 단계는 Application Layer (message-server, websocket-server, system-server)의 Kotlin 마이그레이션입니다.

