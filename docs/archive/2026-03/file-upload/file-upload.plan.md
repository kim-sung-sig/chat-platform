# Plan: file-upload

> **Feature**: 채팅 파일 업로드 API (BE-P1-2)
> **Date**: 2026-03-25
> **Phase**: plan
> **SDD**: `docs/specs/SDD_file-upload.md`

---

## Executive Summary

| Perspective | Description |
|-------------|-------------|
| **Problem** | 채팅 채널에서 파일·이미지 첨부가 불가능해 텍스트 메시지만 전송 가능; 파일 공유 시 외부 링크를 수동으로 복사·붙여넣기해야 하는 UX 마찰이 존재한다 |
| **Solution** | `POST /api/files/upload` (multipart) → AWS S3/MinIO 저장 → DB 메타데이터 기록 파이프라인을 DDD/CQRS 구조로 구현; 파일 타입·크기 검증 및 채널 멤버십 인가 포함 |
| **Function UX Effect** | 채널에서 이미지(10 MB)·문서·오디오·비디오(50 MB) 첨부 가능; `fileUrl` 반환으로 FE 파일 업로드 진행률 UI와 즉시 연동; 기존 `content_media_url` 컬럼 재사용으로 메시지 표시 변경 없음 |
| **Core Value** | Discord 수준의 파일 공유 UX 달성; DDD/CQRS 아키텍처 패턴으로 유지보수 용이; V11 Flyway 마이그레이션으로 스키마 버전 관리 지속 |

---

## 1. 현황 분석 요약

### 1.1 현재 상태

**기존 파일 관련 인프라:**
- ✅ `chat_messages.content_media_url` 컬럼 존재 (V1 DDL)
- ✅ `MessageContent.Image`, `MessageContent.File` 도메인 타입 존재
- ❌ 파일 업로드 API 없음 (`file/` 패키지 미생성)
- ❌ S3/MinIO 의존성 미추가
- ❌ `uploaded_files` 테이블 없음

### 1.2 기존 코드 재사용 가능 항목

| 항목 | 재사용 여부 | 설명 |
|------|:----------:|------|
| `SecurityUtils.getCurrentUserId()` | ✅ | JWT 인증 추출 |
| `JpaChannelMemberRepository.existsByChannelIdAndUserId()` | ✅ | 채널 멤버십 검증 |
| `ChatErrorCode.CHANNEL_NOT_MEMBER` | ✅ | 기존 에러코드 재사용 |
| `GlobalExceptionHandler` | ✅ | 신규 에러코드 등록만 추가 |
| Flyway 마이그레이션 (V10 다음 V11) | ✅ | 순차 마이그레이션 |

---

## 2. Spec Checklist

### API 엔드포인트

- [ ] `POST /api/files/upload` — multipart/form-data 파일 업로드
  - Request: `file` (MultipartFile) + `channelId` (String)
  - Response 201: `{ fileId, fileUrl, fileName, fileSize, mimeType, fileType, uploadedAt }`
- [ ] `GET /api/files/{fileId}` — 파일 메타데이터 조회
  - Response 200: 동일 구조

### Domain Entity / Value Object

- [ ] `UploadedFile` Aggregate Root (순수 POJO)
  - 필드: id, channelId, uploaderId, originalFileName, s3Key, fileUrl, fileSize, mimeType, fileType, status, uploadedAt
  - 메서드: `markCompleted(fileUrl)`, `markFailed()`
- [ ] `UploadStatus` enum — PENDING / COMPLETED / FAILED
- [ ] `FileType` enum — IMAGE / DOCUMENT / AUDIO / VIDEO / OTHER
- [ ] `S3Key` Value Object — `{channelId}/{UUID}/{fileName}` 형식 검증

### Command / Query 분리 (CQRS)

- [ ] `FileUploadCommandService` (인터페이스) + `FileUploadCommandServiceImpl`
  - `uploadFile(String uploaderId, String channelId, MultipartFile file) → FileUploadResponse`
- [ ] `FileQueryService` (인터페이스) + `FileQueryServiceImpl`
  - `getFile(String fileId) → FileUploadResponse`

### Infrastructure

