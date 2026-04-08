---
name: Lombok & Java Code Style (Java 21, UTF-8)
description: Record for DTOs, Lombok on entities, Java 21 features
scope: All Java code
applies-to: Classes, entities, DTOs
version: 1.0
triggers: New class, entity, DTO
---

# Rule: Lombok & Java Code Style

## Record for DTOs (Immutable)

```java
public record SendMessageRequest(
    Long roomId,
    @NotBlank String content,
    @Positive(required = false) Long fileId
) {}

public record MessageResponse(
    Long id,
    String content,
    LocalDateTime createdAt
) {
    public static MessageResponse from(Message msg) {
        return new MessageResponse(msg.getId(), msg.getContent(), msg.getCreatedAt());
    }
}
```

## Lombok on Entities

```java
@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    public static User create(String email, String hash) {
        return User.builder().email(email).passwordHash(hash).build();
    }
}
```

## Lombok on Services

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCommandService {
    
    private final MessageRepository repo;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Message send(Long roomId, Long userId, String content) {
        log.debug("Sending message");
        Message msg = Message.create(roomId, userId, content);
        return repo.save(msg);
    }
}
```

## Java 21 Features

```java
// Sealed classes
public sealed abstract class ChatEvent permits
    MessageCreatedEvent, UserJoinedEvent {}

// Text blocks
String query = """
    SELECT m FROM Message m
    WHERE m.roomId = :roomId
    ORDER BY m.createdAt DESC
    """;

// Pattern matching
if (event instanceof MessageCreatedEvent mce) {
    return mce.content();
}

// Records for immutable DTOs
public record MessageResponse(Long id, String content) {}
```

## Code Style Rules

| Rule | Pattern |
|------|---------|
| **No magic constants** | Use named constants: `Duration.ofHours(2)` |
| **Early return** | Validate at method start |
| **Constructor injection** | Use `@RequiredArgsConstructor` |
| **Null safety** | Use `Optional<T>` |
| **Immutable collections** | `List.of()`, `Map.of()` |
| **Stream API** | Fluent operations |
| **UTF-8 encoding** | All files |

## Lombok Annotations Quick Ref

| Annotation | Use |
|------------|-----|
| `@Getter` / `@Setter` | Entities |
| `@Builder` | Entities, services, tests |
| `@NoArgsConstructor` | JPA entities |
| `@AllArgsConstructor` | Models |
| `@RequiredArgsConstructor` | Spring `@Service` |
| `@Slf4j` | Services |
| `@Data` | ⚠️ Avoid on entities |

## Rules

| Rule | Detail |
|------|--------|
| **Record for DTOs** | All `api/request/`, `api/response/` |
| **Lombok on entity** | `@Getter`, `@Builder`, `@NoArgsConstructor` |
| **Constructor injection** | Use `@RequiredArgsConstructor` |
| **Java 21** | Sealed classes, text blocks, records |
| **No magic constants** | Named constants |
| **Early return** | Validate at start |
| **UTF-8 encoding** | All files |

## Checklist

- [ ] All DTOs are `record`
- [ ] Entities: `@Getter`, `@Builder`, `@NoArgsConstructor`
- [ ] Services: `@RequiredArgsConstructor`
- [ ] All files UTF-8
- [ ] No magic constants
- [ ] Early return pattern
- [ ] Java 21 features used

---

Examples: `lombok-and-code-style/docs/`
