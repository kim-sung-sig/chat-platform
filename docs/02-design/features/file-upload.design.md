# Design: file-upload

> **Feature**: 채팅 파일 업로드 API
> **Date**: 2026-03-25
> **Phase**: design
> **Plan**: [file-upload.plan.md](../../01-plan/features/file-upload.plan.md)
> **SDD**: [SDD_file-upload.md](../../specs/SDD_file-upload.md)

---

## 1. 설계 범위

이 설계는 Plan의 **Spec Checklist** 전체 항목을 구현 대상으로 한다.

| 그룹 | 항목 |
|------|------|
| 도메인 모델 | `UploadedFile` Aggregate Root, `UploadStatus`, `FileType` enum, `S3Key` VO |
| Application | `FileUploadCommandServiceImpl`, `FileQueryServiceImpl` |
| Infrastructure | `S3StorageServiceImpl` (AWS SDK v2), `UploadedFileEntity`, Repository Adapter |
| REST | `FileUploadController` (POST/GET), `FileUploadResponse` Java record |
| DB | Flyway V11 `uploaded_files` 테이블 |
| 에러코드 | `ChatErrorCode` 5개 추가 |
| 빌드 | AWS SDK v2 의존성 추가 |

---

## 2. DDD 레이어 설계

### 2.1 패키지 구조

```
apps/chat/chat-server/src/main/java/com/example/chat/
└── file/
    ├── domain/
    │   ├── model/
    │   │   ├── UploadedFile.java          # Aggregate Root (순수 POJO)
    │   │   ├── UploadStatus.java          # enum: PENDING | COMPLETED | FAILED
    │   │   ├── FileType.java              # enum: IMAGE | DOCUMENT | AUDIO | VIDEO | OTHER
    │   │   └── S3Key.java                 # Value Object
    │   ├── repository/
    │   │   └── UploadedFileRepository.java  # Port (interface)
    │   └── service/
    │       └── S3StorageService.java        # 외부 스토리지 Port (interface)
    ├── application/
    │   └── service/
    │       ├── FileUploadCommandService.java      # Command Port
    │       ├── FileUploadCommandServiceImpl.java  # 구현
    │       ├── FileQueryService.java              # Query Port
    │       └── FileQueryServiceImpl.java          # 구현
    ├── infrastructure/
    │   ├── datasource/
    │   │   ├── UploadedFileEntity.java            # JPA Entity
    │   │   ├── JpaUploadedFileRepository.java     # Spring Data JPA
    │   │   └── UploadedFileRepositoryAdapter.java # Adapter
    │   └── storage/
    │       └── S3StorageServiceImpl.java          # AWS SDK v2 구현
    └── rest/
        ├── controller/
        │   └── FileUploadController.java
        └── dto/
            └── response/
                └── FileUploadResponse.java        # Java record
```

### 2.2 의존성 방향

```
rest → application (interface)
application → domain
infrastructure → domain (interface 구현)
application → infrastructure (Spring DI)
domain ← (no external deps)
```

---

## 3. 도메인 모델 설계

### 3.1 UploadedFile (Aggregate Root)

```java
public class UploadedFile {

    private final String id;              // UUID
    private final String channelId;
    private final String uploaderId;
    private final String originalFileName; // 정규화 전 원본명
    private final String s3Key;           // {channelId}/{UUID}/{normalizedFileName}
    private String fileUrl;               // COMPLETED 후 채워짐
    private final long fileSize;          // bytes
    private final String mimeType;
    private final FileType fileType;
    private UploadStatus status;          // PENDING → COMPLETED | FAILED
    private final ZonedDateTime uploadedAt;

    // 도메인 행위
    public void markCompleted(String fileUrl) { ... }  // PENDING → COMPLETED
    public void markFailed()               { ... }     // PENDING → FAILED

    // 불변식
    // - fileSize > 0
    // - IMAGE 타입 fileSize ≤ 10MB
    // - 기타 타입 fileSize ≤ 50MB
    // - status 전이: PENDING → COMPLETED | FAILED (단방향)
}
```

### 3.2 S3Key (Value Object)

