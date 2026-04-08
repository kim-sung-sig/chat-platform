# common/core - CLAUDE.md

Shared library for **core domain types & cross-cutting concerns**.

## Overview

**Purpose**: Reusable domain exceptions, event bus, audit models, ID generation  
**Used by**: auth-server, chat, push-service, other microservices  
**Stack**: Java 21, Spring Boot (optional), no database dependency

## Package Structure

```
common:core/src/main/java/com/example/chat/common/
├── exception/
│   ├── DomainException.java      # Base exception for all domain errors
│   ├── ValidationException.java
│   └── TechnicalException.java
├── event/
│   ├── DomainEvent.java          # Event base interface
│   ├── DomainEventPublisher.java # Event bus interface
│   └── events/                   # Shared domain events
│       ├── UserCreatedEvent.java
│       └── ...
├── model/
│   ├── AggregateRoot.java        # Base aggregate
│   ├── ValueObject.java
│   └── shared/                   # Shared value objects
│       ├── UserId.java
│       ├── Email.java
│       └── ...
├── audit/
│   ├── AuditInfo.java            # Audit timestamp, actor
│   └── AuditLog.java
├── id/
│   ├── IdGenerator.java          # UUID, Ulid strategies
│   └── ...
└── config/
    └── CoreAutoConfiguration.java # Spring Boot auto-config (optional)
```

## Key Components

### 1. Exception Hierarchy
```java
DomainException (checked, from common:core)
├── UserNotFoundException
├── InvalidOperationException
└── ...

TechnicalException
└── ExternalServiceException
```

### 2. Event Bus
```java
// Interface
public interface DomainEventPublisher {
  void publish(DomainEvent event);
}

// Used in domain layer
domainEventPublisher.publish(new UserCreatedEvent(...));
```

### 3. Shared Value Objects
- `UserId`, `Email`, `PhoneNumber` — type-safe IDs and primitives
- `MoneyAmount`, `Duration` — business concepts

### 4. Audit Info
- Auto-capture: `createdAt`, `createdBy`, `updatedAt`, `updatedBy`
- Used in all aggregate roots

## Build & Test

```bash
./gradlew :common:core:clean build
```

## Testing

- Unit tests only (no database)
- Test exceptions, value objects, event structures
- Minimum 80% coverage

## Usage in Other Modules

```java
// In auth-server domain
import com.example.chat.common.exception.DomainException;
import com.example.chat.common.event.DomainEventPublisher;
import com.example.chat.common.model.UserId;

public class User extends AggregateRoot {
  private UserId id;
  
  public static User create(String email, DomainEventPublisher pub) {
    if (email == null) throw new DomainException("Email required");
    var user = new User(UserId.generate(), email);
    pub.publish(new UserCreatedEvent(user.id, email, now()));
    return user;
  }
}
```

## References

**Consumed by**: auth-server, chat, push-service  
**Parent**: `../../CLAUDE.md`

---
**Last Updated**: 2026-04-08 | **Scope**: shared core library
