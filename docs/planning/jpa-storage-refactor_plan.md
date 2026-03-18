# Planning Document — `jpa-storage-refactor`

## 1. Problem Statement

### What problem are we solving?

The `apps/chat/libs/chat-storage` library contains six JPA entities and six repositories that deviate from the project's established JPA conventions in several systematic ways:

1. **No `BaseEntity`**: Every entity independently declares `createdAt`, `updatedAt`, `@PrePersist`, and `@PreUpdate`. This logic is duplicated across `ChatChannelEntity`, `ChatChannelMetadataEntity`, `ChatFriendshipEntity`, and `UserEntity`, with inconsistent null-guard patterns (some use `Instant.EPOCH` guards, others a plain null check, `ChatMessageEntity` lacks `updatedAt` entirely).

2. **`@Builder` on entities**: Five of six entities use `@Builder` + `@AllArgsConstructor(access = PRIVATE)`. This exposes a public Lombok builder as the construction API, leaking internal field names and allowing callers to bypass domain invariants (e.g., setting `messageStatus = SENT` directly). Only `ChatChannelMemberEntity` has a static factory (`of(...)`), but it delegates internally to the Lombok builder it should replace.

3. **Cohesive field groups not extracted**: `ChatMessageEntity` has five `content_*` columns (`contentText`, `contentMediaUrl`, `contentFileName`, `contentFileSize`, `contentMimeType`) that logically form a `MessageContent` value object. No `@Embeddable` exists for them. Similarly, `ChatChannelMetadataEntity` has a `lastRead*` field group (`lastReadMessageId`, `lastReadAt`) that could form a `LastReadPointer` value object.

4. **Unnecessary JPQL SELECT queries**: `JpaMessageRepository` has four plain `SELECT` queries that can be expressed exactly with Spring Data named methods. `JpaFriendshipRepository` has five SELECT queries expressible as named methods or by adding simple sort parameters. `JpaChannelMetadataRepository` has three SELECT queries replaceable with named methods. `JpaChannelRepository` has one simple SELECT (`findPublicChannels`) replaceable with a named method; the multi-join filter queries are legitimately complex and should remain as `@Query`.

### Why now?

The library is a shared dependency consumed by both `chat-server` and `websocket-server`. As more bounded contexts are added (voice rooms, approval system), the duplicated lifecycle code and builder-based construction surface will spread further. Aligning with the JPA conventions now constrains the growth surface and reduces cognitive overhead for the developer and reviewer agents in subsequent SDLC cycles.

---

## 2. Goals / Non-Goals

### Goals

- Introduce a `BaseEntity` mapped superclass that consolidates `id`, `createdAt`, `updatedAt`, `@PrePersist`, and `@PreUpdate`.
- Remove `@Builder` / `@AllArgsConstructor(access = PRIVATE)` from all entities; replace with a `public static XxxEntity create(...)` factory method that enforces domain invariants at construction time.
- Extract `contentText + contentMediaUrl + contentFileName + contentFileSize + contentMimeType` from `ChatMessageEntity` into a `MessageContent @Embeddable`.
- Evaluate and extract `lastReadMessageId + lastReadAt` from `ChatChannelMetadataEntity` into a `LastReadPointer @Embeddable`.
- Replace all plain SELECT `@Query` methods in repositories with Spring Data named methods or `@EntityGraph` equivalents, wherever expressible without loss of semantics.
- Retain legitimately complex multi-join and `@Modifying` bulk UPDATE/DELETE queries as `@Query`.
- Ensure all existing business methods on entities (`markAsSent`, `decrementUnread`, `accept`, `deactivate`, etc.) are preserved without change.
- All existing tests continue to pass; add or update unit tests for affected entities.

### Non-Goals

- Schema migration (DDL): column names are unchanged; `@Embeddable` fields are mapped with `@AttributeOverride` to the same column names — no Flyway migration needed.
- Changing the `ChatChannelMemberEntity.id` primary key strategy (`GenerationType.IDENTITY`): it differs from other entities which use `String` UUIDs; this is intentional and out of scope.
- Changing the `ChatMessageEntity.createdAt` semantics (it is not an `updatedAt` field; the entity is immutable except via explicit business methods).
- Refactoring the application or domain layers in `chat-server` or `websocket-server` — only the `chat-storage` library is in scope.
- Introducing Querydsl, jOOQ, or any new query framework.
- Changing the `UserEntity` structure beyond applying `BaseEntity` and the static factory.