```java
public record S3Key(String value) {
    // 형식: {channelId}/{uuid}/{normalizedFileName}
    // 생성 팩토리: S3Key.of(channelId, originalFileName)
    //   → UUID.randomUUID() + 파일명 정규화 (공백→_, 특수문자 제거)
    public S3Key {
        if (value == null || !value.matches(".+/.+/.+"))
            throw new IllegalArgumentException("Invalid S3Key format: " + value);
    }
    public static S3Key of(String channelId, String originalFileName) { ... }
}
```

### 3.3 UploadStatus / FileType

```java
public enum UploadStatus { PENDING, COMPLETED, FAILED }

public enum FileType {
    IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER;

    private static final Map<String, FileType> MIME_MAP = Map.ofEntries(
        Map.entry("image/jpeg",       IMAGE),
        Map.entry("image/png",        IMAGE),
        Map.entry("image/gif",        IMAGE),
        Map.entry("image/webp",       IMAGE),
        Map.entry("application/pdf",  DOCUMENT),
        Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOCUMENT),
        Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       DOCUMENT),
        Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", DOCUMENT),
        Map.entry("audio/mpeg",  AUDIO),
        Map.entry("audio/aac",   AUDIO),
        Map.entry("video/mp4",   VIDEO),
        Map.entry("video/webm",  VIDEO)
    );

    public static FileType from(String mimeType) {
        return MIME_MAP.getOrDefault(mimeType, OTHER);
    }

    public boolean isAllowed() { return this != OTHER; }
}
```

---

## 4. Application 서비스 설계

### 4.1 FileUploadCommandServiceImpl

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileUploadCommandServiceImpl implements FileUploadCommandService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;   // 10 MB
    private static final long MAX_FILE_SIZE  = 50L * 1024 * 1024;   // 50 MB

    private final UploadedFileRepository fileRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final S3StorageService s3StorageService;

    @Override
    public FileUploadResponse uploadFile(String uploaderId,
                                         String channelId,
                                         MultipartFile file) {
        // 1. 채널 멤버십 검증
        // 2. 파일 빈 값 검증
        // 3. MIME 타입 → FileType 결정 (허용 여부 검증)
        // 4. 파일 크기 검증 (IMAGE: 10MB, 기타: 50MB)
        // 5. S3Key 생성
        // 6. UploadedFile 도메인 생성 (PENDING)
        // 7. DB 저장 (PENDING)
        // 8. S3 업로드 (s3StorageService.upload)
        //    - 성공: domain.markCompleted(fileUrl)
        //    - 실패: domain.markFailed() → DB 저장 → FILE_UPLOAD_FAILED 예외
        // 9. DB 저장 (COMPLETED)
        // 10. FileUploadResponse.from(domain) 반환
    }
}
```

### 4.2 FileQueryServiceImpl

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileQueryServiceImpl implements FileQueryService {

    private final UploadedFileRepository fileRepository;

    @Override
    public FileUploadResponse getFile(String fileId) {
        return fileRepository.findById(fileId)
            .map(FileUploadResponse::from)
            .orElseThrow(() -> new ChatException(ChatErrorCode.FILE_NOT_FOUND));
    }
}
```

---

## 5. Infrastructure 설계

### 5.1 S3StorageService (Port Interface)

```java
// domain/service/S3StorageService.java
public interface S3StorageService {
    /**
     * S3에 파일 업로드
     * @return 퍼블릭 접근 가능한 fileUrl
     */
    String upload(String s3Key, byte[] content, String contentType) throws FileUploadFailedException;
}
```

