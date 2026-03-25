# SDD: 파일 업로드 API

## 1. Title / Version / Status / Owners

- **Title**: 채팅 파일 업로드 API (File Upload Context)
- **Version**: 1.0
- **Status**: Draft
- **Owners**: chat-platform team
- **Related Docs**:
  - Plan: `docs/01-plan/features/file-upload.plan.md`
  - Chat-improve Plan (원본 요구사항): `docs/01-plan/features/chat-improve.plan.md` (BE-P1-2)
  - Skeleton: `TBD`
  - Tests: `TBD`

---

## 2. Problem Statement

### What problem are we solving?
채팅 채널에서 이미지·파일을 첨부할 수 없어 텍스트 메시지만 가능한 상태.
사용자가 파일을 공유하려면 외부 링크를 직접 복사·붙여넣기해야 한다.

### Why now?
- 예약 발송(BE-P1-1) 구현 완료 후 다음 우선순위 핵심 기능
- 프론트엔드 파일 업로드 UI(FE-P1-2)와 병렬 개발 필요
- `chat_messages.content_media_url` 컬럼이 이미 DDL에 존재(V1 migration) — 스키마 변경 최소

---

## 3. Goals / Non-Goals

### Goals
- `POST /api/files/upload` (multipart/form-data) 엔드포인트 구현
- AWS S3 또는 MinIO(로컬 개발)에 파일 저장 후 퍼블릭/pre-signed URL 반환
- 파일 타입·크기 서버 측 검증 (이미지: 10 MB, 기타: 50 MB)
- DDD 레이어 분리: `file/domain`, `file/application`, `file/infrastructure`, `file/rest`
- CQRS: `FileUploadCommandService`(업로드), `FileQueryService`(메타데이터 조회)
- `uploaded_files` 테이블 추가 (Flyway V11)
- 테스트: 도메인 단위 + CommandService 단위 (Mockito)

### Non-Goals
- 파일 삭제 API (별도 이터레이션)
- CDN 캐싱 설정 (인프라 레벨, 별도 이터레이션)
- 클라이언트 직접 업로드(pre-signed URL 발급 방식) — 이 이터레이션은 서버 프록시 방식
- 바이러스 스캔 (별도 보안 이터레이션)
- 영상/음성 트랜스코딩

---

## 4. Stakeholders / Target Users

- **채팅 사용자**: 채널에서 이미지·파일을 공유하고자 하는 모든 사용자
- **FE 팀**: `FileUploadProgress.vue` 컴포넌트 연동
- **chat-server**: 파일 URL을 메시지의 `content_media_url`에 저장

---

## 5. Requirements

### Functional Requirements

