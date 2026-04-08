---
name: Event-Driven Architecture
description: Publish domain events after state mutations
scope: All microservices
applies-to: CommandService mutations, side effects, cross-service events
version: 1.0
triggers: After save, async operations, Kafka integration
---

# Rule: Event-Driven Architecture

## Core Principle

**Publish domain events after successful state mutations. Handle side effects asynchronously.**

## Step 1: Define Domain Event

```java
public record MessageCreatedEvent(
    Long messageId,
    Long roomId,
    Long senderId,
    String content,
    Instant createdAt
) implements DomainEvent {
    public MessageCreatedEvent {
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Invalid messageId");
        }
    }
}
```

## Step 2: Publish in CommandService

```java
@Service
@Transactional
@RequiredArgsConstructor
public class MessageCommandService {
    
    private final MessageRepository repo;
    private final DomainEventPublisher eventPublisher;
    
    public Message send(Long roomId, Long userId, String content) {
        Message message = Message.create(roomId, userId, content);
        Message saved = repo.save(message);
        
        eventPublisher.publish(new MessageCreatedEvent(
            saved.getId(),
            saved.getRoomId(),
            saved.getSenderId(),
            saved.getContent(),
            Instant.now()
        ));
        
        return saved;
    }
}
```

## Step 3: Event Handler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCreatedEventHandler {
    
    private final SearchIndexService searchService;
    private final NotificationService notificationService;
    
    @EventListener(MessageCreatedEvent.class)
    @Transactional
    public void onMessageCreated(MessageCreatedEvent event) {
        try {
            searchService.indexMessage(event.messageId(), event.content());
            notificationService.notifyRoom(event.roomId(), event.senderId());
        } catch (Exception e) {
            log.error("Failed to handle event: {}", event.messageId(), e);
        }
    }
    
    @EventListener(MessageCreatedEvent.class)
    @Async
    public void sendEmailAsync(MessageCreatedEvent event) {
        // Runs in thread pool
    }
}
```

## Step 4: Kafka (Cross-Service)

```java
@Component
@RequiredArgsConstructor
public class MessagePublisher {
    
    private final KafkaTemplate<String, String> kafka;
    
    @EventListener(MessageCreatedEvent.class)
    public void publishToKafka(MessageCreatedEvent event) {
        kafka.send("chat.messages.created", 
            "msg-" + event.messageId(),
            objectMapper.writeValueAsString(event));
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| **Publish after save** | Event published AFTER `repository.save()` succeeds |
| **Immutable events** | Events are `record` with validation |
| **Handler resilience** | Catch exceptions, log, don't propagate |
| **Async for slow ops** | Mark with `@Async` |
| **No handler in domain** | Handlers in `application/listener/` |
| **Kafka for cross-service** | Inter-service events via Kafka |

## Error Handling

```java
// Pattern 1: Swallow & log (non-critical)
try {
    searchService.index(event);
} catch (Exception e) {
    log.error("Search failed, continuing...", e);
}

// Pattern 2: Async with retry (critical)
@EventListener @Async @Retryable(maxAttempts=3)
public void onEvent(MessageCreatedEvent event) { ... }
```

## Checklist

- [ ] Domain event defined as `record`
- [ ] Event published after `repository.save()`
- [ ] Handler in `application/listener/`
- [ ] Handler catches and logs exceptions
- [ ] Slow operations marked `@Async`
- [ ] Kafka topic for cross-service events

---

Examples: `event-driven-architecture/docs/`
