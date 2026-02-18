# 친구 및 채팅방 관리/조회 기능 고도화 설계

> **작성일**: 2026-02-17  
> **목적**: DDD + EDA 기반 확장 가능한 친구 관리 및 채팅방 고급 조회 기능 설계

---

## 📋 목차

1. [현황 분석](#현황-분석)
2. [고도화 전략](#고도화-전략)
3. [1단계: 친구 관리 기능](#1단계-친구-관리-기능)
4. [2단계: 채팅방 고급 조회](#2단계-채팅방-고급-조회)
5. [3단계: 실시간 상태 관리](#3단계-실시간-상태-관리)
6. [4단계: 성능 최적화](#4단계-성능-최적화)
7. [구현 순서](#구현-순서)

---

## 현황 분석

### 현재 도메인 모델

```
User (Aggregate Root)
├── UserId
├── username, email
├── status (ACTIVE, SUSPENDED, BANNED)
└── lastActiveAt

Channel (Aggregate Root)
├── ChannelId
├── name, description
├── type (DIRECT, GROUP, PUBLIC, PRIVATE)
├── ownerId
├── memberIds (Set<UserId>)
└── active

Message (Aggregate Root)
├── MessageId
├── channelId
├── senderId
├── content
└── status
```

### 현재 기능

✅ **구현된 기능**

- 채널 생성 (Direct, Group)
- 채널 멤버 추가/제거
- 채널 목록 조회 (사용자별)
- 메시지 발송

❌ **부족한 기능**

- 친구 관계 관리 없음
- 채널 검색/필터링 없음
- 읽지 않은 메시지 수 없음
- 마지막 메시지 정보 없음
- 실시간 사용자 상태 없음
- 채팅방 즐겨찾기/알림 설정 없음

---

## 고도화 전략

### 설계 원칙

1. **DDD Aggregate 분리**: Friendship, ChannelMetadata를 별도 Aggregate로 관리
2. **읽기 최적화**: CQRS 패턴 적용 (Command/Query 분리)
3. **캐시 활용**: Redis를 통한 실시간 상태 및 읽기 성능 최적화
4. **이벤트 기반**: EDA를 통한 느슨한 결합
5. **확장성**: SaaS 대비 멀티 테넌시 고려

---

## 1단계: 친구 관리 기능

### 1.1. Friendship Aggregate 설계

```java
/**
 * 친구 관계 Aggregate Root
 *
 * 도메인 규칙:
 * - 친구 관계는 양방향 (A-B 관계 생성 시 B-A도 자동 생성)
 * - 상태: PENDING, ACCEPTED, BLOCKED
 * - 차단은 일방적 가능
 */
@Getter
@Builder
public class Friendship {
	private final FriendshipId id;
	private final UserId userId;        // 관계 요청자
	private final UserId friendId;      // 친구 (대상자)
	private FriendshipStatus status;    // PENDING, ACCEPTED, BLOCKED
	private String nickname;            // 친구 별칭 (선택)
	private boolean favorite;           // 즐겨찾기 여부
	private final Instant createdAt;
	private Instant updatedAt;

	// === Factory Methods ===

	/**
	 * 친구 요청 생성
	 */
	public static Friendship requestFriendship(UserId userId, UserId friendId) {
		if (userId.equals(friendId)) {
			throw new DomainException("Cannot add yourself as a friend");
		}

		return Friendship.builder()
				.id(FriendshipId.generate())
				.userId(userId)
				.friendId(friendId)
				.status(FriendshipStatus.PENDING)
				.favorite(false)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
	}

	// === Business Methods ===

	/**
	 * 친구 요청 수락
	 */
	public void accept() {
		if (this.status != FriendshipStatus.PENDING) {
			throw new DomainException("Only pending requests can be accepted");
		}
		this.status = FriendshipStatus.ACCEPTED;
		this.updatedAt = Instant.now();
	}

	/**
	 * 친구 차단
	 */
	public void block() {
		this.status = FriendshipStatus.BLOCKED;
		this.updatedAt = Instant.now();
	}

	/**
	 * 친구 별칭 설정
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
		this.updatedAt = Instant.now();
	}

	/**
	 * 즐겨찾기 토글
	 */
	public void toggleFavorite() {
		this.favorite = !this.favorite;
		this.updatedAt = Instant.now();
	}

	/**
	 * 수락된 친구 관계인지 확인
	 */
	public boolean isAccepted() {
		return this.status == FriendshipStatus.ACCEPTED;
	}

	/**
	 * 차단된 관계인지 확인
	 */
	public boolean isBlocked() {
		return this.status == FriendshipStatus.BLOCKED;
	}
}
```

### 1.2. FriendshipStatus Enum

```java
/**
 * 친구 관계 상태
 */
public enum FriendshipStatus {
	PENDING,    // 대기 중 (요청됨)
	ACCEPTED,   // 수락됨
	BLOCKED     // 차단됨 (일방적)
}
```

### 1.3. FriendshipRepository 인터페이스

```java
/**
 * 친구 관계 Repository (포트)
 */
public interface FriendshipRepository {

	/**
	 * 친구 관계 저장
	 */
	Friendship save(Friendship friendship);

	/**
	 * ID로 조회
	 */
	Optional<Friendship> findById(FriendshipId id);

	/**
	 * 두 사용자 간 친구 관계 조회
	 */
	Optional<Friendship> findByUserIdAndFriendId(UserId userId, UserId friendId);

	/**
	 * 사용자의 모든 친구 목록 (수락된 관계만)
	 */
	List<Friendship> findAcceptedFriendsByUserId(UserId userId);

	/**
	 * 사용자에게 온 친구 요청 목록
	 */
	List<Friendship> findPendingRequestsByFriendId(UserId friendId);

	/**
	 * 사용자가 차단한 목록
	 */
	List<Friendship> findBlockedByUserId(UserId userId);

	/**
	 * 즐겨찾기 친구 목록
	 */
	List<Friendship> findFavoritesByUserId(UserId userId);

	/**
	 * 친구 관계 삭제
	 */
	void deleteById(FriendshipId id);

	/**
	 * 양방향 친구 관계 존재 여부
	 */
	boolean existsMutualFriendship(UserId userId, UserId friendId);
}
```

### 1.4. FriendshipDomainService

```java
/**
 * 친구 관계 도메인 서비스
 *
 * 책임:
 * - User + Friendship Aggregate 간 협력
 * - 양방향 친구 관계 생성 규칙
 */
public class FriendshipDomainService {

	/**
	 * 친구 요청 생성
	 *
	 * Domain Rule:
	 * - 양방향 관계 생성 (A→B, B→A)
	 * - A→B는 PENDING, B→A는 PENDING
	 * - 차단 상태면 요청 불가
	 */
	public Pair<Friendship, Friendship> requestFriendship(User requester, User target) {
		// Early Return: 자기 자신 체크
		if (requester.getId().equals(target.getId())) {
			throw new DomainException("Cannot add yourself as a friend");
		}

		// Early Return: 사용자 상태 체크
		if (!requester.canSendMessage() || !target.canSendMessage()) {
			throw new DomainException("Users must be active to create friendship");
		}

		// 양방향 관계 생성
		Friendship requestToTarget = Friendship.requestFriendship(
				requester.getId(),
				target.getId()
		);

		Friendship requestFromTarget = Friendship.requestFriendship(
				target.getId(),
				requester.getId()
		);

		return Pair.of(requestToTarget, requestFromTarget);
	}

	/**
	 * 친구 요청 수락
	 *
	 * Domain Rule:
	 * - 양방향 모두 ACCEPTED 상태로 변경
	 */
	public void acceptFriendship(Friendship myRequest, Friendship theirRequest) {
		// Early Return: 상태 검증
		if (myRequest.getStatus() != FriendshipStatus.PENDING) {
			throw new DomainException("Can only accept pending requests");
		}

		// 양방향 수락
		myRequest.accept();
		theirRequest.accept();
	}

	/**
	 * 친구 차단
	 *
	 * Domain Rule:
	 * - 일방적 차단 가능
	 * - 기존 친구 관계가 있으면 차단으로 변경
	 */
	public void blockFriend(Friendship friendship) {
		friendship.block();
	}
}
```

### 1.5. 친구 관리 Use Case (Application Service)

```java
/**
 * 친구 관리 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipApplicationService {

	private final FriendshipRepository friendshipRepository;
	private final UserRepository userRepository;
	private final FriendshipDomainService friendshipDomainService;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 친구 요청
	 */
	public FriendshipResponse requestFriendship(UserId requesterId, UserId targetId) {
		// 1. User Aggregate 조회
		User requester = userRepository.findById(requesterId)
				.orElseThrow(() -> new NotFoundException("Requester not found"));
		User target = userRepository.findById(targetId)
				.orElseThrow(() -> new NotFoundException("Target user not found"));

		// 2. 기존 관계 확인
		Optional<Friendship> existing = friendshipRepository
				.findByUserIdAndFriendId(requesterId, targetId);

		if (existing.isPresent()) {
			if (existing.get().isBlocked()) {
				throw new DomainException("Cannot send request to blocked user");
			}
			throw new DomainException("Friendship already exists");
		}

		// 3. Domain Service를 통한 친구 요청 생성
		Pair<Friendship, Friendship> friendships =
				friendshipDomainService.requestFriendship(requester, target);

		// 4. 저장
		Friendship saved = friendshipRepository.save(friendships.getLeft());
		friendshipRepository.save(friendships.getRight());

		// 5. 이벤트 발행 (알림 전송용)
		eventPublisher.publishEvent(new FriendRequestedEvent(
				requesterId,
				targetId,
				Instant.now()
		));

		return FriendshipResponse.from(saved);
	}

	/**
	 * 친구 요청 수락
	 */
	public void acceptFriendRequest(UserId userId, FriendshipId requestId) {
		// 1. 내 요청 조회 (상대방이 나에게 보낸 요청)
		Friendship myRequest = friendshipRepository.findById(requestId)
				.orElseThrow(() -> new NotFoundException("Request not found"));

		// 2. 권한 확인 (내가 friendId여야 함)
		if (!myRequest.getFriendId().equals(userId)) {
			throw new DomainException("Not authorized to accept this request");
		}

		// 3. 양방향 관계 조회
		Friendship theirRequest = friendshipRepository
				.findByUserIdAndFriendId(myRequest.getUserId(), userId)
				.orElseThrow(() -> new NotFoundException("Mutual request not found"));

		// 4. Domain Service를 통한 수락
		friendshipDomainService.acceptFriendship(myRequest, theirRequest);

		// 5. 저장
		friendshipRepository.save(myRequest);
		friendshipRepository.save(theirRequest);

		// 6. 이벤트 발행
		eventPublisher.publishEvent(new FriendAcceptedEvent(
				userId,
				myRequest.getUserId(),
				Instant.now()
		));
	}

	/**
	 * 친구 목록 조회 (수락된 친구만)
	 */
	@Transactional(readOnly = true)
	public List<FriendshipResponse> getFriendList(UserId userId) {
		return friendshipRepository.findAcceptedFriendsByUserId(userId).stream()
				.map(FriendshipResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 받은 친구 요청 목록
	 */
	@Transactional(readOnly = true)
	public List<FriendshipResponse> getPendingRequests(UserId userId) {
		return friendshipRepository.findPendingRequestsByFriendId(userId).stream()
				.map(FriendshipResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 친구 차단
	 */
	public void blockFriend(UserId userId, UserId friendId) {
		Friendship friendship = friendshipRepository
				.findByUserIdAndFriendId(userId, friendId)
				.orElseThrow(() -> new NotFoundException("Friendship not found"));

		friendshipDomainService.blockFriend(friendship);
		friendshipRepository.save(friendship);
	}

	/**
	 * 친구 별칭 설정
	 */
	public void setFriendNickname(UserId userId, UserId friendId, String nickname) {
		Friendship friendship = friendshipRepository
				.findByUserIdAndFriendId(userId, friendId)
				.orElseThrow(() -> new NotFoundException("Friendship not found"));

		friendship.setNickname(nickname);
		friendshipRepository.save(friendship);
	}

	/**
	 * 즐겨찾기 토글
	 */
	public void toggleFavorite(UserId userId, UserId friendId) {
		Friendship friendship = friendshipRepository
				.findByUserIdAndFriendId(userId, friendId)
				.orElseThrow(() -> new NotFoundException("Friendship not found"));

		friendship.toggleFavorite();
		friendshipRepository.save(friendship);
	}
}
```

---

## 2단계: 채팅방 고급 조회

### 2.1. ChannelMetadata Aggregate 설계

```java
/**
 * 채팅방 메타데이터 Aggregate Root
 *
 * 책임:
 * - 사용자별 채팅방 설정 (알림, 즐겨찾기, 읽은 위치)
 * - 읽지 않은 메시지 수 계산
 * - 마지막 읽은 메시지 추적
 *
 * CQRS 패턴 적용:
 * - Command: Channel Aggregate
 * - Query: ChannelMetadata (읽기 최적화)
 */
@Getter
@Builder
public class ChannelMetadata {
	private final ChannelMetadataId id;
	private final ChannelId channelId;
	private final UserId userId;

	// 사용자별 설정
	private boolean notificationEnabled;  // 알림 설정
	private boolean favorite;             // 즐겨찾기
	private boolean pinned;               // 상단 고정

	// 읽기 상태
	private MessageId lastReadMessageId;  // 마지막 읽은 메시지 ID
	private Instant lastReadAt;           // 마지막 읽은 시간
	private int unreadCount;              // 읽지 않은 메시지 수

	// 메타 정보
	private Instant lastActivityAt;       // 마지막 활동 시간 (메시지 발송/읽음)
	private final Instant createdAt;
	private Instant updatedAt;

	// === Factory Methods ===

	/**
	 * 새로운 채팅방 메타데이터 생성
	 */
	public static ChannelMetadata create(ChannelId channelId, UserId userId) {
		return ChannelMetadata.builder()
				.id(ChannelMetadataId.generate())
				.channelId(channelId)
				.userId(userId)
				.notificationEnabled(true)  // 기본값: 알림 켜짐
				.favorite(false)
				.pinned(false)
				.unreadCount(0)
				.lastActivityAt(Instant.now())
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
	}

	// === Business Methods ===

	/**
	 * 메시지 읽음 처리
	 */
	public void markAsRead(MessageId messageId, int newUnreadCount) {
		this.lastReadMessageId = messageId;
		this.lastReadAt = Instant.now();
		this.unreadCount = newUnreadCount;
		this.lastActivityAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	/**
	 * 읽지 않은 메시지 수 증가
	 */
	public void incrementUnreadCount() {
		this.unreadCount++;
		this.lastActivityAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	/**
	 * 알림 토글
	 */
	public void toggleNotification() {
		this.notificationEnabled = !this.notificationEnabled;
		this.updatedAt = Instant.now();
	}

	/**
	 * 즐겨찾기 토글
	 */
	public void toggleFavorite() {
		this.favorite = !this.favorite;
		this.updatedAt = Instant.now();
	}

	/**
	 * 상단 고정 토글
	 */
	public void togglePinned() {
		this.pinned = !this.pinned;
		this.updatedAt = Instant.now();
	}

	/**
	 * 읽지 않은 메시지가 있는지 확인
	 */
	public boolean hasUnreadMessages() {
		return this.unreadCount > 0;
	}
}
```

### 2.2. ChannelListQuery (CQRS Query 모델)

```java
/**
 * 채팅방 목록 조회용 Query 모델
 *
 * CQRS 패턴:
 * - 읽기 전용 모델
 * - 다양한 필터링/정렬 지원
 */
@Getter
@Builder
public class ChannelListQuery {
	private final UserId userId;

	// 필터 조건
	private ChannelType type;           // 채널 타입 필터
	private Boolean onlyFavorites;      // 즐겨찾기만
	private Boolean onlyUnread;         // 읽지 않은 메시지가 있는 것만
	private String searchKeyword;       // 채널명 검색

	// 정렬 조건
	private ChannelSortBy sortBy;       // 정렬 기준
	private SortDirection direction;    // 정렬 방향

	// 페이징
	private int page;
	private int size;
}

/**
 * 채팅방 정렬 기준
 */
public enum ChannelSortBy {
	LAST_ACTIVITY,   // 마지막 활동 시간 (기본값)
	NAME,            // 채널명
	UNREAD_COUNT,    // 읽지 않은 메시지 수
	CREATED_AT       // 생성 시간
}
```

### 2.3. ChannelListItem (Query Response DTO)

```java
/**
 * 채팅방 목록 아이템
 *
 * UI에 필요한 모든 정보를 포함
 */
@Getter
@Builder
public class ChannelListItem {
	// 채널 기본 정보
	private String channelId;
	private String channelName;
	private ChannelType channelType;

	// 마지막 메시지 정보
	private String lastMessageContent;
	private String lastMessageSenderId;
	private String lastMessageSenderName;
	private Instant lastMessageTime;

	// 사용자별 메타 정보
	private int unreadCount;
	private boolean favorite;
	private boolean pinned;
	private boolean notificationEnabled;

	// 멤버 정보 (Direct 채널용)
	private String otherUserId;         // 1:1 채팅 상대방 ID
	private String otherUserName;       // 1:1 채팅 상대방 이름
	private UserOnlineStatus otherUserStatus;  // 상대방 온라인 상태

	// 그룹 채널 정보
	private int memberCount;
	private Instant lastActivityAt;
}
```

### 2.4. ChannelQueryService

```java
/**
 * 채팅방 조회 Query Service
 *
 * CQRS Query Side:
 * - 복잡한 조회 로직
 * - 여러 Aggregate 조인
 * - 캐시 활용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelQueryService {

	private final ChannelRepository channelRepository;
	private final ChannelMetadataRepository metadataRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final UserOnlineStatusCache onlineStatusCache;  // Redis 캐시

	/**
	 * 채팅방 목록 조회 (고급 필터링)
	 */
	public Page<ChannelListItem> getChannelList(ChannelListQuery query) {
		// 1. 사용자의 채널 목록 조회
		List<Channel> channels = channelRepository.findByMemberId(query.getUserId());

		// 2. 채널 ID 리스트 추출
		List<ChannelId> channelIds = channels.stream()
				.map(Channel::getId)
				.collect(Collectors.toList());

		// 3. 메타데이터 조회 (배치)
		Map<ChannelId, ChannelMetadata> metadataMap =
				metadataRepository.findByChannelIdsAndUserId(channelIds, query.getUserId())
						.stream()
						.collect(Collectors.toMap(
								ChannelMetadata::getChannelId,
								metadata -> metadata
						));

		// 4. 마지막 메시지 조회 (배치)
		Map<ChannelId, Message> lastMessageMap =
				messageRepository.findLastMessageByChannelIds(channelIds);

		// 5. ChannelListItem 변환
		List<ChannelListItem> items = channels.stream()
				.map(channel -> buildChannelListItem(
						channel,
						metadataMap.get(channel.getId()),
						lastMessageMap.get(channel.getId())
				))
				.collect(Collectors.toList());

		// 6. 필터링
		items = applyFilters(items, query);

		// 7. 정렬
		items = applySorting(items, query);

		// 8. 페이징
		return applyPagination(items, query);
	}

	/**
	 * ChannelListItem 빌드
	 */
	private ChannelListItem buildChannelListItem(
			Channel channel,
			ChannelMetadata metadata,
			Message lastMessage
	) {
		ChannelListItemBuilder builder = ChannelListItem.builder()
				.channelId(channel.getId().getValue())
				.channelName(channel.getName())
				.channelType(channel.getType())
				.memberCount(channel.getMemberIds().size());

		// 메타데이터 정보
		if (metadata != null) {
			builder.unreadCount(metadata.getUnreadCount())
					.favorite(metadata.isFavorite())
					.pinned(metadata.isPinned())
					.notificationEnabled(metadata.isNotificationEnabled())
					.lastActivityAt(metadata.getLastActivityAt());
		}

		// 마지막 메시지 정보
		if (lastMessage != null) {
			builder.lastMessageContent(lastMessage.getContent().getText())
					.lastMessageSenderId(lastMessage.getSenderId().getValue())
					.lastMessageTime(lastMessage.getCreatedAt());

			// 발신자 정보 조회
			userRepository.findById(lastMessage.getSenderId())
					.ifPresent(user ->
							builder.lastMessageSenderName(user.getUsername())
					);
		}

		// Direct 채널인 경우 상대방 정보
		if (channel.getType() == ChannelType.DIRECT) {
			UserId otherId = getOtherUserId(channel, metadata.getUserId());
			builder.otherUserId(otherId.getValue());

			userRepository.findById(otherId).ifPresent(otherUser -> {
				builder.otherUserName(otherUser.getUsername());

				// Redis에서 온라인 상태 조회
				UserOnlineStatus status = onlineStatusCache.getStatus(otherId);
				builder.otherUserStatus(status);
			});
		}

		return builder.build();
	}

	/**
	 * Direct 채널에서 상대방 UserId 추출
	 */
	private UserId getOtherUserId(Channel channel, UserId myId) {
		return channel.getMemberIds().stream()
				.filter(id -> !id.equals(myId))
				.findFirst()
				.orElseThrow(() -> new DomainException("Invalid direct channel"));
	}

	/**
	 * 필터링 적용
	 */
	private List<ChannelListItem> applyFilters(
			List<ChannelListItem> items,
			ChannelListQuery query
	) {
		Stream<ChannelListItem> stream = items.stream();

		// 타입 필터
		if (query.getType() != null) {
			stream = stream.filter(item -> item.getChannelType() == query.getType());
		}

		// 즐겨찾기 필터
		if (Boolean.TRUE.equals(query.getOnlyFavorites())) {
			stream = stream.filter(ChannelListItem::isFavorite);
		}

		// 읽지 않은 메시지 필터
		if (Boolean.TRUE.equals(query.getOnlyUnread())) {
			stream = stream.filter(item -> item.getUnreadCount() > 0);
		}

		// 검색어 필터
		if (query.getSearchKeyword() != null && !query.getSearchKeyword().isBlank()) {
			String keyword = query.getSearchKeyword().toLowerCase();
			stream = stream.filter(item ->
					item.getChannelName().toLowerCase().contains(keyword)
			);
		}

		return stream.collect(Collectors.toList());
	}

	/**
	 * 정렬 적용
	 */
	private List<ChannelListItem> applySorting(
			List<ChannelListItem> items,
			ChannelListQuery query
	) {
		Comparator<ChannelListItem> comparator;

		switch (query.getSortBy()) {
			case NAME:
				comparator = Comparator.comparing(ChannelListItem::getChannelName);
				break;
			case UNREAD_COUNT:
				comparator = Comparator.comparing(ChannelListItem::getUnreadCount);
				break;
			case CREATED_AT:
				comparator = Comparator.comparing(ChannelListItem::getLastActivityAt);
				break;
			case LAST_ACTIVITY:
			default:
				// 고정된 채널은 항상 상단
				comparator = Comparator
						.comparing(ChannelListItem::isPinned, Comparator.reverseOrder())
						.thenComparing(
								Comparator.comparing(
										ChannelListItem::getLastActivityAt,
										Comparator.nullsLast(Comparator.naturalOrder())
								).reversed()
						);
		}

		// 정렬 방향 적용
		if (query.getDirection() == SortDirection.ASC) {
			comparator = comparator.reversed();
		}

		return items.stream()
				.sorted(comparator)
				.collect(Collectors.toList());
	}

	/**
	 * 페이징 적용
	 */
	private Page<ChannelListItem> applyPagination(
			List<ChannelListItem> items,
			ChannelListQuery query
	) {
		int start = query.getPage() * query.getSize();
		int end = Math.min(start + query.getSize(), items.size());

		if (start >= items.size()) {
			return new PageImpl<>(List.of(), PageRequest.of(query.getPage(), query.getSize()), items.size());
		}

		List<ChannelListItem> pageItems = items.subList(start, end);
		return new PageImpl<>(pageItems, PageRequest.of(query.getPage(), query.getSize()), items.size());
	}
}
```

---

## 3단계: 실시간 상태 관리

### 3.1. UserOnlineStatus (Redis 캐시)

```java
/**
 * 사용자 온라인 상태
 */
public enum UserOnlineStatus {
	ONLINE,     // 온라인
	AWAY,       // 자리 비움 (5분 이상 비활성)
	OFFLINE     // 오프라인
}

/**
 * 사용자 온라인 상태 캐시 (Redis)
 */
@Component
@RequiredArgsConstructor
public class UserOnlineStatusCache {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String KEY_PREFIX = "user:status:";
	private static final Duration ONLINE_TTL = Duration.ofMinutes(5);

	/**
	 * 사용자 온라인 상태 설정
	 */
	public void setOnline(UserId userId) {
		String key = KEY_PREFIX + userId.getValue();
		redisTemplate.opsForValue().set(
				key,
				UserOnlineStatus.ONLINE.name(),
				ONLINE_TTL
		);
	}

	/**
	 * 사용자 상태 조회
	 */
	public UserOnlineStatus getStatus(UserId userId) {
		String key = KEY_PREFIX + userId.getValue();
		String status = redisTemplate.opsForValue().get(key);

		if (status == null) {
			return UserOnlineStatus.OFFLINE;
		}

		return UserOnlineStatus.valueOf(status);
	}

	/**
	 * 사용자 오프라인 처리
	 */
	public void setOffline(UserId userId) {
		String key = KEY_PREFIX + userId.getValue();
		redisTemplate.delete(key);
	}

	/**
	 * 하트비트 (온라인 상태 갱신)
	 */
	public void heartbeat(UserId userId) {
		setOnline(userId);
	}
}
```

### 3.2. WebSocket 연결 시 온라인 상태 관리

```java
/**
 * WebSocket 핸들러에 온라인 상태 관리 추가
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private final UserOnlineStatusCache onlineStatusCache;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		UserId userId = extractUserId(session);

		// 온라인 상태 설정
		onlineStatusCache.setOnline(userId);

		// 이벤트 발행 (친구들에게 알림)
		eventPublisher.publishEvent(new UserOnlineEvent(userId, Instant.now()));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		UserId userId = extractUserId(session);

		// 오프라인 상태 설정
		onlineStatusCache.setOffline(userId);

		// 이벤트 발행
		eventPublisher.publishEvent(new UserOfflineEvent(userId, Instant.now()));
	}
}
```

---

## 4단계: 성능 최적화

### 4.1. 배치 조회 (N+1 문제 해결)

```java
/**
 * MessageRepository에 배치 조회 추가
 */
public interface MessageRepository {

	/**
	 * 여러 채널의 마지막 메시지를 한 번에 조회
	 */
	Map<ChannelId, Message> findLastMessageByChannelIds(List<ChannelId> channelIds);
}

/**
 * 구현 (JPA Native Query 사용)
 */
@Repository
public class MessageRepositoryAdapter implements MessageRepository {

	@Override
	public Map<ChannelId, Message> findLastMessageByChannelIds(List<ChannelId> channelIds) {
		if (channelIds.isEmpty()) {
			return Map.of();
		}

		// Subquery를 사용한 최신 메시지 조회
		String sql = """
				SELECT m.*
				FROM chat_messages m
				INNER JOIN (
				    SELECT channel_id, MAX(created_at) as max_created_at
				    FROM chat_messages
				    WHERE channel_id IN (:channelIds)
				    GROUP BY channel_id
				) latest ON m.channel_id = latest.channel_id 
				        AND m.created_at = latest.max_created_at
				""";

		// ... 실행 및 변환
	}
}
```

### 4.2. Redis 캐싱 전략

```java
/**
 * 채팅방 목록 캐싱
 */
@Component
@RequiredArgsConstructor
public class ChannelListCacheManager {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String CACHE_KEY_PREFIX = "channel:list:";
	private static final Duration CACHE_TTL = Duration.ofMinutes(10);

	/**
	 * 캐시 조회
	 */
	public Optional<List<ChannelListItem>> getCachedList(UserId userId) {
		String key = CACHE_KEY_PREFIX + userId.getValue();
		String cached = redisTemplate.opsForValue().get(key);

		if (cached == null) {
			return Optional.empty();
		}

		try {
			List<ChannelListItem> items = objectMapper.readValue(
					cached,
					new TypeReference<List<ChannelListItem>>() {}
			);
			return Optional.of(items);
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * 캐시 저장
	 */
	public void cacheList(UserId userId, List<ChannelListItem> items) {
		String key = CACHE_KEY_PREFIX + userId.getValue();
		try {
			String json = objectMapper.writeValueAsString(items);
			redisTemplate.opsForValue().set(key, json, CACHE_TTL);
		}
		catch (Exception e) {
			// 캐싱 실패는 무시 (데이터 일관성에 영향 없음)
		}
	}

	/**
	 * 캐시 무효화
	 */
	public void invalidateCache(UserId userId) {
		String key = CACHE_KEY_PREFIX + userId.getValue();
		redisTemplate.delete(key);
	}
}
```

### 4.3. 이벤트 기반 캐시 무효화

```java
/**
 * 메시지 발송 시 캐시 무효화
 */
@Component
@RequiredArgsConstructor
public class MessageSentEventListener {

	private final ChannelListCacheManager cacheManager;
	private final ChannelRepository channelRepository;

	@EventListener
	@Async
	public void onMessageSent(MessageSentEvent event) {
		// 채널 멤버들의 캐시 무효화
		Channel channel = channelRepository.findById(event.getChannelId())
				.orElse(null);

		if (channel != null) {
			channel.getMemberIds().forEach(cacheManager::invalidateCache);
		}
	}
}
```

---

## 구현 순서

### Phase 1: 친구 관리 기초 (1주)

1. ✅ Friendship Domain 모델 생성
2. ✅ FriendshipRepository 구현
3. ✅ FriendshipDomainService 구현
4. ✅ FriendshipApplicationService 구현
5. ✅ REST API 구현
6. ✅ 테스트 작성

### Phase 2: 채팅방 메타데이터 (1주)

1. ✅ ChannelMetadata Domain 모델 생성
2. ✅ ChannelMetadataRepository 구현
3. ✅ 읽지 않은 메시지 수 계산 로직
4. ✅ REST API 구현 (알림/즐겨찾기 설정)

### Phase 3: 고급 조회 기능 (1주)

1. ✅ ChannelListQuery 모델 설계
2. ✅ ChannelQueryService 구현
3. ✅ 배치 조회 최적화
4. ✅ 필터링/정렬 로직 구현
5. ✅ REST API 구현

### Phase 4: 실시간 상태 (3일)

1. ✅ UserOnlineStatusCache 구현
2. ✅ WebSocket 연결 시 상태 관리
3. ✅ 하트비트 API 구현
4. ✅ 친구 상태 변경 이벤트 발행

### Phase 5: 성능 최적화 (3일)

1. ✅ Redis 캐싱 적용
2. ✅ 이벤트 기반 캐시 무효화
3. ✅ 배치 조회 최적화
4. ✅ 인덱스 최적화

---

## API 엔드포인트 설계

### 친구 관리 API

```
POST   /api/friendships              # 친구 요청
GET    /api/friendships              # 친구 목록 조회
GET    /api/friendships/pending      # 받은 친구 요청 목록
PUT    /api/friendships/{id}/accept  # 친구 요청 수락
DELETE /api/friendships/{id}         # 친구 삭제
POST   /api/friendships/{id}/block   # 친구 차단
PUT    /api/friendships/{id}/nickname # 별칭 설정
PUT    /api/friendships/{id}/favorite # 즐겨찾기 토글
```

### 채팅방 조회 API

```
GET    /api/channels                 # 채팅방 목록 (필터링/정렬)
  ?type=DIRECT
  &onlyFavorites=true
  &onlyUnread=true
  &search=keyword
  &sortBy=LAST_ACTIVITY
  &page=0
  &size=20

GET    /api/channels/{id}            # 채팅방 상세 조회
PUT    /api/channels/{id}/favorite   # 즐겨찾기 토글
PUT    /api/channels/{id}/pin        # 상단 고정 토글
PUT    /api/channels/{id}/notification # 알림 설정 토글
PUT    /api/channels/{id}/read       # 읽음 처리
```

### 온라인 상태 API

```
POST   /api/users/heartbeat          # 하트비트 (온라인 상태 갱신)
GET    /api/users/{id}/status        # 사용자 상태 조회
```

---

## 데이터베이스 스키마

### friendships 테이블

```sql
CREATE TABLE friendships
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    friend_id  VARCHAR(36) NOT NULL,
    status     VARCHAR(20) NOT NULL, -- PENDING, ACCEPTED, BLOCKED
    nickname   VARCHAR(100),
    favorite   BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,

    INDEX      idx_user_id (user_id),
    INDEX      idx_friend_id (friend_id),
    INDEX      idx_user_status (user_id, status),
    UNIQUE KEY uk_friendship (user_id, friend_id)
);
```

### channel_metadata 테이블

```sql
CREATE TABLE channel_metadata
(
    id                   VARCHAR(36) PRIMARY KEY,
    channel_id           VARCHAR(36) NOT NULL,
    user_id              VARCHAR(36) NOT NULL,
    notification_enabled BOOLEAN DEFAULT TRUE,
    favorite             BOOLEAN DEFAULT FALSE,
    pinned               BOOLEAN DEFAULT FALSE,
    last_read_message_id VARCHAR(36),
    last_read_at         TIMESTAMP,
    unread_count         INT     DEFAULT 0,
    last_activity_at     TIMESTAMP,
    created_at           TIMESTAMP   NOT NULL,
    updated_at           TIMESTAMP   NOT NULL,

    INDEX                idx_user_id (user_id),
    INDEX                idx_channel_id (channel_id),
    INDEX                idx_last_activity (user_id, last_activity_at DESC),
    UNIQUE KEY uk_channel_user (channel_id, user_id)
);
```

---

## 이벤트 정의

```java
// 친구 관련 이벤트
public record FriendRequestedEvent(UserId requesterId, UserId targetId, Instant occurredAt) {}

public record FriendAcceptedEvent(UserId userId, UserId friendId, Instant occurredAt) {}

public record FriendBlockedEvent(UserId userId, UserId blockedId, Instant occurredAt) {}

// 사용자 상태 이벤트
public record UserOnlineEvent(UserId userId, Instant occurredAt) {}

public record UserOfflineEvent(UserId userId, Instant occurredAt) {}

// 메시지 관련 이벤트
public record MessageSentEvent(ChannelId channelId, MessageId messageId, Instant occurredAt) {}

public record MessageReadEvent(ChannelId channelId, UserId userId, MessageId messageId, Instant occurredAt) {}
```

---

## 성능 목표

- **채팅방 목록 조회**: 100ms 이내 (캐시 히트 시 10ms)
- **친구 목록 조회**: 50ms 이내
- **읽지 않은 메시지 수 계산**: 실시간 (이벤트 기반)
- **온라인 상태 조회**: 5ms 이내 (Redis)

---

## 확장성 고려사항

### SaaS 멀티 테넌시

- Workspace(Tenant) ID 추가
- 모든 쿼리에 Workspace 필터링
- Row-Level Security 적용

### 대용량 처리

- 채팅방 목록 페이지네이션
- 친구 목록 커서 기반 페이징
- 배치 조회 최적화
- Redis 클러스터링

---

**작성자**: AI Assistant  
**최종 수정일**: 2026-02-17
