---
name: Testing Conventions (JUnit 5, Nested, Korean DisplayNames)
description: Use @Nested for organization, Korean @DisplayName, mock externals
scope: All tests
applies-to: Unit, integration, API tests
version: 1.0
triggers: Any test implementation
---

# Rule: Testing Conventions

## Core Principle

**Use JUnit 5 @Nested. Mock externals, never mock domain. Korean @DisplayName.**

## Test Structure (@Nested)

```java
@DisplayName("MessageCreationService")
class MessageCreationServiceTest {
    
    @Mock private MessageRepository repository;
    @InjectMocks private MessageCreationService service;
    
    @Nested
    @DisplayName("send 메서드")
    class SendMethod {
        
        @Nested
        @DisplayName("정상 케이스")
        class HappyPath {
            
            @Test
            @DisplayName("메시지를 생성하고 저장한다")
            void createsAndSaves() {
                // Given
                Message msg = Message.create(1L, 1L, "Hello");
                given(repository.save(any())).willReturn(msg);
                
                // When
                Message result = service.send(1L, 1L, "Hello");
                
                // Then
                assertThat(result).isNotNull();
                then(repository).should().save(any());
            }
        }
        
        @Nested
        @DisplayName("경계 케이스")
        class Boundary {
            
            @Test
            @DisplayName("빈 메시지는 거부한다")
            void rejectsBlank() {
                assertThatThrownBy(() -> service.send(1L, 1L, "  "))
                    .isInstanceOf(InvalidMessageException.class);
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class Failure {
            
            @Test
            @DisplayName("저장소 오류를 전파한다")
            void propagatesError() {
                given(repository.save(any()))
                    .willThrow(new DataAccessException("DB down"));
                
                assertThatThrownBy(() -> service.send(1L, 1L, "Test"))
                    .isInstanceOf(DataAccessException.class);
            }
        }
    }
}
```

## Test Levels

| Level | Tools | Coverage |
|-------|-------|----------|
| **Unit** | JUnit 5, Mockito | 80%+ |
| **Integration** | TestContainers, SpringBootTest | 50%+ |
| **API** | MockMvc | Contract only |

## Mocking Rules

| What | Mock? | Why |
|------|-------|-----|
| Repository | ✅ Yes | Domain doesn't care about persistence |
| External API | ✅ Yes | Avoid network calls |
| Event bus | ✅ Yes | Events are side effects |
| Domain model | ❌ No | Test business logic directly |

## Integration Test (TestContainers)

```java
@DisplayName("MessageCommandService 통합 테스트")
@SpringBootTest
@Testcontainers
class MessageCommandServiceIntegrationTest {
    
    @Autowired private MessageCommandService commandService;
    @Autowired private MessageRepository repository;
    @MockBean private DomainEventPublisher eventPublisher;
    
    @Test
    @DisplayName("메시지를 DB에 저장하고 이벤트를 발행한다")
    @Transactional
    void savesAndPublishes() {
        Message msg = commandService.send(1L, 1L, "Hello");
        
        Message persisted = repository.findById(msg.getId()).orElseThrow();
        assertThat(persisted.getContent()).isEqualTo("Hello");
        
        then(eventPublisher).should().publish(any(MessageCreatedEvent.class));
    }
}
```

## API Test (MockMvc)

```java
@DisplayName("MessageController API")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class MessageControllerApiTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private MessageCommandService commandService;
    
    @Test
    @DisplayName("유효한 요청으로 201을 반환한다")
    void returns201() throws Exception {
        Message saved = Message.create(1L, 1L, "Test");
        given(commandService.send(any(), any(), any())).willReturn(saved);
        
        mockMvc.perform(post("/api/messages")
            .contentType(APPLICATION_JSON)
            .content("""{"roomId": 1, "content": "Test"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Test"));
    }
}
```

## Test Fixtures (Builder)

```java
public class MessageFixture {
    
    public static MessageBuilder aMessage() {
        return new MessageBuilder();
    }
    
    public static class MessageBuilder {
        private Long roomId = 1L;
        private String content = "Hello";
        
        public MessageBuilder withContent(String content) {
            this.content = content;
            return this;
        }
        
        public Message build() {
            return Message.create(roomId, 1L, content);
        }
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| **@Nested** | One per method, HappyPath/Boundary/Failure groups |
| **@DisplayName** | Korean, descriptive |
| **Given-When-Then** | Organize test body |
| **Mock externals** | Repository, API, event bus (NOT domain) |
| **Use fixtures** | Builder pattern for test data |
| **Coverage** | Domain ≥80%, API tests for contract |
| **@Transactional** | Rollback changes after integration test |

## Checklist

- [ ] Unit tests for domain models (80%+)
- [ ] Integration tests with TestContainers
- [ ] API tests (HTTP contract)
- [ ] Mock only external dependencies
- [ ] @DisplayName with Korean
- [ ] @Nested organization
- [ ] Fixtures for test data

---

Examples: `testing-conventions/docs/`
