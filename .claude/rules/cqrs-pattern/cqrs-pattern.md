---
name: CQRS Pattern (Command Query Responsibility Segregation)
description: Separate write (Commands) from read (Queries) operations
scope: All microservices
applies-to: Creating application services, implementing use cases
version: 1.0
triggers: State mutation, data retrieval, scalable queries
---

# Rule: CQRS Pattern

## Core Principle

**Separate write (Command) and read (Query) operations into distinct services.**

## Service Naming

```
<Entity>CommandService    (Write, @Transactional)
<Entity>QueryService      (Read, @Transactional(readOnly=true))
```

## CommandService Example

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MessageCommandService {
    
    private final MessageRepository repo;
    private final DomainEventPublisher eventPublisher;
    
    public Message send(Long roomId, Long userId, String content) {
        Message message = Message.create(roomId, userId, content);
        Message saved = repo.save(message);
        eventPublisher.publish(new MessageCreatedEvent(...));  // After save
        return saved;
    }
}
```

## QueryService Example

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService {
    
    private final MessageRepository repo;
    
    public List<Message> findByRoom(Long roomId) {
        return repo.findByRoomId(roomId);
    }
    
    // Cursor-based pagination (not offset)
    public CursorPageResult<Message> getThread(
        Long roomId,
        Optional<Long> cursor,
        int pageSize
    ) {
        List<Message> items = cursor
            .map(c -> repo.findBefore(roomId, c, pageSize + 1))
            .orElseGet(() -> repo.findNewest(roomId, pageSize + 1));
        
        boolean hasMore = items.size() > pageSize;
        return new CursorPageResult<>(
            items.stream().limit(pageSize).toList(),
            hasMore ? items.get(pageSize - 1).getId() : null,
            hasMore
        );
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| **Naming** | `<Entity>CommandService` (write), `<Entity>QueryService` (read) |
| **@Transactional** | Command: `@Transactional`, Query: `@Transactional(readOnly=true)` |
| **Events** | CommandService publishes events after save |
| **No mutations in Query** | QueryService read-only |
| **Pagination** | Cursor-based (no offset) |
| **Return types** | Command: entity, Query: DTO |

## Checklist

- [ ] `<Entity>CommandService` exists
- [ ] `<Entity>QueryService` exists  
- [ ] Command has `@Transactional`
- [ ] Query has `@Transactional(readOnly=true)`
- [ ] Events published after command succeeds
- [ ] Cursor pagination used
- [ ] No database mutations in QueryService

---

Examples: `cqrs-pattern/docs/`
