package com.example.chat.storage.adapter;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatChannelMemberEntity;
import com.example.chat.storage.mapper.ChannelMapper;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChannelRepository 구현체 (Adapter)
 *
 * Hexagonal Architecture의 Persistence Adapter
 * - 도메인 포트(ChannelRepository)를 구현
 * - JPA 세부사항을 도메인으로부터 격리
 */
@Repository
public class ChannelRepositoryAdapter implements ChannelRepository {

    private final JpaChannelRepository jpaChannelRepository;
    private final JpaChannelMemberRepository jpaChannelMemberRepository;
    private final ChannelMapper channelMapper;

    public ChannelRepositoryAdapter(
            JpaChannelRepository jpaChannelRepository,
            JpaChannelMemberRepository jpaChannelMemberRepository,
            ChannelMapper channelMapper) {
        this.jpaChannelRepository = jpaChannelRepository;
        this.jpaChannelMemberRepository = jpaChannelMemberRepository;
        this.channelMapper = channelMapper;
    }

    @Override
    @Transactional
    public Channel save(Channel channel) {
        ChatChannelEntity entity = channelMapper.toEntity(channel);
        ChatChannelEntity saved = jpaChannelRepository.save(entity);

        // 멤버 동기화: 기존 멤버 삭제 후 재저장
        jpaChannelMemberRepository.deleteByChannelId(saved.getId());
        List<ChatChannelMemberEntity> memberEntities = channelMapper.toMemberEntities(channel);
        jpaChannelMemberRepository.saveAll(memberEntities);

        Set<String> memberIds = channel.getMemberIds().stream()
                .map(UserId::value)
                .collect(Collectors.toSet());

        return channelMapper.toDomain(saved, memberIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Channel> findById(ChannelId id) {
        return jpaChannelRepository.findById(id.value())
                .map(entity -> {
                    Set<String> memberIds = fetchMemberIds(entity.getId());
                    return channelMapper.toDomain(entity, memberIds);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByMemberId(UserId userId) {
        return findByMemberId(userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByMemberId(String userId) {
        // 사용자가 속한 채널 ID 목록 조회
        List<String> channelIds = jpaChannelMemberRepository.findByUserId(userId).stream()
                .map(ChatChannelMemberEntity::getChannelId)
                .collect(Collectors.toList());

        if (channelIds.isEmpty()) {
            return List.of();
        }

        // 채널 엔티티 조회
        List<ChatChannelEntity> channelEntities = jpaChannelRepository.findAllById(channelIds);

        // 멤버 배치 조회
        Map<String, Set<String>> membersByChannelId = fetchMemberIdsBatch(channelIds);

        return channelEntities.stream()
                .map(entity -> channelMapper.toDomain(entity,
                        membersByChannelId.getOrDefault(entity.getId(), Set.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByOwnerId(UserId userId) {
        List<ChatChannelEntity> entities = jpaChannelRepository.findByOwnerId(userId.value());
        List<String> channelIds = entities.stream().map(ChatChannelEntity::getId).collect(Collectors.toList());
        Map<String, Set<String>> membersByChannelId = fetchMemberIdsBatch(channelIds);

        return entities.stream()
                .map(entity -> channelMapper.toDomain(entity,
                        membersByChannelId.getOrDefault(entity.getId(), Set.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findPublicChannels() {
        List<ChatChannelEntity> entities = jpaChannelRepository.findPublicChannels();
        List<String> channelIds = entities.stream().map(ChatChannelEntity::getId).collect(Collectors.toList());
        Map<String, Set<String>> membersByChannelId = fetchMemberIdsBatch(channelIds);

        return entities.stream()
                .map(entity -> channelMapper.toDomain(entity,
                        membersByChannelId.getOrDefault(entity.getId(), Set.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ChannelId id) {
        jpaChannelMemberRepository.deleteByChannelId(id.value());
        jpaChannelRepository.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ChannelId id) {
        return jpaChannelRepository.existsById(id.value());
    }

    // ===== private helpers =====

    private Set<String> fetchMemberIds(String channelId) {
        return jpaChannelMemberRepository.findByChannelId(channelId).stream()
                .map(ChatChannelMemberEntity::getUserId)
                .collect(Collectors.toSet());
    }

    private Map<String, Set<String>> fetchMemberIdsBatch(List<String> channelIds) {
        if (channelIds.isEmpty()) {
            return Map.of();
        }
        return jpaChannelMemberRepository.findByChannelIdIn(channelIds).stream()
                .collect(Collectors.groupingBy(
                        ChatChannelMemberEntity::getChannelId,
                        Collectors.mapping(ChatChannelMemberEntity::getUserId, Collectors.toSet())));
    }
}