- [ ] `S3StorageService` 인터페이스 (도메인 레이어, Spring 미의존)
- [ ] `S3StorageServiceImpl` — AWS SDK v2 `S3Client` 사용
- [ ] `UploadedFileEntity` JPA Entity (`uploaded_files` 테이블)
  - `fromDomain(UploadedFile)` / `toDomain()` 팩토리 메서드
- [ ] `JpaUploadedFileRepository` extends `JpaRepository`
- [ ] `UploadedFileRepositoryAdapter` — Port/Adapter 패턴
- [ ] Flyway V11: `uploaded_files` 테이블 생성

### 유효성 검증

- [ ] 파일 크기: 이미지 ≤ 10 MB, 기타 ≤ 50 MB
- [ ] 파일 타입: JPEG·PNG·GIF·WEBP·PDF·DOCX·XLSX·PPTX·MP3·AAC·MP4·WEBM
- [ ] 빈 파일 거부 (`file.isEmpty()`)
- [ ] 채널 멤버십 검증 (업로더가 해당 채널 가입자인지)

### 에러 코드 (ChatErrorCode 추가)

- [ ] `FILE_SIZE_EXCEEDED` (HTTP 413)
- [ ] `FILE_TYPE_NOT_ALLOWED` (HTTP 415)
- [ ] `FILE_EMPTY` (HTTP 400)
- [ ] `FILE_NOT_FOUND` (HTTP 404)
- [ ] `FILE_UPLOAD_FAILED` (HTTP 500)

### 빌드 의존성 (build.gradle)

- [ ] `implementation 'software.amazon.awssdk:s3:2.25.0'`
- [ ] `testImplementation 'software.amazon.awssdk:s3:2.25.0'` (mock 테스트용)

### 테스트 시나리오 (단위)

- [ ] 정상 업로드 — 이미지 5 MB → COMPLETED 전이, S3 upload 호출 검증
- [ ] 이미지 크기 초과 — 11 MB → `FILE_SIZE_EXCEEDED`
- [ ] 기타 파일 크기 초과 — 51 MB → `FILE_SIZE_EXCEEDED`
- [ ] 허용되지 않는 MIME → `FILE_TYPE_NOT_ALLOWED`
- [ ] 빈 파일 → `FILE_EMPTY`
- [ ] 채널 미가입 → `CHANNEL_NOT_MEMBER`
- [ ] S3 업로드 실패 → FAILED 상태 저장 + `FILE_UPLOAD_FAILED`
- [ ] `UploadedFile.markCompleted()` 상태 전이
- [ ] `UploadedFile.markFailed()` 상태 전이
- [ ] `S3Key` 형식 검증
- [ ] `UploadedFileEntity.fromDomain()` / `toDomain()` 변환

---

## 3. 아키텍처 구조

### 3.1 DDD 패키지 레이아웃

```
apps/chat/chat-server/src/main/java/com/example/chat/file/
├── domain/
│   ├── model/
│   │   ├── UploadedFile.java          [NEW] Aggregate Root
│   │   ├── UploadStatus.java          [NEW] enum
│   │   ├── FileType.java              [NEW] enum
│   │   └── S3Key.java                 [NEW] Value Object
│   ├── repository/
│   │   └── UploadedFileRepository.java [NEW] Port interface
│   └── service/
│       └── S3StorageService.java      [NEW] 외부 스토리지 Port
├── application/
│   └── service/
│       ├── FileUploadCommandService.java     [NEW] Command Port
│       ├── FileUploadCommandServiceImpl.java  [NEW] 구현
│       ├── FileQueryService.java             [NEW] Query Port
│       └── FileQueryServiceImpl.java          [NEW] 구현
├── infrastructure/
│   ├── datasource/
│   │   ├── UploadedFileEntity.java           [NEW] JPA Entity
│   │   ├── JpaUploadedFileRepository.java    [NEW] Spring Data JPA
│   │   └── UploadedFileRepositoryAdapter.java [NEW] Adapter
│   └── storage/
│       └── S3StorageServiceImpl.java         [NEW] AWS SDK v2
└── rest/
    ├── controller/
    │   └── FileUploadController.java         [NEW]
    └── dto/
        ├── request/
        │   └── (MultipartFile + channelId — controller 파라미터로 처리)
        └── response/
            └── FileUploadResponse.java       [NEW] Java record

apps/chat/libs/chat-storage/src/main/resources/db/migration/
└── V11__uploaded_files.sql                   [NEW]
```

