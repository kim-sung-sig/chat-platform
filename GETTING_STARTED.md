# 프로젝트 실행 가이드

> **채팅 플랫폼 - 로컬 환경 실행 가이드**  
> **작성일**: 2026-02-17

---

## 📋 목차

1. [사전 요구사항](#사전-요구사항)
2. [환경 설정](#환경-설정)
3. [데이터베이스 설정](#데이터베이스-설정)
4. [서버 실행](#서버-실행)
5. [API 테스트](#api-테스트)
6. [문제 해결](#문제-해결)

---

## 사전 요구사항

### 필수 소프트웨어

- **Java 21** (Temurin 권장)
- **Docker Desktop** (PostgreSQL, Redis, Kafka 실행용)
- **Gradle 8.14.3** (Wrapper 포함)
- **Git**

### 선택 사항

- **IntelliJ IDEA** (권장 IDE)
- **Postman** 또는 **cURL** (API 테스트용)
- **DBeaver** 또는 **pgAdmin** (데이터베이스 관리용)

---

## 환경 설정

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd chat-platform
```

### 2. Java 버전 확인

```bash
java -version
# 출력: openjdk version "21.0.x" ...
```

**Windows 환경**:

```powershell
$env:JAVA_HOME = "C:\Users\{username}\.jdks\temurin-21.0.7"
```

**Linux/Mac 환경**:

```bash
export JAVA_HOME=/path/to/java-21
```

---

## 데이터베이스 설정

### Docker Compose로 인프라 실행

프로젝트에 포함된 Docker Compose 파일을 사용합니다.

```bash
cd docker
docker-compose up -d
```

**실행되는 서비스**:

- PostgreSQL (Source) - Port `15432`
- PostgreSQL (Replica) - Port `15433`
- Redis - Port `16379`
- Redis Insight (UI) - Port `5540`
- Kafka - Port `19092`
- Kafka UI - Port `8089`

### 데이터베이스 접속 정보

#### PostgreSQL (Source)

```
Host: localhost
Port: 15432
Database: chat_db
Username: chat_user
Password: dev_password
```

#### PostgreSQL (Replica)

```
Host: localhost
Port: 15433
Database: chat_db
Username: chat_user
Password: dev_password
```

#### Redis

```
Host: localhost
Port: 16379
Password: dev_password
```

### Flyway Migration 자동 실행

서버 시작 시 Flyway가 자동으로 마이그레이션을 실행합니다:

- `V1__init.sql`
- `V2__...sql`
- ...
- `V7__create_friendships_table.sql` ✨ (Phase 1)
- `V8__create_channel_metadata_table.sql` ✨ (Phase 2)

### 데이터베이스 초기화 (필요 시)

```bash
# PostgreSQL 초기화
docker-compose down -v
docker-compose up -d
```

---

## 서버 실행

### 1. Gradle 빌드

```bash
# 전체 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test
```

### 2. 서버별 실행

#### chat-system-server (메인 서버)

```bash
./gradlew :apps:chat:system-server:bootRun
```

**포트**: `20001`

**확인**:

```bash
curl http://localhost:20001/actuator/health
# 출력: {"status":"UP"}
```

---

#### chat-message-server (메시지 서버)

```bash
./gradlew :apps:chat:message-server:bootRun
```

**포트**: `20002`

---

#### chat-websocket-server (WebSocket 서버)

```bash
./gradlew :apps:chat:websocket-server:bootRun
```

**포트**: `20003`

---

### 3. 멀티 서버 동시 실행

**Windows (PowerShell)**:

```powershell
# 터미널 1
./gradlew :apps:chat:system-server:bootRun

# 터미널 2 (새 창)
./gradlew :apps:chat:message-server:bootRun

# 터미널 3 (새 창)
./gradlew :apps:chat:websocket-server:bootRun
```

**Linux/Mac**:

```bash
# Background 실행
./gradlew :apps:chat:system-server:bootRun &
./gradlew :apps:chat:message-server:bootRun &
./gradlew :apps:chat:websocket-server:bootRun &
```

---

## API 테스트

### Swagger UI 접속

서버 실행 후 브라우저에서:

```
http://localhost:20001/swagger-ui.html
```

### cURL로 테스트

#### 1. 친구 요청

```bash
curl -X POST http://localhost:20001/api/friendships \
  -H "X-User-Id: user-123" \
  -H "Content-Type: application/json" \
  -d '{
    "friendId": "user-456"
  }'
```

**예상 응답**:

```json
{
  "id": "...",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "PENDING",
  "nickname": null,
  "favorite": false,
  "createdAt": "2026-02-17T...",
  "updatedAt": "2026-02-17T..."
}
```

---

#### 2. 친구 목록 조회

```bash
curl http://localhost:20001/api/friendships \
  -H "X-User-Id: user-123"
```

---

#### 3. 채팅방 목록 조회 (고급 필터링)

```bash
curl "http://localhost:20001/api/channels?type=DIRECT&onlyUnread=true&sortBy=LAST_ACTIVITY&page=0&size=20" \
  -H "X-User-Id: user-123"
```

---

#### 4. 메시지 읽음 처리

```bash
curl -X PUT "http://localhost:20001/api/channels/channel-123/read?messageId=msg-456" \
  -H "X-User-Id: user-123"
```

---

### Postman Collection

프로젝트에 Postman Collection이 포함되어 있다면:

```bash
# Collection 가져오기
postman/chat-platform.postman_collection.json
```

---

## 문제 해결

### 1. 포트 충돌

**증상**: `Port 20001 is already in use`

**해결**:

```bash
# Windows
netstat -ano | findstr :20001
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :20001
kill -9 <PID>
```

---

### 2. 데이터베이스 연결 실패

**증상**: `Connection refused` 또는 `Could not connect to database`

**해결**:

```bash
# Docker 컨테이너 상태 확인
docker ps

# PostgreSQL 로그 확인
docker logs ms-postgres-source

# 재시작
docker-compose restart
```

---

### 3. Flyway Migration 실패

**증상**: `Flyway migration failed`

**해결**:

```bash
# Flyway 히스토리 확인
psql -h localhost -p 15432 -U chat_user -d chat_db
SELECT * FROM flyway_schema_history;

# 마이그레이션 재실행
./gradlew :apps:chat:libs:chat-storage:flywayClean
./gradlew :apps:chat:libs:chat-storage:flywayMigrate
```

---

### 4. 빌드 실패

**증상**: `Compilation failed`

**해결**:

```bash
# Gradle 캐시 삭제
./gradlew clean

# 의존성 다시 다운로드
./gradlew build --refresh-dependencies

# 특정 모듈만 빌드
./gradlew :apps:chat:system-server:build
```

---

### 5. Redis 연결 실패

**증상**: `Could not connect to Redis`

**해결**:

```bash
# Redis 컨테이너 상태 확인
docker ps | grep redis

# Redis CLI로 연결 테스트
docker exec -it ms-redis redis-cli -a dev_password
> PING
PONG

# Redis 재시작
docker-compose restart ms-redis
```

---

## 환경별 설정

### 개발 환경 (local)

**application.yml**:

```yaml
spring:
  profiles:
    active: local

  datasource:
    url: jdbc:postgresql://localhost:15432/chat_db
    username: chat_user
    password: dev_password

  data:
    redis:
      host: localhost
      port: 16379
      password: dev_password
```

---

### 테스트 환경

**application-test.yml**:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## 로그 확인

### 애플리케이션 로그

**로그 파일 위치**:

```
logs/application.log
logs/error.log
```

**로그 레벨 변경** (application.yml):

```yaml
logging:
  level:
    root: INFO
    com.example.chat: DEBUG
    org.springframework.data: DEBUG
```

---

### Docker 컨테이너 로그

```bash
# PostgreSQL
docker logs ms-postgres-source -f

# Redis
docker logs ms-redis -f

# Kafka
docker logs ms-kafka -f
```

---

## 성능 모니터링

### Actuator 엔드포인트

```bash
# Health Check
curl http://localhost:20001/actuator/health

# Metrics
curl http://localhost:20001/actuator/metrics

# DB Pool
curl http://localhost:20001/actuator/metrics/hikaricp.connections
```

---

### Redis 모니터링 (Redis Insight)

브라우저에서:

```
http://localhost:5540
```

**연결 정보**:

- Host: `ms-redis`
- Port: `6379`
- Password: `dev_password`

---

### Kafka UI

브라우저에서:

```
http://localhost:8089
```

---

## 개발 팁

### 1. Hot Reload (DevTools)

**build.gradle.kts** 에 추가:

```kotlin
dependencies {
	developmentOnly("org.springframework.boot:spring-boot-devtools")
}
```

**IntelliJ IDEA 설정**:

- `File > Settings > Build, Execution, Deployment > Compiler`
- ✅ `Build project automatically`
- ✅ `Allow auto-make to start even if developed application is running`

---

### 2. 데이터베이스 스키마 확인

```bash
# psql 접속
psql -h localhost -p 15432 -U chat_user -d chat_db

# 테이블 목록
\dt

# 테이블 구조 확인
\d chat_friendships
\d chat_channel_metadata
```

---

### 3. 샘플 데이터 생성

**SQL 스크립트 실행**:

```sql
-- 샘플 사용자 생성 (실제로는 auth-server에서 관리)
-- 샘플 친구 관계
INSERT INTO chat_friendships (id, user_id, friend_id, status, favorite, created_at, updated_at)
VALUES ('f-1', 'user-123', 'user-456', 'ACCEPTED', true, NOW(), NOW()),
       ('f-2', 'user-456', 'user-123', 'ACCEPTED', false, NOW(), NOW());

-- 샘플 채널 메타데이터
INSERT INTO chat_channel_metadata (id, channel_id, user_id,
                                   notification_enabled, favorite, pinned,
                                   unread_count, last_activity_at,
                                   created_at, updated_at)
VALUES ('m-1', 'channel-123', 'user-123', true, true, false, 5, NOW(), NOW(), NOW());
```

---

## 다음 단계

1. ✅ **Phase 1-3 완료** - 친구 관리, 채팅방 메타데이터, 고급 조회
2. ⏳ **Phase 4** - 실시간 사용자 상태 (Redis 온라인 상태)
3. ⏳ **Phase 5** - 성능 최적화 (캐싱, 쿼리 최적화)
4. ⏳ **테스트 작성** - 단위/통합 테스트
5. ⏳ **CI/CD 구축** - GitHub Actions, Docker 이미지

---

## 참고 문서

- **API 문서**: [API_ENDPOINTS.md](./API_ENDPOINTS.md)
- **전체 요약**: [FINAL_PROJECT_SUMMARY.md](./FINAL_PROJECT_SUMMARY.md)
- **설계 문서**: [FRIEND_AND_CHANNEL_ENHANCEMENT_DESIGN.md](./FRIEND_AND_CHANNEL_ENHANCEMENT_DESIGN.md)

---

**작성일**: 2026-02-17  
**작성자**: AI Assistant  
**버전**: 1.0
