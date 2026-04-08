# Record DTO — Examples

## Pagination Response

```java
public record PageResponse<T>(
    List<T> content,
    Long nextCursor,
    boolean hasMore,
    int size
) {
    public static <T, D> PageResponse<D> of(
        List<T> items,
        int pageSize,
        Function<T, D> mapper,
        Function<T, Long> idExtractor
    ) {
        boolean hasMore = items.size() > pageSize;
        List<D> page = items.stream().limit(pageSize).map(mapper).toList();
        Long cursor = hasMore ? idExtractor.apply(items.get(pageSize - 1)) : null;
        return new PageResponse<>(page, cursor, hasMore, page.size());
    }
}
```

## Nested Response

```java
public record ChatRoomResponse(
    Long id,
    String name,
    UserSummary lastSender,
    MessageSummary lastMessage
) {
    public record UserSummary(Long id, String name) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getDisplayName());
        }
    }

    public record MessageSummary(Long id, String content) {
        public static MessageSummary from(Message msg) {
            return new MessageSummary(msg.getId(), msg.getContent());
        }
    }

    public static ChatRoomResponse from(ChatRoom room, Message last, User sender) {
        return new ChatRoomResponse(
            room.getId(), room.getName(),
            UserSummary.from(sender),
            MessageSummary.from(last)
        );
    }
}
```

## Compact Constructor Validation

```java
public record CreateUserRequest(
    @Email String email,
    @NotBlank String password,
    String displayName
) {
    public CreateUserRequest {
        email = email.trim().toLowerCase();
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = email.split("@")[0];
        }
    }
}
```

## Batch Factory

```java
public record MessageResponse(Long id, String content, String senderName) {

    public static MessageResponse from(Message message, User sender) {
        return new MessageResponse(
            message.getId(), message.getContent(), sender.getDisplayName()
        );
    }

    public static List<MessageResponse> from(List<Message> messages, Map<Long, User> senders) {
        return messages.stream()
            .map(msg -> from(msg, senders.get(msg.getSenderId())))
            .toList();
    }
}
```

## Anti-Patterns

```java
// ❌ class instead of record — mutable
public class MessageResponse {
    private Long id;
    public void setId(Long id) { this.id = id; }
}

// ❌ Expose domain entity directly
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}

// ✅ Always convert at API boundary
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return UserResponse.from(userRepository.findById(id).orElseThrow());
}
```