| ID | 요구사항 |
|----|---------|
| FR-01 | `POST /api/files/upload` — multipart/form-data로 파일 1개 업로드 |
| FR-02 | 업로드 성공 시 `{ fileId, fileUrl, fileName, fileSize, mimeType, uploadedAt }` 반환 |
| FR-03 | 이미지(image/*) 최대 10 MB, 기타 파일 최대 50 MB 검증 |
| FR-04 | 지원 파일 타입: 이미지(JPEG·PNG·GIF·WEBP), 문서(PDF·DOCX·XLSX·PPTX), 오디오(MP3·AAC), 비디오(MP4·WEBM) |
| FR-05 | 업로드된 파일 메타데이터를 `uploaded_files` DB에 저장 (uploaderId, channelId, S3 key) |
| FR-06 | 파일명 충돌 방지: S3 key = `{channelId}/{UUID}/{originalFileName}` |
| FR-07 | 채널 멤버만 해당 채널에 파일 업로드 가능 (채널 멤버십 검증) |
| FR-08 | `GET /api/files/{fileId}` — 파일 메타데이터 조회 (fileUrl 포함) |

### Non-Functional Requirements

| ID | 요구사항 |
|----|---------|
| NFR-01 | 단일 파일 업로드 P99 응답 시간 < 5 s (10 MB 기준, LAN 환경) |
| NFR-02 | S3/MinIO 업로드 실패 시 DB 롤백 (트랜잭션 경계 명확화) |
| NFR-03 | 파일 URL은 퍼블릭 읽기 가능 (CDN 연동 후 변경 가능) |
| NFR-04 | 컴파일 게이트 통과: `./gradlew compileJava compileTestJava --no-daemon` |

---

## 6. Domain Knowledge

### Glossary

| 용어 | 정의 |
|------|------|
| `UploadedFile` | 업로드 완료된 파일의 Aggregate Root. S3 key, 원본 파일명, MIME 타입, 크기 보유 |
| `FileUrl` | S3 퍼블릭 URL 또는 pre-signed URL. Value Object |
| `S3Key` | S3 버킷 내 객체 경로 Value Object (`{channelId}/{UUID}/{fileName}`) |
| `UploadStatus` | PENDING → COMPLETED / FAILED |
| `FileType` | IMAGE / DOCUMENT / AUDIO / VIDEO / OTHER |

### Invariants / Constraints

- 이미지 파일 크기 ≤ 10 MB
- 기타 파일 크기 ≤ 50 MB
- 업로더는 반드시 해당 채널의 멤버여야 함
- S3 Key는 전역 유일 (UUID 포함)
- 파일 이름은 공백 제거 및 URL 안전 문자로 정규화

### Domain Rules

- 파일 업로드는 단방향: 업로드 후 URL 변경 불가 (불변)
- MIME 타입은 Content-Type 헤더가 아닌 서버 측 실제 바이트 검증으로 결정 (TBD: Apache Tika 또는 단순 확장자 매핑)
- S3 업로드 실패 시 `UploadStatus.FAILED`로 DB 저장 후 클라이언트에 오류 반환

---

## 7. Domain Model & Boundaries

### Bounded Context
`file` — 파일 업로드 및 메타데이터 관리. `message` Context와 독립적.
`message` Context에서 `fileUrl`을 `content_media_url`로 직접 참조 (ID FK 없음).

### Aggregate Root
```
UploadedFile
  - id: String (UUID)
  - channelId: String
  - uploaderId: String
  - originalFileName: String
  - s3Key: String
  - fileUrl: String
  - fileSize: Long (bytes)
  - mimeType: String
  - fileType: FileType (enum)
  - status: UploadStatus (enum)
  - uploadedAt: ZonedDateTime
```

### Enums
```
UploadStatus: PENDING, COMPLETED, FAILED
FileType: IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER
```

### Value Objects
- `S3Key` — `{channelId}/{uuid}/{originalFileName}` 형식 검증 포함
- `FileUrl` — HTTPS URL 형식 검증

---

## 8. Interfaces

### Commands / Queries

| 이름 | 타입 | 설명 |
|------|------|------|
| `UploadFileCommand` | Command | 파일 업로드 요청 (uploaderId, channelId, file) |
| `GetFileQuery` | Query | fileId로 메타데이터 조회 |

### REST Endpoints

#### `POST /api/files/upload`
```
Content-Type: multipart/form-data

Request fields:
  - file: MultipartFile (required)
  - channelId: String (required)

Response 201:
{
  "fileId":    "uuid-string",
  "fileUrl":   "https://s3.../...",
  "fileName":  "screenshot.png",
  "fileSize":  102400,
  "mimeType":  "image/png",
  "fileType":  "IMAGE",
  "uploadedAt": "2026-03-25T12:00:00Z"
}
```

#### `GET /api/files/{fileId}`
```
Response 200:
{
  "fileId":    "uuid-string",
  "fileUrl":   "https://s3.../...",
  "fileName":  "document.pdf",
  "fileSize":  512000,
  "mimeType":  "application/pdf",
  "fileType":  "DOCUMENT",
  "uploadedAt": "2026-03-25T12:00:00Z"
}
```

### Error Codes

| 코드 | HTTP | 설명 |
|------|------|------|
| `FILE_SIZE_EXCEEDED` | 413 | 파일 크기 초과 |
| `FILE_TYPE_NOT_ALLOWED` | 415 | 허용되지 않는 파일 타입 |
| `FILE_EMPTY` | 400 | 파일 내용 없음 |
| `FILE_NOT_FOUND` | 404 | fileId에 해당 파일 없음 |
| `CHANNEL_NOT_MEMBER` | 403 | 채널 미가입 (기존 코드 재사용) |
| `FILE_UPLOAD_FAILED` | 500 | S3/MinIO 업로드 실패 |

### External Systems

| 시스템 | 용도 | 연동 방식 |
|--------|------|----------|
| AWS S3 / MinIO | 파일 저장 | AWS SDK v2 (`software.amazon.awssdk:s3:2.25.0`) |
| PostgreSQL | 메타데이터 저장 | JPA (기존 DataSource 재사용) |

---

## 9. Data Model

### `uploaded_files` 테이블 (Flyway V11)

```sql
CREATE TABLE uploaded_files (
    id              VARCHAR(36)  PRIMARY KEY,
    channel_id      VARCHAR(36)  NOT NULL,
    uploader_id     VARCHAR(36)  NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    s3_key          VARCHAR(500) NOT NULL UNIQUE,
    file_url        VARCHAR(1000) NOT NULL,
    file_size       BIGINT       NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    file_type       VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_file_size CHECK (file_size > 0)
);

CREATE INDEX idx_uploaded_files_channel  ON uploaded_files(channel_id);
CREATE INDEX idx_uploaded_files_uploader ON uploaded_files(uploader_id);
```

### JPA Entity: `UploadedFileEntity`

- 패키지: `com.example.chat.file.infrastructure.datasource`
- `@Getter @NoArgsConstructor(access = PROTECTED)`
- `fromDomain(UploadedFile)` / `toDomain()` 변환 메서드

---

## 10. Workflow / State Transitions

### 파일 업로드 흐름

```
Client
  │ POST /api/files/upload (multipart)
  ▼
FileUploadController
  │ SecurityUtils.getCurrentUserId()
  │ @Valid
  ▼
FileUploadCommandService
  ├─ 채널 멤버십 검증 (JpaChannelMemberRepository)
  ├─ 파일 타입·크기 검증
  ├─ UploadedFile 도메인 생성 (status=PENDING)
  ├─ DB 저장 (PENDING)
  ├─ S3StorageService.upload(s3Key, bytes, mimeType)
  │     └─ 성공: fileUrl 획득
  │     └─ 실패: FileUploadFailedException 발생
  ├─ domain.markCompleted(fileUrl)
  └─ DB 저장 (COMPLETED)
  ▼
FileUploadResponse (201)
```

### UploadStatus 전이

```
PENDING ──(S3 성공)──→ COMPLETED
PENDING ──(S3 실패)──→ FAILED
```

---

## 11. Validation Rules & Edge Cases

| 규칙 | 설명 |
|------|------|
| 파일 크기 — 이미지 | `image/*` → 최대 10 MB (10 × 1024 × 1024 bytes) |
| 파일 크기 — 기타 | 그 외 → 최대 50 MB (50 × 1024 × 1024 bytes) |
| 빈 파일 | `file.isEmpty()` → `FILE_EMPTY` 오류 |
| 파일 타입 | MIME 타입이 허용 목록에 없으면 → `FILE_TYPE_NOT_ALLOWED` |
| 파일명 정규화 | 공백 → `_`, 특수문자 제거, 최대 200자 |
| S3 실패 | DB PENDING 레코드 FAILED 전이 후 예외 전파 |
| 동시 업로드 | UUID 기반 S3 Key로 충돌 없음 |
| 채널 미가입 | `CHANNEL_NOT_MEMBER` 오류 (기존 에러코드 재사용) |

---

## 12. Security / Privacy / Compliance

- **AuthN**: `SecurityUtils.getCurrentUserId()` — JWT 기반 인증 (기존 공통 모듈)
- **AuthZ**: 채널 멤버십 검증 (업로더가 해당 채널 가입자인지)
- **파일 URL 접근**: 현재 퍼블릭 S3 URL — 민감 파일은 pre-signed URL로 전환 (별도 이터레이션)
- **Content-Type 신뢰 금지**: 클라이언트 헤더 아닌 서버 측 MIME 검증 (TBD: Tika or magic-bytes)
- **파일명 인젝션**: 정규화 처리로 Path Traversal 방지
- **S3 자격증명**: `application.yml` — `aws.s3.access-key`, `aws.s3.secret-key` (환경변수로 주입)

---

## 13. Observability

### Logs (구조화 로그)
```json
{ "event": "file.upload.started",  "fileId": "...", "channelId": "...", "fileSize": 1024 }
{ "event": "file.upload.completed","fileId": "...", "fileUrl": "...", "durationMs": 120 }
{ "event": "file.upload.failed",   "fileId": "...", "reason": "S3 timeout" }
```

### Metrics
- `file.upload.count{status=success|failure}` — 업로드 성공/실패 건수
- `file.upload.size.bytes` — 업로드 파일 크기 히스토그램

### Tracing
- 기존 Brave/Zipkin trace propagation 활용 (공통 logging 모듈)

---

## 14. Test Strategy

### 단위 테스트 (Mockito)

| 시나리오 | 클래스 |
|---------|--------|
| 정상 업로드 — 이미지 5 MB → COMPLETED 전이 + S3 호출 검증 | `FileUploadCommandServiceImplTest` |
| 파일 크기 초과 — 이미지 11 MB → FILE_SIZE_EXCEEDED | `FileUploadCommandServiceImplTest` |
| 파일 크기 초과 — 기타 51 MB → FILE_SIZE_EXCEEDED | `FileUploadCommandServiceImplTest` |
| 허용되지 않는 MIME 타입 → FILE_TYPE_NOT_ALLOWED | `FileUploadCommandServiceImplTest` |
| 빈 파일 → FILE_EMPTY | `FileUploadCommandServiceImplTest` |
| 채널 미가입 → CHANNEL_NOT_MEMBER | `FileUploadCommandServiceImplTest` |
| S3 업로드 실패 → FAILED 상태 저장 + 예외 전파 | `FileUploadCommandServiceImplTest` |
| 도메인 정상 생성 — PENDING 상태 | `UploadedFileTest` |
| `markCompleted(fileUrl)` — COMPLETED 전이 | `UploadedFileTest` |
| `markFailed()` — FAILED 전이 | `UploadedFileTest` |
| Entity `fromDomain` / `toDomain` 변환 | `UploadedFileEntityTest` |
| `S3Key` 형식 검증 | `S3KeyTest` |

### 테스트 패턴
- `@Nested` + `@DisplayName(한국어)` 준수
- S3StorageService mock 처리 (실제 S3 호출 없음)

---

## 15. Risks & Assumptions

| 위험 | 대응 |
|------|------|
| S3/MinIO 미구성 환경에서 테스트 실패 | `S3StorageService` 인터페이스 + Mock 구현체 분리; 로컬은 Mock 사용 |
| 대용량 파일 메모리 OOM | `MultipartFile` 임시 파일 처리 (`spring.servlet.multipart.location` 설정) |
| 동일 파일 중복 업로드 | UUID 기반 S3 Key로 중복 방지 (내용 중복은 허용) |
| MinIO vs S3 호환성 | AWS SDK v2의 S3 API는 MinIO와 호환 — `endpoint` 설정으로 전환 가능 |

---

## 16. Open Questions

| # | 질문 | 담당 |
|---|------|------|
| OQ-01 | MIME 타입 검증 방식: Apache Tika vs 확장자 매핑? (Tika는 의존성 추가 필요) | chat-platform team |
| OQ-02 | S3 버킷 공개 여부: 퍼블릭 읽기 vs pre-signed URL? (보안 정책에 따라 결정) | 보안 담당 |
| OQ-03 | MinIO 로컬 개발 환경 Docker Compose 기존에 있는지 확인 필요 | DevOps |
| OQ-04 | `file_size` 제한을 Spring `MultipartResolver` 레벨에서도 설정할지 (현재 서비스 레이어만) | chat-platform team |

---

## 17. Traceability

| 요구사항 ID | 테스트 시나리오 |
|------------|----------------|
| FR-01 | 정상 업로드 |
| FR-02 | 정상 업로드 응답 필드 검증 |
| FR-03 | 크기 초과 이미지, 크기 초과 기타 파일 |
| FR-04 | 허용되지 않는 MIME 타입 |
| FR-05 | DB 저장 호출 검증 (CommandService 테스트) |
| FR-06 | S3Key 형식 검증 (`{channelId}/{UUID}/{fileName}`) |
| FR-07 | 채널 미가입 오류 |
| FR-08 | FileQueryService (별도 테스트) |

---

## 18. References

- Plan (chat-improve): `docs/01-plan/features/chat-improve.plan.md` (BE-P1-2)
- 예약 발송 SDD (참조 패턴): `docs/specs/SDD_scheduled-message.md`
- AWS SDK v2 S3 Docs: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html
- Flyway 이전 마이그레이션: `apps/chat/libs/chat-storage/src/main/resources/db/migration/V10__scheduled_message_fix.sql`
- CONVENTIONS.md: `docs/conventions/CONVENTIONS.md`
