package com.example.chat.storage.adapter;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.metadata.ChannelMetadata;
import com.example.chat.domain.channel.metadata.ChannelMetadataId;
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.mapper.ChannelMetadataMapper;
import com.example.chat.storage.repository.JpaChannelMetadataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChannelMetadataRepository 구현체 (Adapter)
 */
@Repository
public class ChannelMetadataRepositoryAdapter implements ChannelMetadataRepository {
    private final JpaChannelMetadataRepository jpaRepository;
    private final ChannelMetadataMapper mapper;

    public ChannelMetadataRepositoryAdapter(JpaChannelMetadataRepository jpaRepository, ChannelMetadataMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ChannelMetadata save(ChannelMetadata metadata) {
        var entity = mapper.toEntity(metadata);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelMetadata> findById(ChannelMetadataId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelMetadata> findByChannelIdAndUserId(ChannelId channelId, UserId userId) {
        return jpaRepository.findByChannelIdAndUserId(channelId.value(), userId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelMetadata> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ChannelId, ChannelMetadata> findByChannelIdsAndUserId(
            List<ChannelId> channelIds,
            UserId userId) {
        List<String> ids = channelIds.stream().map(ChannelId::value).collect(Collectors.toList());

        return jpaRepository.findByChannelIdsAndUserId(ids, userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toMap(ChannelMetadata::getChannelId, it -> it));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelMetadata> findFavoritesByUserId(UserId userId) {
        return jpaRepository.findFavoritesByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelMetadata> findPinnedByUserId(UserId userId) {
        return jpaRepository.findPinnedByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelMetadata> findWithUnreadByUserId(UserId userId) {
        return jpaRepository.findWithUnreadByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(ChannelMetadataId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional
    public void deleteByChannelId(ChannelId channelId) {
        jpaRepository.deleteByChannelId(channelId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByChannelIdAndUserId(ChannelId channelId, UserId userId) {
        return jpaRepository.existsByChannelIdAndUserId(channelId.value(), userId.value());
    }
}
