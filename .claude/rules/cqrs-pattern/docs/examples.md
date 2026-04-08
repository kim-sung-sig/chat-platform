# CQRS Pattern — Examples

## Full CommandService (with multiple operations)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository repo;
    private final PasswordEncryptor encryptor;
    private final DomainEventPublisher events;

    public User register(String email, String rawPassword) {
        if (repo.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
        User user = User.create(email, encryptor.hash(rawPassword));
        User saved = repo.save(user);
        events.publish(new UserCreatedEvent(saved.getId(), saved.getEmail(), Instant.now()));
        return saved;
    }

    public User updateProfile(Long userId, String displayName) {
        User user = repo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateDisplayName(displayName);
        return repo.save(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = repo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        if (!encryptor.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidCredentialException("Wrong password");
        }
        user.changePassword(encryptor.hash(newPassword));
        repo.save(user);
        events.publish(new PasswordChangedEvent(userId, Instant.now()));
    }
}
```

## Full QueryService (with search & pagination)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository repo;

    public UserDetail getById(Long id) {
        return repo.findById(id)
            .map(UserDetail::from)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public CursorPageResult<UserSummary> listActiveUsers(Optional<Long> cursor, int pageSize) {
        List<User> users = cursor
            .map(c -> repo.findActiveUsersBefore(c, pageSize + 1))
            .orElseGet(() -> repo.findNewestActiveUsers(pageSize + 1));

        boolean hasMore = users.size() > pageSize;
        List<UserSummary> page = users.stream()
            .limit(pageSize)
            .map(UserSummary::from)
            .toList();
        Long nextCursor = hasMore ? users.get(pageSize - 1).getId() : null;
        return new CursorPageResult<>(page, nextCursor, hasMore);
    }

    public List<UserSummary> searchByName(String keyword) {
        return repo.searchByDisplayName(keyword).stream()
            .map(UserSummary::from)
            .toList();
    }
}
```

## Controller wiring Command + Query

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        User user = commandService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetail> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<CursorPageResult<UserSummary>> list(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(queryService.listActiveUsers(Optional.ofNullable(cursor), size));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        User updated = commandService.updateProfile(id, request.displayName());
        return ResponseEntity.ok(UserResponse.from(updated));
    }
}
```

## Cursor Pagination Helper (Reusable)

```java
public record CursorPageResult<T>(
    List<T> content,
    Long nextCursor,
    boolean hasMore
) {
    public static <T> CursorPageResult<T> of(
        List<T> fetched,
        int pageSize,
        Function<T, Long> cursorExtractor
    ) {
        boolean hasMore = fetched.size() > pageSize;
        List<T> page = fetched.stream().limit(pageSize).toList();
        Long cursor = hasMore ? cursorExtractor.apply(page.getLast()) : null;
        return new CursorPageResult<>(page, cursor, hasMore);
    }
}
```