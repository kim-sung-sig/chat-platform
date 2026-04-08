# Lombok & Code Style — Examples

## JPA Entity (Full Lombok)

```java
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_room_id", columnList = "room_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"room", "sender"})  // Prevent lazy-load in toString
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;  // Optimistic locking

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    // Business methods (not Lombok-generated)
    public static Message create(Long roomId, Long senderId, String content) {
        return Message.builder()
            .roomId(roomId)
            .senderId(senderId)
            .content(content)
            .build();
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
```

## Java 21 Text Blocks in Queries

```java
@Repository
interface SpringDataMessageRepository extends JpaRepository<MessageEntity, Long> {

    // ✅ Text blocks for multi-line @Query
    @Query("""
        SELECT m FROM MessageEntity m
        WHERE m.roomId = :roomId
          AND m.createdAt < (
              SELECT m2.createdAt FROM MessageEntity m2 WHERE m2.id = :cursor
          )
          AND m.deletedAt IS NULL
        ORDER BY m.createdAt DESC
        LIMIT :limit
    """)
    List<MessageEntity> findBefore(
        @Param("roomId") Long roomId,
        @Param("cursor") Long cursor,
        @Param("limit") int limit
    );
}
```

## Java 21 Text Blocks in Tests

```java
@Test
@DisplayName("유효한 JSON으로 201을 반환한다")
void returns201() throws Exception {
    mockMvc.perform(post("/api/messages")
        .contentType(APPLICATION_JSON)
        .content("""
            {
                "roomId": 1,
                "content": "Hello, World!"
            }
        """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Hello, World!"));
}
```

## Java 21 Pattern Matching

```java
// Event processing with pattern matching
public void handleEvent(DomainEvent event) {
    if (event instanceof MessageCreatedEvent mce) {
        searchService.indexMessage(mce.messageId(), mce.content());
    } else if (event instanceof UserCreatedEvent uce) {
        searchService.indexUser(uce.userId(), uce.email());
    } else if (event instanceof RoomCreatedEvent rce) {
        searchService.indexRoom(rce.roomId(), rce.name());
    }
}

// Switch expression (Java 21)
String topic = switch (event) {
    case MessageCreatedEvent e -> "chat.messages.created";
    case UserCreatedEvent e    -> "auth.users.created";
    case RoomCreatedEvent e    -> "chat.rooms.created";
    default -> "events.unknown";
};
```

## Early Return & No Magic Constants

```java
// ✅ Named constants
private static final int MAX_CONTENT_LENGTH = 5000;
private static final Duration SESSION_EXPIRY = Duration.ofHours(24);
private static final int MAX_MEMBERS_PER_ROOM = 1000;

public Message send(Long roomId, Long userId, String content) {
    // ✅ Early return for validation
    if (roomId == null || roomId <= 0) {
        throw new IllegalArgumentException("roomId must be positive");
    }
    if (content == null || content.isBlank()) {
        throw new InvalidMessageException("Content required");
    }
    if (content.length() > MAX_CONTENT_LENGTH) {
        throw new InvalidMessageException("Content exceeds " + MAX_CONTENT_LENGTH + " chars");
    }

    // Main logic after validation
    Message message = Message.create(roomId, userId, content.trim());
    return repo.save(message);
}
```

## Optional & Stream (Functional Style)

```java
// ✅ Optional chain
public UserResponse getProfile(Long userId) {
    return userRepository.findById(userId)
        .map(UserResponse::from)
        .orElseThrow(() -> new UserNotFoundException(userId));
}

// ✅ Stream with method reference
public List<String> getActiveUserEmails(Long roomId) {
    return memberRepository.findByRoomId(roomId).stream()
        .filter(Member::isActive)
        .map(Member::getUserEmail)
        .sorted()
        .toList();
}

// ✅ Immutable collection
private static final List<String> ALLOWED_MIME_TYPES = List.of(
    "image/jpeg", "image/png", "image/gif", "image/webp"
);
```