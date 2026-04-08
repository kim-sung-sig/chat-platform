---
name: Repository Pattern (Domain Port & Infrastructure Adapter)
description: Domain port interface, JPA impl in infrastructure, cursor pagination
scope: All data access
applies-to: Entity persistence, CRUD operations
version: 1.0
triggers: New domain model, QueryService read operations
---

# Rule: Repository Pattern

## Core Principle

**Interface in `domain/repository/` (port). Impl in `infrastructure/` (adapter). Never expose JPA entity.**

## Repository Interface (Domain)

```java
public interface MessageRepository {
    
    Message save(Message message);
    
    Optional<Message> findById(Long id);
    
    List<Message> findByRoomId(Long roomId);
    
    // Cursor-based pagination (not offset)
    List<Message> findMessagesBefore(Long roomId, Long cursor, int limit);
    
    List<Message> findNewestMessages(Long roomId, int limit);
    
    void deleteById(Long id);  // Soft delete
}
```

## JPA Entity (Infrastructure)

```java
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_room_id", columnList = "room_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

## Repository Implementation (Infrastructure)

```java
@Repository
@RequiredArgsConstructor
public class JpaMessageRepository implements MessageRepository {
    
    private final SpringDataMessageRepository springRepo;
    private final MessageMapper mapper;
    
    @Override
    public Message save(Message message) {
        MessageEntity entity = mapper.toPersistence(message);
        MessageEntity saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Message> findById(Long id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public List<Message> findByRoomId(Long roomId) {
        return springRepo.findByRoomIdOrderByCreatedAtDesc(roomId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Message> findMessagesBefore(Long roomId, Long cursor, int limit) {
        return springRepo.findMessagesBefore(roomId, cursor, limit)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public void deleteById(Long id) {
        springRepo.softDelete(id);
    }
}
```

## Mapper (Entity ↔ Domain)

```java
@Component
public class MessageMapper {
    
    public MessageEntity toPersistence(Message message) {
        return MessageEntity.builder()
            .id(message.getId())
            .roomId(message.getRoomId())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }
    
    public Message toDomain(MessageEntity entity) {
        return Message.builder()
            .id(entity.getId())
            .roomId(entity.getRoomId())
            .content(entity.getContent())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
```

## Cursor vs Offset Pagination

❌ Offset (doesn't scale):
```java
@Query("SELECT m FROM Message m ORDER BY m.id DESC OFFSET :offset LIMIT :limit")
List<Message> findWithOffset(int offset, int limit);
```

✅ Cursor (efficient):
```java
@Query("""
    SELECT m FROM MessageEntity m
    WHERE m.roomId = :roomId
      AND m.createdAt < (SELECT m2.createdAt FROM MessageEntity m2 WHERE m2.id = :cursor)
    ORDER BY m.createdAt DESC
    LIMIT :limit
""")
List<MessageEntity> findBefore(@Param("roomId") Long roomId, 
                                @Param("cursor") Long cursor, 
                                @Param("limit") int limit);
```

## Rules

| Rule | Detail |
|------|--------|
| **Interface in domain** | `domain/repository/` |
| **Impl in infrastructure** | `infrastructure/persistence/repository/` |
| **Domain language** | Methods describe business ops, not SQL |
| **No entity leaks** | Always convert via mapper |
| **Cursor pagination** | Always use cursor (id-based) |
| **Soft delete** | Use `deleted_at` timestamp |
| **Indexes** | Define on query columns |

## Checklist

- [ ] Repository interface in `domain/repository/`
- [ ] Implementation in `infrastructure/persistence/`
- [ ] JPA entity separate from domain
- [ ] Mapper for Entity ↔ Domain
- [ ] Cursor-based pagination
- [ ] Soft delete (deleted_at)
- [ ] Indexes on query columns

---

Examples: `repository-pattern/docs/`
