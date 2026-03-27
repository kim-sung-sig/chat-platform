-- V11: uploaded_files 테이블 생성 (파일 업로드 메타데이터)

CREATE TABLE IF NOT EXISTS uploaded_files(
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    channel_id        VARCHAR(36)  NOT NULL,
    uploader_id       VARCHAR(36)  NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    s3_key            VARCHAR(500) NOT NULL UNIQUE,
    file_url          VARCHAR(1000),
    file_size         BIGINT       NOT NULL CHECK (file_size > 0),
    mime_type         VARCHAR(100) NOT NULL,
    file_type         VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    uploaded_at       timestamp  NOT NULL
);

create index idx_uploaded_files_channel_status on uploaded_files (channel_id, status);
