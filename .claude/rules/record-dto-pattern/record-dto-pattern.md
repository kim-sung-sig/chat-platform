---
name: Record-based DTO Pattern
description: |
  Use Java `record` for all DTOs in api/request and api/response layers.
scope: All microservices
paths:
  - "apps/*/src/main/java/com/example/chat/*/api/request/"
  - "apps/*/src/main/java/com/example/chat/*/api/response/"
applies-to: Creating HTTP request/response DTOs
version: 1.0
triggers: New REST endpoint, HTTP DTO, cross-layer transfer
---

# Rule: Record-based DTO Pattern

## Core Principle

**All DTOs must be Java `record` (immutable). Responses use `from()` factory method.**

## Request DTO Pattern

```java
public record SendMessageRequest(
    Long roomId,
    @NotBlank String content,
    @Positive(required = false) Long fileId
) {
    public SendMessageRequest {
        if (content != null) content = content.trim();
    }
}
```

## Response DTO Pattern

```java
public record MessageResponse(
    Long id,
    String content,
    String senderName,
    LocalDateTime createdAt
) {
    public static MessageResponse from(Message message, User sender) {
        return new MessageResponse(
            message.getId(),
            message.getContent(),
            sender.getDisplayName(),
            message.getCreatedAt()
        );
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| **record only** | All DTOs use `record` (no class) |
| **Immutable** | All fields `final` automatically |
| **Validation** | Add `@Valid`, `@NotBlank` on REQUEST only |
| **Factory method** | Response has `public static from(Domain)` |
| **No setters** | Records are immutable |
| **Batch factory** | Provide `from(List<Domain>)` |

## Controller Integration

```java
@PostMapping
public ResponseEntity<MessageResponse> send(
    @Valid @RequestBody SendMessageRequest request,
    Authentication auth
) {
    Message message = commandService.send(
        request.roomId(),
        auth.userId(),
        request.content()
    );
    return ResponseEntity.ok(MessageResponse.from(message, getCurrentUser(auth)));
}
```

## Checklist

- [ ] Request record has `@Valid`, `@NotBlank`
- [ ] Response record has `from()` factory
- [ ] No setter methods
- [ ] Domain model never exposed in response
- [ ] Batch factory for list responses

---

See examples: `record-dto-pattern/docs/`
