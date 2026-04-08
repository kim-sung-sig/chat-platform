# Repository Pattern — Examples

## Full JPA Repository Interface (Domain)

```java
public interface MessageRepository {

    Message save(Message message);

    Optional<Message> findById(Long id);

    List<Message> findByRoomId(Long roomId);

    List<Message> findMessagesBefore(Long roomId, Long cursor, int limit);

    List<Message> findNewestMessages(Long roomId, int limit);

    List<Message> searchMessages(Long roomId, String keyword, LocalDate from, LocalDate to,
                                  Long cursor, int limit);

    long countByRoomId(Long roomId);

    void deleteById(Long id);  // Soft delete
}
```

## Spring Data Internal Interface

```java
@Repository
interface SpringDataMessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long roomId);

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
    List<MessageEntity> findMessagesBefore(
        @Param("roomId") Long roomId,
        @Param("cursor") Long cursor,
        @Param("limit") int limit
    );

    @Query("""
        SELECT m FROM MessageEntity m
        WHERE m.roomId = :roomId
          AND m.deletedAt IS NULL
        ORDER BY m.createdAt DESC
        LIMIT :limit
    """)
    List<MessageEntity> findNewest(
        @Param("roomId") Long roomId,
        @Param("limit") int limit
    );

    @Query("""
        SELECT m FROM MessageEntity m
        WHERE m.roomId = :roomId
          AND (:keyword IS NULL OR m.content LIKE %:keyword%)
          AND (:from IS NULL OR m.createdAt >= :from)
          AND (:to IS NULL OR m.createdAt <= :to)
          AND (:cursor IS NULL OR m.id < :cursor)
          AND m.deletedAt IS NULL
        ORDER BY m.createdAt DESC
        LIMIT :limit
    """)
    List<MessageEntity> search(
        @Param("roomId") Long roomId,
        @Param("keyword") String keyword,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("cursor") Long cursor,
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedAt = :now WHERE m.id = :id AND m.deletedAt IS NULL")
    int softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);
}
```

## Full Repository Implementation

```java
@Repository
@RequiredArgsConstructor
public class JpaMessageRepository implements MessageRepository {

    private final SpringDataMessageRepository springRepo;
    private final MessageMapper mapper;

    @Override
    public Message save(Message message) {
        MessageEntity entity = mapper.toPersistence(message);
        return mapper.toDomain(springRepo.save(entity));
    }

    @Override
    public Optional<Message> findById(Long id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Message> findByRoomId(Long roomId) {
        return springRepo
            .findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(roomId)
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
    public List<Message> findNewestMessages(Long roomId, int limit) {
        return springRepo.findNewest(roomId, limit)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Message> searchMessages(Long roomId, String keyword,
                                         LocalDate from, LocalDate to,
                                         Long cursor, int limit) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;
        return springRepo.search(roomId, keyword, fromDt, toDt, cursor, limit)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteById(Long id) {
        int updated = springRepo.softDelete(id, LocalDateTime.now());
        if (updated == 0) {
            throw new MessageNotFoundException(id);
        }
    }
}
```

## Mapper with Enum Conversion

```java
@Component
public class MessageMapper {

    public MessageEntity toPersistence(Message message) {
        return MessageEntity.builder()
            .id(message.getId())
            .roomId(message.getRoomId())
            .senderId(message.getSenderId())
            .content(message.getContent())
            .status(message.getStatus().name())
            .createdAt(message.getCreatedAt())
            .updatedAt(message.getUpdatedAt())
            .deletedAt(message.getDeletedAt())
            .build();
    }

    public Message toDomain(MessageEntity entity) {
        return Message.builder()
            .id(entity.getId())
            .roomId(entity.getRoomId())
            .senderId(entity.getSenderId())
            .content(entity.getContent())
            .status(MessageStatus.valueOf(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
```