package com.example.chat.file.infrastructure.datasource;

import com.example.chat.file.domain.model.UploadedFile;
import com.example.chat.file.domain.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Port/Adapter — UploadedFileRepository 구현체 */
@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryAdapter implements UploadedFileRepository {

    private final JpaUploadedFileRepository jpaRepository;

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        UploadedFileEntity entity = UploadedFileEntity.fromDomain(uploadedFile);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<UploadedFile> findById(String id) {
        return jpaRepository.findById(id).map(UploadedFileEntity::toDomain);
    }
}