---

## 3. Target Users / Stakeholders

- **Developer agent**: primary implementer; consumes this plan to produce the refactored code.
- **Reviewer agent**: evaluates the resulting diff against the JPA conventions checklist.
- **QA agent**: runs the existing test suite and verifies all passing tests remain green.
- **Other bounded-context developers**: consumers of `chat-storage` entities and repositories; the static factory API change is the only breaking surface.

---

## 4. Requirements

### Functional Requirements

**FR-1 — BaseEntity**
- Create `com.example.chat.storage.domain.entity.BaseEntity` as a `@MappedSuperclass`.
- Fields: `createdAt: Instant` (`@Column(name = "created_at", nullable = false, updatable = false)`), `updatedAt: Instant` (`@Column(name = "updated_at", nullable = false)`).
- `@PrePersist`: set both to `Instant.now()` if null.
- `@PreUpdate`: set `updatedAt = Instant.now()`.
- All entities that currently declare `createdAt` and/or `updatedAt` extend `BaseEntity`: `ChatChannelEntity`, `ChatChannelMetadataEntity`, `ChatFriendshipEntity`, `UserEntity`.
- `ChatMessageEntity` is a special case: it has `createdAt` but no `updatedAt`. It extends `BaseEntity` but overrides `updatedAt` behaviour — see FR-1a.
- `ChatChannelMemberEntity` uses `joinedAt` in place of `createdAt`; it does NOT extend `BaseEntity`. Its `@PrePersist` guard is retained as-is.

**FR-1a — ChatMessageEntity and BaseEntity**
- `ChatMessageEntity` has a write-once `createdAt` with no `updatedAt`. Option A: extend `BaseEntity` and accept a no-op `updatedAt` column (requires a new column or nullable override). Option B: do not extend `BaseEntity` but extract only `@PrePersist`/`@PreUpdate` behaviour into it.
- **Decision**: Extend `BaseEntity` with `updatedAt` mapped to a new `updated_at` column (`nullable = true`) so that the existing DDL is backwards-compatible without a hard migration. The `updatedAt` column will be populated by `@PreUpdate` on status transitions. This avoids a schema change on `created_at` and adds observability.
- If the team decides against adding a new column, `ChatMessageEntity` stays independent and extends nothing — this is the safer no-DDL-change path. **Default: `ChatMessageEntity` does NOT extend `BaseEntity`; it retains its own `@PrePersist` and removes only `@Builder`.**

**FR-2 — Static Factory Methods**
- Remove `@Builder` and `@AllArgsConstructor(access = PRIVATE)` from all entities.
- Add `public static XxxEntity create(...)` with all mandatory construction parameters as arguments.
- The factory sets `messageStatus`, `active`, `notificationEnabled`, `favorite`, `pinned`, `unreadCount` defaults explicitly in the method body.
- `ChatChannelMemberEntity.of(String channelId, String userId)` is renamed to `create(...)` for consistency; the old `of` may be kept as a deprecated alias if callers in `chat-server`/`websocket-server` use it — verify via grep before removing.

**FR-3 — MessageContent @Embeddable**
- Create `com.example.chat.storage.domain.entity.MessageContent` as an `@Embeddable`.
- Fields: `contentText`, `contentMediaUrl`, `contentFileName`, `contentFileSize`, `contentMimeType` with identical `@Column` definitions.
- Replace the five flat fields in `ChatMessageEntity` with `@Embedded private MessageContent content`.
- Getters for individual content fields are delegated through `getContent()` or direct delegation methods to maintain backward compatibility with any caller using `entity.getContentText()`.

**FR-4 — LastReadPointer @Embeddable (optional / conditional)**
- Create `com.example.chat.storage.domain.entity.LastReadPointer` as an `@Embeddable` containing `lastReadMessageId` and `lastReadAt`.
- Replace in `ChatChannelMetadataEntity` with `@Embedded private LastReadPointer lastRead`.
- The `markAsRead(String messageId)` business method is updated to delegate to the embeddable.
- This is marked conditional: implement only if the developer judges the field group has sufficient cohesion and the `markAsRead` method delegates cleanly. If the delegation makes the business method less readable, skip and note in the implementation commit message.

