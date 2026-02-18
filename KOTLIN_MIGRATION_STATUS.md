# Kotlin 마이그레이션 가이드

> **작성일**: 2026-02-17  
> **상태**: Domain Layer 일부 완료

---

## 📋 현재 상황

프로젝트는 **Kotlin 베이스**이지만, Phase 1-3에서 **Java로 작성**되었습니다.

### 마이그레이션 완료 (5개 파일)

✅ **Domain Layer - Friendship Aggregate (Kotlin)**

```
apps/chat/libs/chat-domain/src/main/kotlin/com/example/chat/domain/
├── friendship/
│   ├── Friendship.kt                  ✅ (Kotlin data class)
│   ├── FriendshipId.kt                ✅ (Kotlin value class)
│   ├── FriendshipStatus.kt            ✅ (Kotlin enum)
│   └── FriendshipRepository.kt        ✅ (Kotlin interface)
└── service/
    └── FriendshipDomainService.kt     ✅ (Kotlin @Service)
```

### 아직 Java인 파일들

❌ **Storage Layer (Java)**

```
apps/chat/libs/chat-storage/src/main/java/
├── entity/
│   ├── ChatFriendshipEntity.java
│   └── ChatChannelMetadataEntity.java
├── repository/
│   ├── JpaFriendshipRepository.java
│   └── JpaChannelMetadataRepository.java
├── mapper/
│   ├── FriendshipMapper.java
│   └── ChannelMetadataMapper.java
└── adapter/
    ├── FriendshipRepositoryAdapter.java
    └── ChannelMetadataRepositoryAdapter.java
```

❌ **Application Layer (Java)**

```
apps/chat/system-server/src/main/java/
├── application/
│   ├── service/
│   │   ├── FriendshipApplicationService.java
│   │   ├── ChannelMetadataApplicationService.java
│   │   └── ChannelQueryService.java
│   ├── dto/
│   │   ├── request/*.java
│   │   └── response/*.java
│   └── query/
│       ├── ChannelListQuery.java
│       └── ChannelSortBy.java
└── controller/
    ├── FriendshipController.java
    ├── ChannelMetadataController.java
    └── ChannelQueryController.java
```

---

## 🎯 Kotlin 마이그레이션 전략

### 1단계: Domain Layer (최우선) ✅

**완료**:

- ✅ Friendship Aggregate → Kotlin data class
- ✅ FriendshipId → Kotlin value class (@JvmInline)
- ✅ FriendshipStatus → Kotlin enum
- ✅ FriendshipRepository → Kotlin interface
- ✅ FriendshipDomainService → Kotlin @Service

**남은 작업**:

- [ ] ChannelMetadata Aggregate
- [ ] ChannelMetadataId
- [ ] ChannelMetadataRepository

---

### 2단계: Storage Layer (다음)

**변환 예시**:

**Before (Java)**:

```java

@Entity
@Table(name = "chat_friendships")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFriendshipEntity {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "user_id")
	private String userId;
	// ...
}
```

**After (Kotlin)**:

```kotlin
@Entity
@Table(name = "chat_friendships")
data class ChatFriendshipEntity(
	@Id
	@Column(name = "id")
	val id: String,

	@Column(name = "user_id")
	val userId: String,

	@Column(name = "friend_id")
	val friendId: String,

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	val status: FriendshipStatus,

	@Column(name = "nickname")
	var nickname: String? = null,

	@Column(name = "favorite")
	var favorite: Boolean = false,

	@Column(name = "created_at")
	val createdAt: Instant,

	@Column(name = "updated_at")
	var updatedAt: Instant
)
```

---

### 3단계: Application Layer

**변환 예시**:

**Before (Java)**:

```java

@Service
@RequiredArgsConstructor
public class FriendshipApplicationService {
	private final FriendshipRepository friendshipRepository;

	public FriendshipResponse requestFriendship(String requesterId, String targetId) {
		// ...
	}
}
```

**After (Kotlin)**:

```kotlin
@Service
class FriendshipApplicationService(
	private val friendshipRepository: FriendshipRepository,
	private val userRepository: UserRepository,
	private val friendshipDomainService: FriendshipDomainService,
	private val eventPublisher: ApplicationEventPublisher
) {
	@Transactional
	fun requestFriendship(requesterId: String, targetId: String): FriendshipResponse {
		logger.info { "Requesting friendship: $requesterId → $targetId" }

		val requester = userRepository.findById(UserId.of(requesterId))
			?: throw ResourceNotFoundException("User not found: $requesterId")

		val target = userRepository.findById(UserId.of(targetId))
			?: throw ResourceNotFoundException("User not found: $targetId")

		// ...
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}
```

---

### 4단계: Controller

**변환 예시**:

**Before (Java)**:

```java

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {
	private final FriendshipApplicationService service;

	@PostMapping
	public ResponseEntity<FriendshipResponse> requestFriendship(
			@RequestHeader("X-User-Id") String userId,
			@RequestBody FriendshipRequest request
	) {
		// ...
	}
}
```

**After (Kotlin)**:

