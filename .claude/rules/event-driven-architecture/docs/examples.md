# Event-Driven Architecture — Examples

## Multiple Events from One Command

```java
@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository repo;
    private final DomainEventPublisher events;

    public User register(String email, String passwordHash) {
        User user = User.create(email, passwordHash);
        User saved = repo.save(user);

        // Multiple events for different subscribers
        events.publish(new UserCreatedEvent(saved.getId(), saved.getEmail(), Instant.now()));
        events.publish(new WelcomeEmailRequestedEvent(saved.getId(), saved.getEmail()));
        return saved;
    }
}
```

## Handler with Multiple Side Effects

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventHandler {

    private final SearchIndexService searchService;
    private final AuditLogRepository auditLog;
    private final SlackNotifier slackNotifier;

    @EventListener(UserCreatedEvent.class)
    @Transactional
    public void onUserCreated(UserCreatedEvent event) {
        // Side effect 1: search index
        try {
            searchService.indexUser(event.userId(), event.email());
        } catch (Exception e) {
            log.error("Search index failed for user {}", event.userId(), e);
        }

        // Side effect 2: audit log (must succeed)
        auditLog.save(AuditLog.of("USER_CREATED", event.userId(), event.createdAt()));
    }

    // Side effect 3: async (slow external call)
    @EventListener(UserCreatedEvent.class)
    @Async("notificationExecutor")
    public void notifySlack(UserCreatedEvent event) {
        slackNotifier.send("#signups", "New user: " + event.email());
    }
}
```

## Kafka Consumer (Cross-Service)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventConsumer {

    private final NotificationApplicationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "chat.messages.created",
        groupId = "push-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessageCreated(ConsumerRecord<String, String> record) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(
                record.value(), MessageCreatedEvent.class
            );
            log.info("Push service received message event: {}", event.messageId());
            notificationService.sendPushForNewMessage(
                event.roomId(), event.senderId(), event.content()
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event: {}", record.value(), e);
            throw new RuntimeException("Deserialization failed", e);  // Triggers retry
        } catch (Exception e) {
            log.error("Failed to process MessageCreatedEvent", e);
            // Don't rethrow non-retryable errors
        }
    }
}
```

## DomainEventPublisher Implementation

```java
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    @Override
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}
```

## Kafka DLQ (Dead Letter Queue)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventConsumerWithDlq {

    @KafkaListener(topics = "chat.messages.created", groupId = "push-service")
    public void onMessageCreated(
        ConsumerRecord<String, String> record,
        Acknowledgment ack
    ) {
        try {
            MessageCreatedEvent event = deserialize(record.value());
            processEvent(event);
            ack.acknowledge();  // Manual ack on success
        } catch (RetryableException e) {
            log.warn("Retryable error, will retry", e);
            throw e;  // Kafka retries
        } catch (Exception e) {
            log.error("Non-retryable error, sending to DLQ", e);
            sendToDlq(record);  // Manual DLQ routing
            ack.acknowledge();  // Still ack to prevent infinite loop
        }
    }
}
```