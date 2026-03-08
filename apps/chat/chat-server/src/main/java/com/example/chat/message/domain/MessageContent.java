package com.example.chat.message.domain;

/**
 * 메시지 내용 (Sealed Interface)
 *
 * Phase 4: chat-domain → chat-server 내부 이동
 * 메시지 타입 다형성 - 실제 도메인 가치가 있어 유지
 */
public sealed interface MessageContent {

    boolean isEmpty();

    default String getText() { return null; }
    default String getMediaUrl() { return null; }
    default String getFileName() { return null; }
    default Long getFileSize() { return null; }
    default String getMimeType() { return null; }

    record Text(String text) implements MessageContent {
        public Text {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Text content cannot be null or blank");
            }
        }
        @Override public boolean isEmpty() { return text.isBlank(); }
        public String getText() { return text(); }
    }

    record Image(String mediaUrl, String fileName, Long fileSize) implements MessageContent {
        public Image {
            if (mediaUrl == null || mediaUrl.isBlank()) {
                throw new IllegalArgumentException("Media URL cannot be null or blank");
            }
        }
        @Override public boolean isEmpty() { return mediaUrl.isBlank(); }
        public String getMediaUrl() { return mediaUrl(); }
        public String getFileName() { return fileName(); }
        public Long getFileSize() { return fileSize(); }
    }

    record File(String mediaUrl, String fileName, Long fileSize, String mimeType) implements MessageContent {
        public File {
            if (mediaUrl == null || mediaUrl.isBlank()) {
                throw new IllegalArgumentException("Media URL cannot be null or blank");
            }
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or blank");
            }
        }
        @Override public boolean isEmpty() { return mediaUrl.isBlank(); }
        public String getMediaUrl() { return mediaUrl(); }
        public String getFileName() { return fileName(); }
        public Long getFileSize() { return fileSize(); }
        public String getMimeType() { return mimeType(); }
    }

    static Text text(String text) { return new Text(text); }
    static Image image(String mediaUrl, String fileName, Long fileSize) { return new Image(mediaUrl, fileName, fileSize); }
    static File file(String mediaUrl, String fileName, Long fileSize, String mimeType) { return new File(mediaUrl, fileName, fileSize, mimeType); }
}