```kotlin
@RestController
@RequestMapping("/api/friendships")
class FriendshipController(
	private val friendshipApplicationService: FriendshipApplicationService
) {
	@PostMapping
	fun requestFriendship(
		@RequestHeader("X-User-Id") userId: String,
		@Valid @RequestBody request: FriendshipRequest
	): ResponseEntity<FriendshipResponse> {
		logger.info { "POST /api/friendships - userId: $userId, friendId: ${request.friendId}" }

		val response = friendshipApplicationService.requestFriendship(userId, request.friendId)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}
```

---

## 🔧 Kotlin 주요 변환 패턴

### 1. Data Class 활용

**Java**:

```java

@Getter
@Builder
@AllArgsConstructor
public class FriendshipResponse {
	private String id;
	private String userId;
	private FriendshipStatus status;
}
```

**Kotlin**:

```kotlin
data class FriendshipResponse(
	val id: String,
	val userId: String,
	val status: FriendshipStatus
)
```

---

### 2. Null Safety

**Java**:

```java
private String nickname; // nullable

public void setNickname(String nickname) {
	this.nickname = nickname;
}
```

**Kotlin**:

```kotlin
var nickname: String? = null

fun setNickname(nickname: String) {
	this.nickname = nickname
}
```

---

### 3. Value Class (Inline Class)

**Java**:

```java

@Getter
@EqualsAndHashCode
public class FriendshipId {
	private final String value;

	private FriendshipId(String value) {
		this.value = value;
	}
}
```

**Kotlin**:

```kotlin
@JvmInline
value class FriendshipId(val value: String) {
	init {
		require(value.isNotBlank()) { "FriendshipId cannot be blank" }
	}
}
```

---

### 4. Extension Functions

**Java**:

```java
public static FriendshipResponse from(Friendship friendship) {
	return FriendshipResponse.builder()
			.id(friendship.getId().getValue())
			// ...
			.build();
}
```

**Kotlin**:

```kotlin
fun Friendship.toResponse() = FriendshipResponse(
	id = id.value,
	userId = userId.value,
	friendId = friendId.value,
	status = status,
	// ...
)
```

---

### 5. Early Return with require/check

**Java**:

```java
if(userId.equals(friendId)){
		throw new

DomainException("Cannot add yourself");
}
```

**Kotlin**:

```kotlin
require(userId != friendId) {
	throw DomainException("Cannot add yourself")
}

// 또는 더 간단히
require(userId != friendId) { "Cannot add yourself" }
```

---

## 📊 마이그레이션 진행률

```
Domain Layer:        ████████████░░░░░░░░  60% (Friendship 완료)
Storage Layer:       ░░░░░░░░░░░░░░░░░░░░   0%
Application Layer:   ░░░░░░░░░░░░░░░░░░░░   0%
Controller Layer:    ░░░░░░░░░░░░░░░░░░░░   0%

전체:                ███░░░░░░░░░░░░░░░░░  15%
```

---

## 🚀 다음 단계

### 즉시 실행 가능한 작업

1. **ChannelMetadata를 Kotlin으로 변환**
	- ChannelMetadata.kt (data class)
	- ChannelMetadataId.kt (value class)
	- ChannelMetadataRepository.kt (interface)

2. **Storage Layer 변환**
	- Entity (data class)
	- Repository Adapter (class)
	- Mapper (extension functions)

3. **Application/Controller 변환**
	- Service (constructor injection)
	- Controller (간결한 함수)
	- DTO (data class)

---

## 💡 권장 사항

### Java와 Kotlin 혼용

현재 프로젝트는 **Kotlin 베이스**이지만, **Java와 Kotlin을 혼용**할 수 있습니다:

✅ **허용 가능**:

- Java와 Kotlin 코드가 동일 프로젝트에 공존
- 점진적 마이그레이션

❌ **비권장**:

- 동일 패키지 내 Java/Kotlin 혼재
- 무분별한 혼용

### 마이그레이션 우선순위

1. **Domain Layer** (최우선) ✅
	- 비즈니스 로직의 핵심
	- Kotlin의 표현력 극대화

2. **Application Layer**
	- Service, Controller
	- Null Safety 활용

3. **Storage Layer**
	- JPA Entity
	- Repository

---

## 📝 현재 상태 정리

### 완료된 Kotlin 파일 (5개)

1. `Friendship.kt` - Domain Aggregate (data class)
2. `FriendshipId.kt` - Value Object (value class)
3. `FriendshipStatus.kt` - Enum
4. `FriendshipRepository.kt` - Repository interface
5. `FriendshipDomainService.kt` - Domain Service

### 기능 영향 없음

- ✅ **모든 API는 정상 동작** (Java 파일들이 아직 있음)
- ✅ **빌드 성공**
- ✅ **기존 문서 유효**

---

## 🎯 결론

**현재 프로젝트는 Java와 Kotlin이 혼용된 상태**이며, **점진적으로 Kotlin으로 전환 중**입니다.

- ✅ Domain Layer의 Friendship Aggregate는 Kotlin으로 완료
- ⏳ 나머지 Layer는 Java로 유지 (정상 동작)
- 🔄 향후 점진적으로 Kotlin으로 전환 예정

**모든 기능은 정상 작동하며, API 문서와 실행 가이드는 그대로 유효합니다.**

---

**작성일**: 2026-02-17  
**작성자**: AI Assistant
