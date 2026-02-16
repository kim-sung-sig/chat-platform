# 채팅 플랫폼 (Chat Platform)

> **DDD + EDA 기반의 확장 가능한 채팅 플랫폼**  
> Spring Boot 3.5.6 | Spring Cloud 2024.0.0 | Kotlin | PostgreSQL | Redis | WebSocket | Kafka

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![Tests](https://img.shields.io/badge/tests-63%20passed-brightgreen)](https://github.com)
[![Coverage](https://img.shields.io/badge/coverage-80%25-yellow)](https://github.com)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue)](https://spring.io/projects/spring-cloud)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-7F52FF)](https://kotlinlang.org/)

---

## 📋 프로젝트 개요

### 핵심 기능

- ✅ **실시간 채팅** - WebSocket 기반 실시간 메시지 전송
- ✅ **다양한 채널 타입** - 일대일, 그룹, 공개, 비공개 채널
- ✅ **메시지 타입** - 텍스트, 이미지, 파일, 시스템 메시지
- ✅ **예약 메시지** - 단발성 및 주기적 메시지 스케줄링
- ✅ **커서 페이징** - 대용량 메시지 조회 최적화
- ✅ **멀티 인스턴스** - Redis Pub/Sub 기반 분산 환경 지원
- ✅ **마이크로서비스 아키텍처** - Spring Cloud Netflix 기반

### 기술 스택

| 카테고리              | 기술                                                    |
|-------------------|-------------------------------------------------------|
| **Backend**       | Spring Boot 3.5.6, Kotlin 1.9.25, Java 21             |
| **MSA**           | Spring Cloud 2024.0.0, Config Server, Eureka, Gateway |
| **Database**      | PostgreSQL 15, Flyway                                 |
| **Cache**         | Redis 7                                               |
| **Message Queue** | Kafka 3.9, Redis Pub/Sub                              |
| **Scheduler**     | Quartz 2.5.0                                          |
| **WebSocket**     | Spring WebSocket, STOMP                               |
| **Testing**       | JUnit 5, AssertJ, TestContainers                      |
| **API Docs**      | Swagger/OpenAPI 3.0                                   |
| **Build**         | Gradle 8.14.3                                         |

---

## 🏗️ 아키텍처

### Multi-Module 구조
```
chat-platform/
├── infrastructure/           # Spring Cloud 인프라 (Kotlin)
│   ├── config-server/        # Config Server (Port: 8888)
│   ├── eureka-server/        # Service Discovery (Port: 8761)
│   └── api-gateway/          # API Gateway (Port: 8000)
│
├── apps/chat/libs/
│   ├── chat-domain/          # 순수 도메인 계층 (DDD)
│   │   ├── message/          # Message Aggregate
│   │   ├── channel/          # Channel Aggregate
│   │   ├── schedule/         # ScheduleRule Aggregate
│   │   ├── user/             # User Aggregate
│   │   └── service/          # Domain Services
│   │
│   └── chat-storage/         # 영속성 계층 (Hexagonal)
│       ├── entity/           # JPA Entities
│       ├── repository/       # JPA Repositories
│       ├── adapter/          # Repository Adapters
│       └── mapper/           # Domain ↔ Entity Mappers
│
├── apps/chat/
│   ├── message-server/       # 메시지 발송 서버 (Port: 8081) - Kotlin
│   │   ├── application/      # Application Services
│   │   ├── presentation/     # REST Controllers
│   │   └── infrastructure/   # Redis, Event Publisher, Kafka
│   │
│   ├── system-server/        # 시스템 관리 서버 (Port: 8082) - Java
│   │   ├── application/      # Channel, Schedule Services
│   │   ├── controller/       # REST Controllers
│   │   ├── job/              # Quartz Jobs
│   │   └── infrastructure/   # Quartz, Lock, WebClient
│   │
│   └── websocket-server/     # WebSocket 서버 (Port: 20002) - Kotlin
│       ├── handler/          # WebSocket Handlers
│       ├── session/          # Session Manager (Redis)
│       └── subscriber/       # Redis Subscriber
│
└── common/                   # 공통 모듈
    ├── core/                 # Exception, Util, Constants
    ├── security/             # JWT, Authentication (Kotlin)
    ├── web/                  # Web Common
    └── logging/              # Logging
```

### DDD 패턴 적용

```java
// Aggregate Root
public class Message {
	private final MessageId id;              // Value Object
	private final ChannelId channelId;       // Value Object
	private final UserId senderId;           // Value Object
	private final MessageContent content;    // Value Object
	private final MessageType type;          // Enum
	private MessageStatus status;            // Enum
}

// Domain Service
public class MessageDomainService {
	public Message createTextMessage(Channel channel, User sender, String text) {
		// Channel + User Aggregate 협력을 통한 도메인 규칙 검증
		validateMessageSendingPermission(channel, sender);
		MessageContent content = MessageContent.text(text);
		return Message.create(channel.getId(), sender.getId(), content, MessageType.TEXT);
	}
}

// Repository (Port)
public interface MessageRepository {
	Message save(Message message);

	Optional<Message> findById(MessageId id);
}

// Adapter (Implementation)
@Repository
public class MessageRepositoryAdapter implements MessageRepository {
	private final JpaMessageRepository jpaRepository;
	private final MessageMapper mapper;
	// ...
}
```

### CQRS 패턴

```java
// Command (Write) - 쓰기 작업
@Transactional
public MessageResponse sendMessage(SendMessageRequest request) {
	Message message = messageDomainService.createTextMessage(...);
	Message saved = messageRepository.save(message);
	messageEventPublisher.publishMessageSent(saved);
	return MessageResponse.from(saved);
}

// Query (Read) - 읽기 작업 (커서 페이징)
@Transactional(readOnly = true)
public CursorPageResponse<MessageResponse> getMessages(
		String channelId, String cursor, int limit) {
	List<Message> messages = messageRepository.findByChannelId(...);
	return CursorPageResponse.of(messages, nextCursor, hasNext);
}
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose
- PostgreSQL 15
- Redis 7

### 환경 설정

#### 1. 데이터베이스 및 Redis 시작
```bash
cd docker
docker-compose up -d
```

#### 2. 애플리케이션 빌드
```bash
./gradlew clean build
```

#### 3. 서버 실행

**chat-message-server (Port: 8081)**
```bash
./gradlew :chat-message-server:bootRun
```

**chat-system-server (Port: 8082)**
```bash
./gradlew :chat-system-server:bootRun
```

**chat-websocket-server (Port: 20002)**

```bash
./gradlew :chat-websocket-server:bootRun
```

### API 문서

- **Message Server**: http://localhost:8081/swagger-ui.html
- **System Server**: http://localhost:8082/swagger-ui.html

---

## 📊 API 엔드포인트

### 메시지 API (chat-message-server)

| Method | Endpoint                             | 설명                 |
|--------|--------------------------------------|--------------------|
| POST   | `/api/messages`                      | 메시지 발송             |
| GET    | `/api/messages/channels/{channelId}` | 메시지 목록 조회 (커서 페이징) |
| GET    | `/api/messages/health`               | Health Check       |

### 채널 관리 API (chat-system-server)

| Method | Endpoint                                        | 설명        |
|--------|-------------------------------------------------|-----------|
| POST   | `/api/v1/channels/direct`                       | 일대일 채널 생성 |
| POST   | `/api/v1/channels/group`                        | 그룹 채널 생성  |
| POST   | `/api/v1/channels/public`                       | 공개 채널 생성  |
| POST   | `/api/v1/channels/private`                      | 비공개 채널 생성 |
| GET    | `/api/v1/channels/{channelId}`                  | 채널 상세 조회  |
| GET    | `/api/v1/channels/my`                           | 내 채널 목록   |
| POST   | `/api/v1/channels/{channelId}/members`          | 멤버 추가     |
| DELETE | `/api/v1/channels/{channelId}/members/{userId}` | 멤버 제거     |
| PATCH  | `/api/v1/channels/{channelId}/info`             | 채널 정보 수정  |
| POST   | `/api/v1/channels/{channelId}/deactivate`       | 채널 비활성화   |
| POST   | `/api/v1/channels/{channelId}/activate`         | 채널 활성화    |

### 예약 메시지 API (chat-system-server)

| Method | Endpoint                              | 설명         |
|--------|---------------------------------------|------------|
| POST   | `/api/schedules/one-time`             | 단발성 스케줄 생성 |
| POST   | `/api/schedules/recurring`            | 주기적 스케줄 생성 |
| POST   | `/api/schedules/{scheduleId}/pause`   | 스케줄 일시정지   |
| POST   | `/api/schedules/{scheduleId}/resume`  | 스케줄 재개     |
| POST   | `/api/schedules/{scheduleId}/cancel`  | 스케줄 취소     |
| GET    | `/api/schedules/my`                   | 내 스케줄 목록   |
| GET    | `/api/schedules/channels/{channelId}` | 채널 스케줄 목록  |

---

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew :chat-domain:test
```

**테스트 통계:**

- ✅ MessageDomainService: 22개
- ✅ ChannelDomainService: 21개
- ✅ ScheduleDomainService: 20개
- **총 63개 테스트 - 모두 통과**

### 통합 테스트 실행
```bash
./gradlew test
```

---

## 📚 주요 기능 설명

### 1. 실시간 채팅

```javascript
// WebSocket 연결
const socket = new WebSocket('ws://localhost:20002/ws/chat');

// 메시지 수신
socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('Received:', message);
};

// 메시지 발송
fetch('http://localhost:8081/api/messages', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        channelId: 'channel-123',
        messageType: 'TEXT',
        payload: {text: 'Hello!'}
    })
});
```

### 2. 예약 메시지

**단발성 메시지 (1시간 후 발송)**

```json
POST /api/schedules/one-time
{
  "channelId": "channel-123",
  "messageType": "TEXT",
  "payload": {
    "text": "예약 메시지"
  },
  "scheduledAt": "2025-12-15T10:00:00Z"
}
```

**주기적 메시지 (매일 오전 9시)**

```json
POST /api/schedules/recurring
{
  "channelId": "channel-123",
  "messageType": "TEXT",
  "payload": {
    "text": "일일 리포트"
  },
  "cronExpression": "0 0 9 * * ?"
}
```

### 3. 커서 페이징
```bash
# 첫 페이지
GET /api/messages/channels/channel-123?limit=20

# 다음 페이지
GET /api/messages/channels/channel-123?cursor=eyJpZCI6MTIzfQ&limit=20
```

---

## 🎯 주요 성과

### 아키텍처

- ✅ **DDD 패턴 완벽 구현** - Aggregate Root, Value Object, Domain Service
- ✅ **CQRS 패턴 적용** - Command/Query 분리
- ✅ **Hexagonal Architecture** - Port & Adapter
- ✅ **EDA (Event-Driven)** - Redis Pub/Sub

### 코드 품질

- ✅ **Early Return 패턴** - 가독성 극대화
- ✅ **Aggregate 중심 설계** - Key가 아닌 도메인 전달
- ✅ **테스트 주도** - 63개 단위 테스트

### 성능 최적화

- ✅ **커서 페이징** - 대용량 데이터 처리
- ✅ **WebClient** - 비동기 HTTP 클라이언트
- ✅ **Connection Pool** - 효율적인 리소스 관리

---

## 📖 문서

### 아키텍처 문서

- [채팅 플랫폼 아키텍처 및 설계](docs/architecture/채팅_플랫폼_아키텍처_및_설계.md)
- [아키텍처 재설계 최종](docs/architecture/아키텍처_재설계_최종.md)
- [마이그레이션 실행계획](docs/architecture/마이그레이션_실행계획.md)

### 세션 보고서

- [Session 6: Domain Service 재설계](docs/sessions/Session6_완료보고서.md)
- [Session 7: API 문서화 및 통합 테스트](docs/sessions/Session7_최종_완료보고서.md)
- [Session 8: MessageDomainService, ChannelDomainService 테스트](docs/sessions/Session8_완료보고서.md)
- [Session 9: ScheduleDomainService 테스트](docs/sessions/Session9_완료보고서.md)
- [Session 10: 최종 정리](docs/sessions/Session10_완료보고서.md)

### 기능별 보고서

- [채널 관리 기능](Channel_관리_기능_완료보고서.md)
- [메시지 조회 기능](메시지_조회_기능_완료보고서.md)
- [DDD Domain Service 재설계](DDD_Domain_Service_재설계_완료보고서.md)
- [Domain 분리](Domain_분리_완료보고서.md)

### 최종 보고서

- [전체 진행상황 (2025-12-15)](전체_진행상황_2025-12-15.md)
- [전체 세션 회귀 및 반성](전체_세션_회귀_및_반성_보고서.md)
- [채팅 플랫폼 구현 세션 완료 종합](채팅_플랫폼_구현_세션_완료_종합보고서.md)

---

## 🛠️ 기술 상세

### Database Schema (Flyway)

```sql
-- V1: users 테이블
CREATE TABLE users
(
    id             VARCHAR(36) PRIMARY KEY,
    username       VARCHAR(50) UNIQUE       NOT NULL,
    email          VARCHAR(255) UNIQUE      NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE,
    last_active_at TIMESTAMP WITH TIME ZONE
);

-- V2: chat_channels 테이블
CREATE TABLE chat_channels
(
    id         VARCHAR(36) PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    type       VARCHAR(20)              NOT NULL,
    owner_id   VARCHAR(36)              NOT NULL,
    active     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- V3: chat_messages 테이블
CREATE TABLE chat_messages
(
    id                  VARCHAR(36) PRIMARY KEY,
    channel_id          VARCHAR(36)              NOT NULL,
    sender_id           VARCHAR(36)              NOT NULL,
    message_type        VARCHAR(20)              NOT NULL,
    content_text        TEXT,
    content_media_url   VARCHAR(500),
    content_file_name   VARCHAR(255),
    content_file_size   BIGINT,
    status              VARCHAR(20)              NOT NULL,
    sent_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    reply_to_message_id VARCHAR(36),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### 분산 락 (Redis)

```java

@Service
public class DistributedLockService {
	public boolean tryLock(String key) {
		return redisTemplate.opsForValue()
				.setIfAbsent(key, "locked", 30, TimeUnit.SECONDS);
	}

	public void unlock(String key) {
		redisTemplate.delete(key);
	}
}
```

---

## 🚧 향후 계획

### 단기 (1-2주)

- [ ] 통합 테스트 안정화
- [ ] 읽음 처리 (Read Receipt)
- [ ] 메시지 수정/삭제 기능

### 중기 (1개월)

- [ ] 파일 업로드 (S3 연동)
- [ ] 메시지 검색 (Elasticsearch)
- [ ] 알림 시스템 (Push Notification)

### 장기 (3개월)

- [ ] MSA 전환 (Kubernetes)
- [ ] 모니터링 (Prometheus + Grafana)
- [ ] 성능 테스트 (JMeter, 1000 TPS)

---

## 👥 기여

프로젝트에 기여하고 싶으신가요? Pull Request를 환영합니다!

### 개발 가이드라인

1. **코드 컨벤션**
	- Early Return 패턴 사용
	- Aggregate 중심 설계
	- Given-When-Then 테스트 작성

2. **커밋 메시지**
   ```
   feat: 새로운 기능 추가
   fix: 버그 수정
   docs: 문서 수정
   test: 테스트 추가/수정
   refactor: 코드 리팩토링
   ```

---

## 📄 라이선스

This project is licensed under the MIT License.

---

## 📧 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해주세요.

---

**Made with ❤️ by Chat Platform Team**