### 5.2 S3StorageServiceImpl (AWS SDK v2)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl implements S3StorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;  // @ConfigurationProperties

    @Override
    public String upload(String s3Key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(s3Properties.getBucketName())
            .key(s3Key)
            .contentType(contentType)
            .contentLength((long) content.length)
            .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));

        return "https://%s.s3.%s.amazonaws.com/%s"
            .formatted(s3Properties.getBucketName(), s3Properties.getRegion(), s3Key);
    }
}
```

### 5.3 S3Properties

```java
@ConfigurationProperties(prefix = "aws.s3")
@Getter @Setter
public class S3Properties {
    private String bucketName;
    private String region;
    private String endpoint;     // MinIO 로컬 개발용 (비어 있으면 AWS 기본값 사용)
    private String accessKey;
    private String secretKey;
}
```

### 5.4 UploadedFileEntity

```java
@Entity
@Table(name = "uploaded_files", indexes = {
    @Index(name = "idx_uploaded_files_channel",  columnList = "channel_id"),
    @Index(name = "idx_uploaded_files_uploader", columnList = "uploader_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFileEntity {

    @Id @Column(name = "id", length = 36)           private String id;
    @Column(name = "channel_id",  length = 36)      private String channelId;
    @Column(name = "uploader_id", length = 36)      private String uploaderId;
    @Column(name = "original_file_name", length = 255) private String originalFileName;
    @Column(name = "s3_key",     length = 500,  unique = true) private String s3Key;
    @Column(name = "file_url",   length = 1000)     private String fileUrl;
    @Column(name = "file_size")                     private long fileSize;
    @Column(name = "mime_type",  length = 100)      private String mimeType;
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type",  length = 20)       private FileType fileType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",     length = 20)       private UploadStatus status;
    @Column(name = "uploaded_at")                   private ZonedDateTime uploadedAt;

    public static UploadedFileEntity fromDomain(UploadedFile domain) { ... }
    public UploadedFile toDomain() { ... }
}
```

---

## 6. REST API 설계

### 6.1 FileUploadController

```java
@Tag(name = "File", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadCommandService commandService;
    private final FileQueryService queryService;

    @Operation(summary = "파일 업로드")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "업로드 성공"),
        @ApiResponse(responseCode = "400", description = "빈 파일"),
        @ApiResponse(responseCode = "403", description = "채널 미가입"),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과"),
        @ApiResponse(responseCode = "415", description = "허용되지 않는 파일 타입"),
        @ApiResponse(responseCode = "500", description = "업로드 실패")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileUploadResponse upload(
            @RequestParam("channelId") String channelId,
            @RequestParam("file")     MultipartFile file) {
        String uploaderId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new IllegalStateException("Not authenticated"));
        return commandService.uploadFile(uploaderId, channelId, file);
    }

    @Operation(summary = "파일 메타데이터 조회")
    @GetMapping("/{fileId}")
    public FileUploadResponse getFile(@PathVariable String fileId) {
        return queryService.getFile(fileId);
    }
}
```

### 6.2 FileUploadResponse (Java Record)

```java
public record FileUploadResponse(
    String fileId,
    String fileUrl,
    String fileName,
    long   fileSize,
    String mimeType,
    FileType fileType,
    ZonedDateTime uploadedAt
) {
    public static FileUploadResponse from(UploadedFile domain) {
        return new FileUploadResponse(
            domain.getId(),
            domain.getFileUrl(),
            domain.getOriginalFileName(),
            domain.getFileSize(),
            domain.getMimeType(),
            domain.getFileType(),
            domain.getUploadedAt()
        );
    }
}
```

---

## 7. 에러코드 추가 (ChatErrorCode)

```java
// 기존 ChatErrorCode enum에 추가
FILE_EMPTY(HttpStatus.BAD_REQUEST,            "CHAT-FILE-001", "파일이 비어 있습니다."),
FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "CHAT-FILE-002", "파일 크기가 허용 한도를 초과했습니다."),
FILE_TYPE_NOT_ALLOWED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "CHAT-FILE-003", "허용되지 않는 파일 형식입니다."),
FILE_NOT_FOUND(HttpStatus.NOT_FOUND,          "CHAT-FILE-004", "파일을 찾을 수 없습니다."),
FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT-FILE-005", "파일 업로드에 실패했습니다."),
```

---

## 8. 데이터베이스 설계

### V11 Flyway 마이그레이션

```sql
-- V11__uploaded_files.sql
CREATE TABLE uploaded_files (
    id                VARCHAR(36)   PRIMARY KEY,
    channel_id        VARCHAR(36)   NOT NULL,
    uploader_id       VARCHAR(36)   NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    s3_key            VARCHAR(500)  NOT NULL UNIQUE,
    file_url          VARCHAR(1000),
    file_size         BIGINT        NOT NULL,
    mime_type         VARCHAR(100)  NOT NULL,
    file_type         VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    uploaded_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_file_size CHECK (file_size > 0)
);

