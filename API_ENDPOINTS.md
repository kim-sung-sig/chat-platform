# API 엔드포인트 전체 목록

> **프로젝트**: 채팅 플랫폼  
> **작성일**: 2026-02-17  
> **총 엔드포인트**: 21개

---

## 📋 목차

1. [친구 관리 API (12개)](#친구-관리-api)
2. [채팅방 메타데이터 API (8개)](#채팅방-메타데이터-api)
3. [채팅방 조회 API (1개)](#채팅방-조회-api)

---

## 친구 관리 API

**Base URL**: `/api/friendships`

### 1. 친구 요청

```http
POST /api/friendships
```

**Request Headers**:

```
X-User-Id: user-123
Content-Type: application/json
```

**Request Body**:

```json
{
  "friendId": "user-456"
}
```

**Response** (201 Created):

```json
{
  "id": "friendship-789",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "PENDING",
  "nickname": null,
  "favorite": false,
  "createdAt": "2026-02-17T10:30:00Z",
  "updatedAt": "2026-02-17T10:30:00Z"
}
```

---

### 2. 친구 목록 조회 (수락된 친구)

```http
GET /api/friendships
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "friendship-789",
    "userId": "user-123",
    "friendId": "user-456",
    "status": "ACCEPTED",
    "nickname": "철수",
    "favorite": true,
    "createdAt": "2026-02-15T08:00:00Z",
    "updatedAt": "2026-02-16T14:30:00Z"
  }
]
```

---

### 3. 받은 친구 요청 목록

```http
GET /api/friendships/pending
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "friendship-999",
    "userId": "user-789",
    "friendId": "user-123",
    "status": "PENDING",
    "nickname": null,
    "favorite": false,
    "createdAt": "2026-02-17T09:00:00Z",
    "updatedAt": "2026-02-17T09:00:00Z"
  }
]
```

---

### 4. 보낸 친구 요청 목록

```http
GET /api/friendships/sent
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "friendship-888",
    "userId": "user-123",
    "friendId": "user-999",
    "status": "PENDING",
    "nickname": null,
    "favorite": false,
    "createdAt": "2026-02-17T08:30:00Z",
    "updatedAt": "2026-02-17T08:30:00Z"
  }
]
```

---

### 5. 즐겨찾기 친구 목록

```http
GET /api/friendships/favorites
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "friendship-789",
    "userId": "user-123",
    "friendId": "user-456",
    "status": "ACCEPTED",
    "nickname": "철수",
    "favorite": true,
    "createdAt": "2026-02-15T08:00:00Z",
    "updatedAt": "2026-02-16T14:30:00Z"
  }
]
```

---

### 6. 친구 요청 수락

```http
PUT /api/friendships/{requestId}/accept
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `requestId`: 친구 요청 ID

**Response** (200 OK):

```json
{
  "id": "friendship-999",
  "userId": "user-123",
  "friendId": "user-789",
  "status": "ACCEPTED",
  "nickname": null,
  "favorite": false,
  "createdAt": "2026-02-17T09:00:00Z",
  "updatedAt": "2026-02-17T10:35:00Z"
}
```

---

### 7. 친구 요청 거절

```http
DELETE /api/friendships/{requestId}/reject
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `requestId`: 친구 요청 ID

**Response** (204 No Content)

---

### 8. 친구 삭제

```http
DELETE /api/friendships/users/{friendId}
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `friendId`: 삭제할 친구의 사용자 ID

**Response** (204 No Content)

---

### 9. 친구 차단

```http
POST /api/friendships/users/{friendId}/block
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `friendId`: 차단할 친구의 사용자 ID

**Response** (200 OK):

```json
{
  "id": "friendship-789",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "BLOCKED",
  "nickname": null,
  "favorite": false,
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:00:00Z"
}
```

---

### 10. 친구 차단 해제

```http
DELETE /api/friendships/users/{friendId}/block
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `friendId`: 차단 해제할 친구의 사용자 ID

**Response** (200 OK):

```json
{
  "id": "friendship-789",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "ACCEPTED",
  "nickname": null,
  "favorite": false,
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:05:00Z"
}
```

---

### 11. 친구 별칭 설정

```http
PUT /api/friendships/users/{friendId}/nickname
```

**Request Headers**:

```
X-User-Id: user-123
Content-Type: application/json
```

**Path Parameters**:

- `friendId`: 별칭을 설정할 친구의 사용자 ID

**Request Body**:

```json
{
  "nickname": "친한친구"
}
```

**Response** (200 OK):

```json
{
  "id": "friendship-789",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "ACCEPTED",
  "nickname": "친한친구",
  "favorite": false,
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:10:00Z"
}
```

---

### 12. 즐겨찾기 토글

```http
PUT /api/friendships/users/{friendId}/favorite
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `friendId`: 즐겨찾기를 토글할 친구의 사용자 ID

**Response** (200 OK):

```json
{
  "id": "friendship-789",
  "userId": "user-123",
  "friendId": "user-456",
  "status": "ACCEPTED",
  "nickname": "친한친구",
  "favorite": true,
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:15:00Z"
}
```

---

## 채팅방 메타데이터 API

**Base URL**: `/api/channels`

### 1. 메타데이터 조회/생성

```http
GET /api/channels/{channelId}/metadata
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `channelId`: 채널 ID

**Response** (200 OK):

```json
{
  "id": "metadata-123",
  "channelId": "channel-456",
  "userId": "user-123",
  "notificationEnabled": true,
  "favorite": false,
  "pinned": false,
  "lastReadMessageId": "msg-789",
  "lastReadAt": "2026-02-17T10:00:00Z",
  "unreadCount": 5,
  "lastActivityAt": "2026-02-17T10:30:00Z",
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T10:30:00Z"
}
```

---

### 2. 메시지 읽음 처리

```http
PUT /api/channels/{channelId}/read?messageId={messageId}
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `channelId`: 채널 ID

**Query Parameters**:

- `messageId`: 읽은 메시지 ID

**Response** (200 OK):

```json
{
  "id": "metadata-123",
  "channelId": "channel-456",
  "userId": "user-123",
  "notificationEnabled": true,
  "favorite": false,
  "pinned": false,
  "lastReadMessageId": "msg-999",
  "lastReadAt": "2026-02-17T11:00:00Z",
  "unreadCount": 0,
  "lastActivityAt": "2026-02-17T11:00:00Z",
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:00:00Z"
}
```

---

### 3. 알림 설정 토글

```http
PUT /api/channels/{channelId}/notification
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `channelId`: 채널 ID

**Response** (200 OK):

```json
{
  "id": "metadata-123",
  "channelId": "channel-456",
  "userId": "user-123",
  "notificationEnabled": false,
  "favorite": false,
  "pinned": false,
  "lastReadMessageId": "msg-999",
  "lastReadAt": "2026-02-17T11:00:00Z",
  "unreadCount": 0,
  "lastActivityAt": "2026-02-17T11:00:00Z",
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:05:00Z"
}
```

---

### 4. 즐겨찾기 토글

```http
PUT /api/channels/{channelId}/favorite
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `channelId`: 채널 ID

**Response** (200 OK):

```json
{
  "id": "metadata-123",
  "channelId": "channel-456",
  "userId": "user-123",
  "notificationEnabled": false,
  "favorite": true,
  "pinned": false,
  "lastReadMessageId": "msg-999",
  "lastReadAt": "2026-02-17T11:00:00Z",
  "unreadCount": 0,
  "lastActivityAt": "2026-02-17T11:00:00Z",
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:10:00Z"
}
```

---

### 5. 상단 고정 토글

```http
PUT /api/channels/{channelId}/pin
```

**Request Headers**:

```
X-User-Id: user-123
```

**Path Parameters**:

- `channelId`: 채널 ID

**Response** (200 OK):

```json
{
  "id": "metadata-123",
  "channelId": "channel-456",
  "userId": "user-123",
  "notificationEnabled": false,
  "favorite": true,
  "pinned": true,
  "lastReadMessageId": "msg-999",
  "lastReadAt": "2026-02-17T11:00:00Z",
  "unreadCount": 0,
  "lastActivityAt": "2026-02-17T11:00:00Z",
  "createdAt": "2026-02-15T08:00:00Z",
  "updatedAt": "2026-02-17T11:15:00Z"
}
```

---

### 6. 즐겨찾기 채팅방 목록

```http
GET /api/channels/favorites
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "metadata-123",
    "channelId": "channel-456",
    "userId": "user-123",
    "notificationEnabled": false,
    "favorite": true,
    "pinned": false,
    "lastReadMessageId": "msg-999",
    "lastReadAt": "2026-02-17T11:00:00Z",
    "unreadCount": 0,
    "lastActivityAt": "2026-02-17T11:00:00Z",
    "createdAt": "2026-02-15T08:00:00Z",
    "updatedAt": "2026-02-17T11:10:00Z"
  }
]
```

---

### 7. 상단 고정 채팅방 목록

```http
GET /api/channels/pinned
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "metadata-123",
    "channelId": "channel-456",
    "userId": "user-123",
    "notificationEnabled": false,
    "favorite": true,
    "pinned": true,
    "lastReadMessageId": "msg-999",
    "lastReadAt": "2026-02-17T11:00:00Z",
    "unreadCount": 0,
    "lastActivityAt": "2026-02-17T11:00:00Z",
    "createdAt": "2026-02-15T08:00:00Z",
    "updatedAt": "2026-02-17T11:15:00Z"
  }
]
```

---

### 8. 읽지 않은 메시지가 있는 채팅방 목록

```http
GET /api/channels/unread
```

**Request Headers**:

```
X-User-Id: user-123
```

**Response** (200 OK):

```json
[
  {
    "id": "metadata-789",
    "channelId": "channel-999",
    "userId": "user-123",
    "notificationEnabled": true,
    "favorite": false,
    "pinned": false,
    "lastReadMessageId": "msg-555",
    "lastReadAt": "2026-02-17T09:00:00Z",
    "unreadCount": 10,
    "lastActivityAt": "2026-02-17T11:20:00Z",
    "createdAt": "2026-02-16T10:00:00Z",
    "updatedAt": "2026-02-17T11:20:00Z"
  }
]
```

---

## 채팅방 조회 API

**Base URL**: `/api/channels`

### 1. 채팅방 목록 조회 (고급 필터링/정렬)

```http
GET /api/channels?type={type}&onlyFavorites={boolean}&onlyUnread={boolean}&onlyPinned={boolean}&search={keyword}&sortBy={sortBy}&direction={direction}&page={page}&size={size}
```

**Request Headers**:

```
X-User-Id: user-123
```

**Query Parameters**:

| 파라미터            | 타입            | 필수 | 기본값           | 설명                                            |
|-----------------|---------------|----|---------------|-----------------------------------------------|
| `type`          | ChannelType   | No | -             | DIRECT, GROUP, PUBLIC, PRIVATE                |
| `onlyFavorites` | Boolean       | No | false         | 즐겨찾기만 보기                                      |
| `onlyUnread`    | Boolean       | No | false         | 읽지 않은 메시지 있는 것만                               |
| `onlyPinned`    | Boolean       | No | false         | 상단 고정만 보기                                     |
| `search`        | String        | No | -             | 검색 키워드 (채널명, 상대방 이름)                          |
| `sortBy`        | ChannelSortBy | No | LAST_ACTIVITY | LAST_ACTIVITY, NAME, UNREAD_COUNT, CREATED_AT |
| `direction`     | SortDirection | No | DESC          | ASC, DESC                                     |
| `page`          | Integer       | No | 0             | 페이지 번호 (0부터 시작)                               |
| `size`          | Integer       | No | 20            | 페이지 크기                                        |

**Example Request**:

```http
GET /api/channels?type=DIRECT&onlyFavorites=true&onlyUnread=true&search=김철수&sortBy=LAST_ACTIVITY&direction=DESC&page=0&size=20
```

**Response** (200 OK):

```json
{
  "content": [
    {
      "channelId": "channel-456",
      "channelName": "김철수",
      "channelDescription": null,
      "channelType": "DIRECT",
      "active": true,
      "lastMessageId": "msg-999",
      "lastMessageContent": "안녕하세요",
      "lastMessageSenderId": "user-456",
      "lastMessageSenderName": "김철수",
      "lastMessageTime": "2026-02-17T10:30:00Z",
      "unreadCount": 5,
      "favorite": true,
      "pinned": false,
      "notificationEnabled": true,
      "lastReadAt": "2026-02-17T10:00:00Z",
      "lastActivityAt": "2026-02-17T10:30:00Z",
      "memberCount": 2,
      "otherUserId": "user-456",
      "otherUserName": "김철수",
      "otherUserEmail": "kim@example.com",
      "ownerUserId": null,
      "ownerUserName": null,
      "createdAt": "2026-02-15T08:00:00Z"
    },
    {
      "channelId": "channel-789",
      "channelName": "프로젝트팀",
      "channelDescription": "프로젝트 논의방",
      "channelType": "GROUP",
      "active": true,
      "lastMessageId": "msg-888",
      "lastMessageContent": "회의 시작합니다",
      "lastMessageSenderId": "user-999",
      "lastMessageSenderName": "이영희",
      "lastMessageTime": "2026-02-17T09:45:00Z",
      "unreadCount": 3,
      "favorite": true,
      "pinned": true,
      "notificationEnabled": true,
      "lastReadAt": "2026-02-17T09:00:00Z",
      "lastActivityAt": "2026-02-17T09:45:00Z",
      "memberCount": 5,
      "otherUserId": null,
      "otherUserName": null,
      "otherUserEmail": null,
      "ownerUserId": "user-888",
      "ownerUserName": "박민수",
      "createdAt": "2026-02-10T14:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 2,
  "empty": false
}
```

---

## 에러 응답

### 일반적인 에러 형식

```json
{
  "timestamp": "2026-02-17T11:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Friend ID is required",
  "path": "/api/friendships"
}
```

### 주요 에러 코드

| 상태 코드                     | 설명     | 예시                  |
|---------------------------|--------|---------------------|
| 400 Bad Request           | 잘못된 요청 | 필수 필드 누락, 유효성 검증 실패 |
| 404 Not Found             | 리소스 없음 | 친구 관계가 존재하지 않음      |
| 409 Conflict              | 충돌     | 이미 친구 요청이 존재함       |
| 500 Internal Server Error | 서버 오류  | 예기치 않은 오류           |

---

## 테스트 예시 (cURL)

### 친구 요청

```bash
curl -X POST http://localhost:20001/api/friendships \
  -H "X-User-Id: user-123" \
  -H "Content-Type: application/json" \
  -d '{"friendId": "user-456"}'
```

### 채팅방 목록 조회

```bash
curl -X GET "http://localhost:20001/api/channels?onlyUnread=true&sortBy=LAST_ACTIVITY&page=0&size=20" \
  -H "X-User-Id: user-123"
```

### 알림 토글

```bash
curl -X PUT http://localhost:20001/api/channels/channel-456/notification \
  -H "X-User-Id: user-123"
```

---

## 인증/인가

현재 구현에서는 **X-User-Id 헤더**를 사용하여 사용자를 식별합니다.

향후 JWT 기반 인증으로 전환 예정:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

**작성일**: 2026-02-17  
**버전**: 1.0  
**상태**: Phase 1-3 완료