**FR-5 — Repository: Replace SELECT @Query with Named Methods**

| Repository | Current `@Query` method | Replacement |
|---|---|---|
| `JpaMessageRepository` | `findByChannelIdBeforeCursor` | `findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc` |
| `JpaMessageRepository` | `findByChannelIdLatest` | `findByChannelIdOrderByCreatedAtDesc` |
| `JpaMessageRepository` | `findBySenderIdBeforeCursor` | `findBySenderIdAndCreatedAtBeforeOrderByCreatedAtDesc` |
| `JpaMessageRepository` | `findBySenderIdLatest` | `findBySenderIdOrderByCreatedAtDesc` |
| `JpaFriendshipRepository` | `findAcceptedFriendsByUserId` | `findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc` |
| `JpaFriendshipRepository` | `findPendingRequestsByFriendId` | `findByFriendIdAndStatusOrderByCreatedAtDesc` |
| `JpaFriendshipRepository` | `findPendingRequestsByUserId` | `findByUserIdAndStatusOrderByCreatedAtDesc` |
| `JpaFriendshipRepository` | `findBlockedByUserId` | `findByUserIdAndStatusOrderByUpdatedAtDesc` |
| `JpaFriendshipRepository` | `findFavoritesByUserId` | `findByUserIdAndStatusAndFavoriteTrueOrderByUpdatedAtDesc` |
| `JpaChannelMetadataRepository` | `findFavoritesByUserId` | `findByUserIdAndFavoriteTrueOrderByLastActivityAtDesc` |
| `JpaChannelMetadataRepository` | `findPinnedByUserId` | `findByUserIdAndPinnedTrueOrderByLastActivityAtDesc` |
| `JpaChannelMetadataRepository` | `findWithUnreadByUserId` | `findByUserIdAndUnreadCountGreaterThanOrderByLastActivityAtDesc` |
| `JpaChannelRepository` | `findPublicChannels` | `findByChannelTypeAndActiveTrue(ChannelType.PUBLIC)` — named method with enum param |

- `JpaChannelMetadataRepository.findByChannelIdsAndUserId`: replace with `findByChannelIdInAndUserId(List<String>, String)` (named method).
- `JpaMessageRepository.findLastMessagesByChannelIds`: contains a correlated subquery; keep as `@Query`.
- `JpaChannelRepository.findByMemberIdWithFilters`, `findByMemberIdWithAllFilters`, `findChannelIdsByMemberId`: multi-join queries across three entities with conditional predicates; keep as `@Query`.
- `JpaFriendshipRepository.existsMutualFriendship`: contains an OR condition across reversed user/friend IDs; keep as `@Query`.
- `JpaChannelMetadataRepository.bulkIncrementUnreadCount`, `updateLastActivity`: bulk `@Modifying` UPDATE; keep as `@Query`.
- `JpaMessageRepository.bulkDecrementUnreadCountBeforeCursor`, `bulkDecrementUnreadCountAfterCursor`: bulk `@Modifying` UPDATE; keep as `@Query`.

**FR-6 — Caller Impact**
- All call sites in `chat-server` and `websocket-server` that use the old repository method names must be updated to the new named method signatures.
- All call sites using `XxxEntity.builder()` must be updated to `XxxEntity.create(...)`.

### Non-Functional Requirements

- **NFR-1**: No DDL change to existing columns. `@Embeddable` fields use `@AttributeOverride` to preserve column names.
- **NFR-2**: The refactor is a pure library change; no REST API or Kafka message format changes.
- **NFR-3**: All existing unit and integration tests pass after the refactor. New tests are added for the `BaseEntity` lifecycle and `MessageContent` embeddable.
- **NFR-4**: No new framework dependencies introduced.

---

## 5. Domain Knowledge (Deep Knowledge)

### Entities and their current deviations