CREATE INDEX idx_uploaded_files_channel  ON uploaded_files(channel_id);
CREATE INDEX idx_uploaded_files_uploader ON uploaded_files(uploader_id);
```

---

## 9. 빌드 의존성

```groovy
// apps/chat/chat-server/build.gradle에 추가
// AWS SDK v2 (파일 업로드)
implementation 'software.amazon.awssdk:s3:2.25.0'
implementation 'software.amazon.awssdk:auth:2.25.0'
```

### application.yml 추가

```yaml
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET:chat-files-local}
    region: ${AWS_REGION:ap-northeast-2}
    endpoint: ${AWS_S3_ENDPOINT:}
    access-key: ${AWS_ACCESS_KEY:}
    secret-key: ${AWS_SECRET_KEY:}

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 52MB
```

---

## 10. 테스트 설계

### 10.1 단위 테스트 목록

| 클래스 | 테스트 시나리오 | 수 |
|--------|----------------|:--:|
| `UploadedFileTest` | 정상 생성, `markCompleted()`, `markFailed()`, 불변식 검증 | 5 |
| `S3KeyTest` | 정상 생성, 잘못된 형식 거부, `of()` 팩토리 | 3 |
| `FileTypeTest` | MIME 타입별 FileType 결정, OTHER 처리 | 4 |
| `FileUploadCommandServiceImplTest` | 정상 업로드, 크기 초과(이미지/기타), 타입 미허용, 빈 파일, 채널 미가입, S3 실패 | 7 |
| `FileQueryServiceImplTest` | 정상 조회, 없는 fileId | 2 |
| `UploadedFileEntityTest` | `fromDomain()`, `toDomain()` 변환 | 2 |

**총 단위 테스트: 23개**

### 10.2 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadCommandServiceImpl 단위 테스트")
class FileUploadCommandServiceImplTest {

    @Mock UploadedFileRepository fileRepository;
    @Mock JpaChannelMemberRepository channelMemberRepository;
    @Mock S3StorageService s3StorageService;
    @InjectMocks FileUploadCommandServiceImpl service;

    @Nested
    @DisplayName("파일 업로드 (uploadFile)")
    class UploadFile {
        @Test @DisplayName("정상 업로드 — 이미지 5MB → COMPLETED 전이 및 S3 upload 호출")
        void givenValidImage_whenUpload_thenCompleted() { ... }

        @Test @DisplayName("이미지 파일 크기 초과 — 11MB → FILE_SIZE_EXCEEDED")
        void givenImageOver10MB_whenUpload_thenException() { ... }
        // ...
    }
}
```

---

## 11. 구현 순서 (Do Phase 가이드)

```
Step 1. 에러코드 추가
  → common/core/.../ChatErrorCode.java (5개 추가)

Step 2. 도메인 모델
  → UploadStatus.java
  → FileType.java (MIME_MAP 포함)
  → S3Key.java (record + 팩토리)
  → UploadedFile.java (Aggregate Root)

Step 3. Domain Port 인터페이스
  → UploadedFileRepository.java
  → S3StorageService.java

Step 4. Infrastructure
  → V11__uploaded_files.sql (Flyway)
  → UploadedFileEntity.java (JPA)
  → JpaUploadedFileRepository.java
  → UploadedFileRepositoryAdapter.java
  → S3Properties.java (@ConfigurationProperties)
  → S3StorageServiceImpl.java (AWS SDK v2)

Step 5. Application
  → FileUploadCommandService.java (interface)
  → FileUploadCommandServiceImpl.java
  → FileQueryService.java (interface)
  → FileQueryServiceImpl.java

Step 6. REST
  → FileUploadResponse.java (record)
  → FileUploadController.java

Step 7. 빌드 설정
  → build.gradle AWS SDK 의존성
  → application.yml aws.s3 + multipart 설정

Step 8. 단위 테스트 (23개)
  → TDD: 테스트 먼저 → 구현

Step 9. 컴파일 게이트
  → ./gradlew compileJava compileTestJava --no-daemon
```

---

## 12. 관련 문서

| 단계 | 문서 |
|------|------|
| Plan | [file-upload.plan.md](../../01-plan/features/file-upload.plan.md) |
| SDD  | [SDD_file-upload.md](../../specs/SDD_file-upload.md) |
| Skeleton | TBD (`/spec-to-skeleton file-upload`) |
| Tests    | TBD (`/skeleton-to-tests file-upload`) |
