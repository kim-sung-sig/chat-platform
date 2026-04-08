# Testing Conventions — Examples

## Full @Nested Unit Test

```java
@DisplayName("UserCommandService")
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordEncryptor encryptor;
    @Mock private DomainEventPublisher events;
    @InjectMocks private UserCommandService service;

    @Nested
    @DisplayName("register 메서드")
    class RegisterMethod {

        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {

            @Test
            @DisplayName("유저를 저장하고 UserCreatedEvent를 발행한다")
            void savesUserAndPublishesEvent() {
                // Given
                given(repo.existsByEmail("test@example.com")).willReturn(false);
                given(encryptor.hash("password123")).willReturn("hashed");
                User saved = User.create("test@example.com", "hashed");
                given(repo.save(any())).willReturn(saved);

                // When
                User result = service.register("test@example.com", "password123");

                // Then
                assertThat(result.getEmail()).isEqualTo("test@example.com");
                then(events).should().publish(any(UserCreatedEvent.class));
            }
        }

        @Nested
        @DisplayName("경계 케이스")
        class Boundary {

            @Test
            @DisplayName("이미 존재하는 이메일은 예외를 발생시킨다")
            void throwsOnDuplicateEmail() {
                given(repo.existsByEmail("dup@example.com")).willReturn(true);

                assertThatThrownBy(() -> service.register("dup@example.com", "password123"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("dup@example.com");
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {

            @Test
            @DisplayName("저장소 오류는 전파된다")
            void propagatesRepositoryError() {
                given(repo.existsByEmail(any())).willReturn(false);
                given(repo.save(any())).willThrow(new DataAccessException("DB down") {});

                assertThatThrownBy(() -> service.register("test@example.com", "pass"))
                    .isInstanceOf(DataAccessException.class);

                then(events).shouldHaveNoInteractions();  // No event on failure
            }
        }
    }

    @Nested
    @DisplayName("changePassword 메서드")
    class ChangePasswordMethod {

        @Test
        @DisplayName("비밀번호를 변경하고 이벤트를 발행한다")
        void changesPasswordAndPublishesEvent() {
            // Given
            User user = User.create("test@example.com", "oldHash");
            given(repo.findById(1L)).willReturn(Optional.of(user));
            given(encryptor.matches("oldPass", "oldHash")).willReturn(true);
            given(encryptor.hash("newPass")).willReturn("newHash");
            given(repo.save(any())).willReturn(user);

            // When
            service.changePassword(1L, "oldPass", "newPass");

            // Then
            then(events).should().publish(any(PasswordChangedEvent.class));
        }

        @Test
        @DisplayName("잘못된 기존 비밀번호는 예외를 발생시킨다")
        void throwsOnWrongOldPassword() {
            User user = User.create("test@example.com", "correctHash");
            given(repo.findById(1L)).willReturn(Optional.of(user));
            given(encryptor.matches("wrongPass", "correctHash")).willReturn(false);

            assertThatThrownBy(() -> service.changePassword(1L, "wrongPass", "newPass"))
                .isInstanceOf(InvalidCredentialException.class);

            then(repo).should(never()).save(any());
        }
    }
}
```

## Parameterized Test (경계값)

```java
@Nested
@DisplayName("validateContent 경계 테스트")
class ContentValidationTest {

    @ParameterizedTest(name = "content={0} → expect={2}")
    @CsvSource({
        "Hello,           정상 메시지,          true",
        "'  ',            공백만 입력,           false",
        "'',              빈 문자열,             false",
    })
    @DisplayName("다양한 content 입력값 검증")
    void validateContent(String content, String desc, boolean shouldPass) {
        if (shouldPass) {
            assertThatNoException().isThrownBy(() -> Message.create(1L, 1L, content));
        } else {
            assertThatThrownBy(() -> Message.create(1L, 1L, content))
                .isInstanceOf(InvalidMessageException.class);
        }
    }
}
```

## TestContainers 통합 테스트

```java
@DisplayName("MessageRepository 통합 테스트")
@SpringBootTest
@Testcontainers
class MessageRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MessageRepository messageRepository;

    @Nested
    @DisplayName("findMessagesBefore 메서드")
    class FindMessagesBeforeMethod {

        @Test
        @DisplayName("커서 기반으로 이전 메시지를 시간 역순으로 반환한다")
        @Transactional
        void returnsPreviousMessagesInOrder() {
            Message msg1 = messageRepository.save(Message.create(1L, 1L, "First"));
            Message msg2 = messageRepository.save(Message.create(1L, 1L, "Second"));
            Message msg3 = messageRepository.save(Message.create(1L, 1L, "Third"));

            List<Message> result = messageRepository.findMessagesBefore(1L, msg3.getId(), 10);

            assertThat(result)
                .extracting(Message::getContent)
                .containsExactly("Second", "First");
        }
    }
}
```

## Fixture Builder

```java
public class UserFixture {

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static User activeUser() {
        return aUser().build();
    }

    public static User adminUser() {
        return aUser().withRole(Role.ADMIN).build();
    }

    public static class UserBuilder {
        private String email = "test@example.com";
        private String passwordHash = "hashedPassword";
        private Role role = Role.USER;

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withRole(Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .build();
        }
    }
}
```