### 3.2 의존성 방향

```
rest → application (interface) → domain
infrastructure → domain (interface 구현)
application → infrastructure (DI)
```

---

## 4. 기술 스택 추가

```groovy
// build.gradle (chat-server)
implementation 'software.amazon.awssdk:s3:2.25.0'
implementation 'software.amazon.awssdk:auth:2.25.0'  // DefaultCredentialsProvider
```

### application.yml 설정 추가

```yaml
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET:chat-files-local}
    region: ${AWS_REGION:ap-northeast-2}
    endpoint: ${AWS_S3_ENDPOINT:}        # MinIO 로컬 개발용
    access-key: ${AWS_ACCESS_KEY:}
    secret-key: ${AWS_SECRET_KEY:}
```

---

## 5. 우선순위 매트릭스

| ID | 항목 | 우선순위 | 예상 공수 |
|----|------|----------|-----------|
| FILE-01 | 도메인 모델 (UploadedFile, enums, S3Key) | P0 | 0.5d |
| FILE-02 | Flyway V11 마이그레이션 | P0 | 0.25d |
| FILE-03 | UploadedFileEntity + Repository Adapter | P0 | 0.5d |
| FILE-04 | S3StorageService 인터페이스 + Impl | P0 | 1d |
| FILE-05 | FileUploadCommandServiceImpl (검증 + 업로드) | P0 | 1d |
| FILE-06 | FileQueryServiceImpl | P0 | 0.25d |
| FILE-07 | FileUploadController | P0 | 0.5d |
| FILE-08 | ChatErrorCode 추가 (5개) | P0 | 0.25d |
| FILE-09 | 단위 테스트 (12개+) | P0 | 1d |
| FILE-10 | build.gradle AWS SDK 추가 | P0 | 0.25d |

**총 예상 공수: ~5.5일**

---

## 6. 컨벤션 준수 체크리스트

- [ ] DDD 레이어 분리: `domain → application → infrastructure → rest`
- [ ] CQRS 명명: `*CommandService` (쓰기), `*QueryService` (읽기)
- [ ] Record DTO: `FileUploadResponse` Java record
- [ ] Factory method: `FileUploadResponse.from(domain)` 패턴
- [ ] `@CurrentUser` → `SecurityUtils.getCurrentUserId()` 사용
- [ ] `@Setter` 금지: `UploadedFileEntity`는 `@Getter + @NoArgsConstructor(PROTECTED)`
- [ ] 테스트: `@Nested + @DisplayName(한국어)` 패턴
- [ ] 컴파일 게이트: `./gradlew compileJava compileTestJava --no-daemon` 통과 필수

---

## 7. 다음 단계

이 Plan이 완성되면 다음 순서로 진행한다:

1. **Design**: `/pdca design file-upload`
2. **SDD 뼈대**: `/spec-to-skeleton file-upload`
3. **TDD 테스트 작성**: `/skeleton-to-tests file-upload`
4. **구현**: Spec Checklist 항목 하나씩 체크
5. **Gap 분석**: `/pdca analyze file-upload`
6. **완료 보고**: `/pdca report file-upload`

---

## 8. Open Questions (SDD에서 인계)

| # | 질문 | 기본 결정 |
|---|------|----------|
| OQ-01 | MIME 타입 검증: Apache Tika vs 확장자 매핑? | 이터레이션 1은 확장자 매핑으로 간단 처리 |
| OQ-02 | S3 버킷 공개 여부 | 퍼블릭 읽기 URL (보안 이터레이션에서 pre-signed 전환) |
| OQ-03 | MinIO 로컬 개발 Docker Compose 확인 | TBD — 없으면 Mock S3 사용 |
| OQ-04 | `MultipartResolver` 레벨 파일 크기 제한 설정 | `spring.servlet.multipart.max-file-size=50MB` 추가 |
