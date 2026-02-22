package com.example.chat.domain.message;

/**
 * 메시지 내용 (Sealed Interface)
 */
public sealed interface MessageContent {

    /**
     * 내용이 비어있는지 확인
     */
    boolean isEmpty();

    // JavaBean 스타일 접근자를 인터페이스에 선언하여
    // MessageContent 타입에서 바로 호출 가능하도록 기본 구현을 제공합니다.
    default String getText() { return null; }
    default String getMediaUrl() { return null; }
    default String getFileName() { return null; }
    default Long getFileSize() { return null; }
    default String getMimeType() { return null; }

    /**
     * 텍스트 메시지 내용
     */
    record Text(String text) implements MessageContent {
        public Text {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Text content cannot be null or blank");
            }
        }

        @Override
        public boolean isEmpty() {
            return text.isBlank();
        }

        // JavaBean 호환 getter
        public String getText() { return text(); }
    }

    /**
     * 이미지 메시지 내용
     */
    record Image(String mediaUrl, String fileName, Long fileSize) implements MessageContent {
        public Image {
            if (mediaUrl == null || mediaUrl.isBlank()) {
                throw new IllegalArgumentException("Media URL cannot be null or blank");
            }
        }

        @Override
        public boolean isEmpty() {
            return mediaUrl.isBlank();
        }

        // JavaBean 호환 getters
        public String getMediaUrl() { return mediaUrl(); }
        public String getFileName() { return fileName(); }
        public Long getFileSize() { return fileSize(); }
    }

    /**
     * 파일 메시지 내용
     */
    record File(String mediaUrl, String fileName, Long fileSize, String mimeType) implements MessageContent {
        public File {
            if (mediaUrl == null || mediaUrl.isBlank()) {
                throw new IllegalArgumentException("Media URL cannot be null or blank");
            }
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or blank");
            }
        }

        @Override
        public boolean isEmpty() {
            return mediaUrl.isBlank();
        }

        // JavaBean 호환 getters
        public String getMediaUrl() { return mediaUrl(); }
        public String getFileName() { return fileName(); }
        public Long getFileSize() { return fileSize(); }
        public String getMimeType() { return mimeType(); }
    }

    // Static factory methods to maintain compatibility or ease of use
    static Text text(String text) {
        return new Text(text);
    }

    static Image image(String mediaUrl, String fileName, Long fileSize) {
        return new Image(mediaUrl, fileName, fileSize);
    }

    static File file(String mediaUrl, String fileName, Long fileSize, String mimeType) {
        return new File(mediaUrl, fileName, fileSize, mimeType);
    }
}
