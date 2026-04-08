# common/logging - CLAUDE.md

Shared library for **structured logging & distributed tracing**.

## Overview

**Purpose**: Centralized logging config, MDC setup, Micrometer integration  
**Used by**: auth-server, chat, push-service  
**Stack**: SLF4J, Logback, Micrometer Tracing (Brave), Zipkin

## Package Structure

```
common:logging/src/main/java/com/example/chat/common/
├── logging/
│   ├── LoggingConfiguration.java   # Logback config beans
│   ├── MdcFilter.java              # MDC context setup
│   ├── CorrelationIdGenerator.java # Correlation ID (trace/span)
│   └── LoggingAspect.java          # @Aspect for method tracing
├── config/
│   └── TracingAutoConfiguration.java # Micrometer Tracing setup
└── resources/
    └── logback-spring.xml           # Logback configuration
```

## Key Features

### 1. Structured Logging
- **Format**: JSON (for log aggregation: ELK, Datadog, etc.)
- **Fields**: timestamp, level, logger, message, traceId, spanId, userId, requestId
- **Config file**: `logback-spring.xml`

### 2. MDC (Mapped Diagnostic Context)
- Auto-injected via `MdcFilter` (servlet filter)
- Keys: `traceId`, `spanId`, `userId`, `requestId`, `correlationId`
- Available in all log statements: `log.info("User {} logged in", mdc.userId);`

### 3. Micrometer Tracing
- **Tracer**: Brave implementation
- **Reporter**: Zipkin (for distributed tracing)
- **Auto-config**: Spring Boot auto-configuration
- **Result**: All HTTP requests, DB calls traced automatically

### 4. Correlation ID
- Generated per request (UUID or Ulid)
- Propagated across service boundaries (HTTP headers: `X-Correlation-Id`)
- Used for debugging multi-service flows

## Configuration

**Logback** (`src/main/resources/logback-spring.xml`):
```xml
<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
<!-- Or JSON: -->
<encoder class="net.logstash.logback.encoder.LogstashEncoder" />
```

**Micrometer Tracing** (auto-configured):
```properties
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (dev), 0.1 (prod)
  endpoints:
    web:
      exposure:
        include: prometheus, health, ...
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

## Build & Test

```bash
./gradlew :common:logging:clean build
```

## Usage in Other Modules

```java
// In auth-server controller
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@RestController
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);
  
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    String userId = "user-123"; // After authentication
    MDC.put("userId", userId);
    log.info("User login attempt: {}", req.email); // Includes traceId, spanId, userId
    // ...
  }
}
```

## References

**Used by**: auth-server, chat, push-service  
**Parent**: `../../CLAUDE.md`  
**Depends on**: common:core

---
**Last Updated**: 2026-04-08 | **Scope**: shared logging library