| Entity | Has BaseEntity | Has @Builder | Embeddable candidates | JPQL SELECT queries in repo |
|---|---|---|---|---|
| `ChatMessageEntity` | No (own `@PrePersist` only) | Yes | `MessageContent` (5 fields) | 4 (all replaceable) |
| `ChatChannelEntity` | No (own `@PrePersist`+`@PreUpdate`) | Yes | None | 1 replaceable, 3 complex keep |
| `ChatChannelMemberEntity` | No (`joinedAt` only) | Yes (+ `of()`) | None | 0 |
| `ChatChannelMetadataEntity` | No (own `@PrePersist`+`@PreUpdate`) | Yes | `LastReadPointer` (2 fields, conditional) | 3 replaceable, 2 bulk keep |
| `ChatFriendshipEntity` | No (own `@PrePersist`+`@PreUpdate`) | Yes | None | 5 replaceable, 1 keep |
| `UserEntity` | No (own `@PrePersist`+`@PreUpdate`) | Yes | None | 0 |

### @PrePersist inconsistency details

- `ChatChannelEntity`: guards with `== null || .equals(EPOCH)` — inconsistent with `ChatChannelMetadataEntity` and `ChatFriendshipEntity` which use plain null checks.
- `ChatMessageEntity`: same `EPOCH` guard pattern but no `updatedAt`.
- `UserEntity`: same `EPOCH` guard on `createdAt`, no guard on `updatedAt` (it is written only in `@PreUpdate`).
- `BaseEntity` `@PrePersist` must use plain null check for correctness (no EPOCH special-casing).

### Builder.Default fields that must be handled in static factories

| Entity | Field | Default |
|---|---|---|
| `ChatMessageEntity` | `messageStatus` | `MessageStatus.PENDING` |
| `ChatMessageEntity` | `createdAt` | `Instant.now()` (handled by `@PrePersist`) |
| `ChatMessageEntity` | `unreadCount` | `0` |
| `ChatChannelEntity` | `active` | `true` |
| `ChatChannelEntity` | `createdAt` / `updatedAt` | `Instant.now()` (handled by `BaseEntity`) |
| `ChatChannelMemberEntity` | `joinedAt` | `Instant.now()` (handled by `@PrePersist`) |
| `ChatChannelMetadataEntity` | `notificationEnabled` | `true` |
| `ChatChannelMetadataEntity` | `favorite` | `false` |
| `ChatChannelMetadataEntity` | `pinned` | `false` |
| `ChatChannelMetadataEntity` | `unreadCount` | `0` |
| `ChatFriendshipEntity` | `favorite` | `false` |
| `UserEntity` | `status` | `UserStatus.ACTIVE` |
| `UserEntity` | `createdAt` | `Instant.now()` (handled by `BaseEntity`) |

### Glossary

| Term | Definition |
|---|---|
| `BaseEntity` | `@MappedSuperclass` providing `createdAt`, `updatedAt`, and JPA lifecycle callbacks |
| `@Embeddable` | JPA annotation marking a class whose fields are mapped to columns of the owning entity's table |
| `MessageContent` | Value object encapsulating the five `content_*` columns of `chat_messages` |
| `LastReadPointer` | Value object encapsulating `lastReadMessageId` + `lastReadAt` in `chat_channel_metadata` |
| Static factory method | `public static XxxEntity create(...)` — the only allowed construction path post-refactor |
| Named method | Spring Data repository method whose query is derived from the method name |
| Cursor pagination | Query pattern: `findByXxxAndCreatedAtBefore(cursor, Pageable)` — no offset |

---

## 6. Domain Model & Boundaries

### Bounded Context

`chat-storage` is a shared infrastructure library, not a bounded context itself. It is consumed by:
- `chat-server` (write path: command services; read path: query services)
- `websocket-server` (read path: subscription events)

### Aggregate Boundaries (as expressed by entities)

| Root | Entities |
|---|---|
| Channel | `ChatChannelEntity`, `ChatChannelMemberEntity`, `ChatChannelMetadataEntity` |
| Message | `ChatMessageEntity` |
| Friendship | `ChatFriendshipEntity` |
| User | `UserEntity` |

### Key Value Objects (new)

- `MessageContent`: `contentText`, `contentMediaUrl`, `contentFileName`, `contentFileSize`, `contentMimeType`
- `LastReadPointer` (conditional): `lastReadMessageId`, `lastReadAt`

### Ubiquitous Language

- `create(...)` — the only way to construct a new entity instance
- `BaseEntity` — the common lifecycle superclass
- `MessageContent` — what a chat message says or carries
- `LastReadPointer` — a member's progress marker within a channel

---

## 7. Data & Interfaces

### Inputs (construction)

After refactor, all entity construction occurs through static `create(...)` factory methods:

