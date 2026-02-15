# Refactoring Plan and Implementation Steps

Based on the analysis of the current structure, here is the plan to refactor `apps/chat` to meet the requirements (Kotlin base, error fixing, test codes, push service integration).

## 1. Environment Setup (Kotlin Migration)
- **Goal**: Convert all build scripts to Kotlin DSL (`build.gradle.kts`) and enable Kotlin support for all `apps/chat` modules.
- **Targets**: `message-server`, `websocket-server`, `system-server`, and shared `libs` (`chat-domain`, `chat-storage`).

## 2. Structural Fixes & Refactoring
- **Issue Identified**: logic mismatch in Redis Pub/Sub channels.
    - `message-server` publishes to `chat:message:sent:{channelId}`.
    - `websocket-server` subscribes to `chat:room:{channelId}` (expects prefix `chat:room:`).
- **Fix**: Standardize on `chat:room:{channelId}` for both publishing and subscribing.
- **Action**: Refactor `MessageEventPublisher` (message-server) and `RedisMessageSubscriber` (websocket-server) while converting them to Kotlin.

## 3. Push Service Integration
- **Goal**: `message-server` needs to propagate messages to `push-service`.
- **Method**: `push-service` uses Kafka. `message-server` must act as a Kafka Producer.
- **Action**: Add `spring-kafka` dependency to `message-server` and implement `KafkaMessageProducer`.

## 4. Testing
- **Goal**: Ensure reliability.
- **Method**: Add JUnit 5 + MockK tests for `MessageApplicationService` and Event Publishers.

---

# Proceeding with Step 1: Project Build Configuration
I will now convert the `build.gradle` files to `build.gradle.kts` for `message-server`, `websocket-server`, and `system-server`.
