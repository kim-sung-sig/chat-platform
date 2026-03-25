package com.example.chat.file.infrastructure.datasource;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA 레포지토리 */
public interface JpaUploadedFileRepository extends JpaRepository<UploadedFileEntity, String> {
}