```
ChatMessageEntity.create(id, channelId, senderId, messageType, content)
ChatChannelEntity.create(id, name, description, channelType, ownerId)
ChatChannelMemberEntity.create(channelId, userId)
ChatChannelMetadataEntity.create(id, channelId, userId)
ChatFriendshipEntity.create(id, userId, friendId, status)
UserEntity.create(id, username, email)
```

### Outputs (repository method signatures — changed names)

Key signature changes in repositories:

```java
// JpaMessageRepository — before → after
findByChannelIdBeforeCursor(channelId, cursor, pageable)
  → findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId, cursor, pageable)

findByChannelIdLatest(channelId, pageable)
  → findByChannelIdOrderByCreatedAtDesc(channelId, pageable)

findBySenderIdBeforeCursor(senderId, cursor, pageable)
  → findBySenderIdAndCreatedAtBeforeOrderByCreatedAtDesc(senderId, cursor, pageable)

findBySenderIdLatest(senderId, pageable)
  → findBySenderIdOrderByCreatedAtDesc(senderId, pageable)

// JpaFriendshipRepository — before → after
findAcceptedFriendsByUserId(userId)
  → findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc(userId, FriendshipStatus.ACCEPTED)

findPendingRequestsByFriendId(friendId)
  → findByFriendIdAndStatusOrderByCreatedAtDesc(friendId, FriendshipStatus.PENDING)

findPendingRequestsByUserId(userId)
  → findByUserIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING)

findBlockedByUserId(userId)
  → findByUserIdAndStatusOrderByUpdatedAtDesc(userId, FriendshipStatus.BLOCKED)

findFavoritesByUserId(userId)
  → findByUserIdAndStatusAndFavoriteTrueOrderByUpdatedAtDesc(userId, FriendshipStatus.ACCEPTED)

// JpaChannelMetadataRepository — before → after
findByChannelIdsAndUserId(channelIds, userId)
  → findByChannelIdInAndUserId(channelIds, userId)

findFavoritesByUserId(userId)
  → findByUserIdAndFavoriteTrueOrderByLastActivityAtDesc(userId)

findPinnedByUserId(userId)
  → findByUserIdAndPinnedTrueOrderByLastActivityAtDesc(userId)

findWithUnreadByUserId(userId)
  → findByUserIdAndUnreadCountGreaterThanOrderByLastActivityAtDesc(userId, 0)

// JpaChannelRepository — before → after
findPublicChannels()
  → findByChannelTypeAndActiveTrue(ChannelType.PUBLIC)
```

### External Systems

- No external system changes. The library is an internal JPA persistence layer.

---

## 8. Risks & Assumptions

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R-1 | `@Embeddable` column mapping breaks schema if `@AttributeOverride` names are incorrect | Medium | High | Verify column name parity in `@AttributeOverride` against current `@Column` definitions; run integration test with TestContainers |
| R-2 | Named method names become unwieldy (e.g., `findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc`) and harder to read than `@Query` | Low | Low | Accept long names as a trade-off; add Javadoc explaining intent |
| R-3 | Callers in `chat-server` or `websocket-server` use `XxxEntity.builder()` directly — missed during grep | Medium | Medium | Grep all modules for `.builder()` calls on storage entities before removing `@Builder`; fail the build if any remain |
| R-4 | `ChatMessageEntity` extending `BaseEntity` adds an `updated_at` column — potential DDL mismatch in CI with existing schema | High | High | Default decision: `ChatMessageEntity` does NOT extend `BaseEntity` — avoids any schema change risk |
| R-5 | `FriendshipStatus` enum-based named method (`findByUserIdAndStatus`) requires callers to pass the enum value — breaking API change if callers used string-based `@Query` | Low | Low | Pass `FriendshipStatus` enum directly; callers already use the enum type |
| R-6 | `findByUserIdAndUnreadCountGreaterThan(userId, 0)` generates `unread_count > 0` which equals the previous `m.unreadCount > 0` condition | Low | Low | Semantically equivalent; verify with a unit test |

### Assumptions

- A-1: No Flyway migration exists for the current schema; `@Embeddable` fields mapped to the same column names require no DDL change.
- A-2: `ChatChannelMemberEntity` deliberately uses `joinedAt` (not `createdAt`/`updatedAt`) and a surrogate `Long` PK — it is excluded from `BaseEntity`.
- A-3: The `id` field strategy differs per entity (UUID strings vs. `GenerationType.IDENTITY`). `BaseEntity` does NOT include `id` — each entity declares its own `@Id` to preserve these differences.
- A-4: `@Builder.Default` fields that carry domain defaults (e.g., `messageStatus = PENDING`) must be replicated explicitly in static factory methods.
- A-5: All callers of `ChatChannelMemberEntity.of(...)` are within `chat-server`; after rename to `create(...)`, the old `of` method may be removed.

---

## 9. Success Metrics

- All six JPA conventions checklist items from `jpa.md` pass for every entity:
  - [ ] Entity extends `BaseEntity` (or has documented exception)
  - [ ] No public default constructor
  - [ ] Static factory method present
  - [ ] `@Builder` removed from class
  - [ ] Flat primitive groups extracted to `@Embeddable` where applicable
  - [ ] No new JPQL SELECT queries — named methods or `@EntityGraph` used
- `./gradlew :apps:chat:libs:chat-storage:test` passes with zero failures.
- `./gradlew :apps:chat:chat-server:test` and `:apps:chat:websocket-server:test` pass with zero failures (caller compatibility).
- Reviewer agent scores >= 81 on the refactored diff.

---

## 10. Open Questions

- **OQ-1**: Should `BaseEntity` include the `id` field as a `String` (UUID), or should each entity continue to own its `@Id`? Recommendation: each entity owns `@Id` to accommodate `ChatChannelMemberEntity`'s `Long` PK and avoid a forced type constraint.
- **OQ-2**: Should `ChatMessageEntity` extend `BaseEntity` (adding an `updated_at` column) or remain independent? Recommendation: remain independent (no DDL change). Developer must document this exception in the class Javadoc.
- **OQ-3**: Is `LastReadPointer` (`lastReadMessageId` + `lastReadAt`) worth extracting as an embeddable? The `markAsRead` business method mutates both fields atomically, which is a good signal for cohesion. Developer to confirm during implementation.
- **OQ-4**: For `findByChannelTypeAndActiveTrue(ChannelType.PUBLIC)` — does Spring Data correctly handle a String-mapped enum column with a named method? Verify the JPQL generated uses `EnumType.STRING` binding. If not, keep the simple `@Query`.
- **OQ-5**: The `JpaFriendshipRepository.findAcceptedFriendsByUserId` orders by `favorite DESC, updatedAt DESC`. Spring Data named method `findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc` should generate this sort, but the method name is long. Confirm the generated query is correct before removing the `@Query` annotation.

---

## 11. Validation Plan

### User Acceptance Criteria

- All entities in `chat-storage` pass the `jpa.md` convention checklist.
- No `@Builder` annotation remains on any entity class.
- No `@Query` SELECT annotation remains except for legitimately complex joins or correlated subqueries (as documented in FR-5).
- `MessageContent` exists as an `@Embeddable` and is embedded in `ChatMessageEntity`.
- `BaseEntity` exists and is extended by `ChatChannelEntity`, `ChatChannelMetadataEntity`, `ChatFriendshipEntity`, and `UserEntity`.

### QA Checklist

- [ ] `./gradlew :apps:chat:libs:chat-storage:test` — zero failures
- [ ] `./gradlew :apps:chat:chat-server:test` — zero failures
- [ ] `./gradlew :apps:chat:websocket-server:test` — zero failures
- [ ] Grep for `XxxEntity.builder()` in all modules — zero results
- [ ] Grep for `@Builder` on entity classes — zero results
- [ ] Grep for `@Query.*SELECT` in repositories — only complex multi-join and correlated subquery cases remain
- [ ] `BaseEntity` `@PrePersist` sets both `createdAt` and `updatedAt`; `@PreUpdate` sets `updatedAt` — verified by a unit test
- [ ] `MessageContent` fields map to the same column names as the original flat fields — verified by `@AttributeOverride` inspection and/or integration test
- [ ] All static factory method signatures match the required construction parameters — verified by code review

### Monitoring / Observability Signals

- No runtime observability change is expected. The refactor is purely structural.
- If `BaseEntity` or `@Embeddable` introduces unexpected Hibernate behaviour (e.g., N+1 on embedded fetch, unexpected UPDATE on `updatedAt`), Zipkin traces on write paths will show additional SQL.
