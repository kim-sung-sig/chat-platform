package com.example.chat.storage.adapter;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatChannelMemberEntity;
import com.example.chat.storage.mapper.ChannelMapper;
import com.example.chat.storage.repository.JpaChatChannelMemberRepository;
import com.example.chat.storage.repository.JpaChatChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChannelRepository 구현체 (Adapter)
 */
@Repository
@RequiredArgsConstructor
public class ChannelRepositoryAdapter implements ChannelRepository {

    private final JpaChatChannelRepository jpaChannelRepository;
    private final JpaChatChannelMemberRepository jpaMemberRepository;
    private final ChannelMapper mapper;

    @Override
    @Transactional
    public Channel save(Channel channel) {
        // 1. 채널 엔티티 저장
        ChatChannelEntity channelEntity = mapper.toEntity(channel);
        ChatChannelEntity savedChannel = jpaChannelRepository.save(channelEntity);

        // 2. 기존 멤버 삭제 후 새로 저장 (간단한 구현)
        String channelId = savedChannel.getId();
        jpaMemberRepository.deleteByChannelIdAndUserId(channelId, channel.getOwnerId().getValue());

        // 3. 멤버 엔티티 저장
        List<ChatChannelMemberEntity> memberEntities = mapper.toMemberEntities(channel);
        jpaMemberRepository.saveAll(memberEntities);

        // 4. 저장된 멤버 조회
        Set<String> memberIds = jpaMemberRepository.findByChannelId(channelId).stream()
                .map(ChatChannelMemberEntity::getUserId)
                .collect(Collectors.toSet());

        return mapper.toDomain(savedChannel, memberIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Channel> findById(ChannelId id) {
        return jpaChannelRepository.findById(id.getValue())
                .map(entity -> {
                    Set<String> memberIds = jpaMemberRepository.findByChannelId(entity.getId()).stream()
                            .map(ChatChannelMemberEntity::getUserId)
                            .collect(Collectors.toSet());
                    return mapper.toDomain(entity, memberIds);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByMemberId(UserId userId) {
        List<String> channelIds = jpaMemberRepository.findChannelIdsByUserId(userId.getValue());

        return channelIds.stream()
                .map(ChannelId::of)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByMemberId(String userId) {
        return findByMemberId(UserId.of(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findByOwnerId(UserId userId) {
        List<ChatChannelEntity> entities = jpaChannelRepository.findByOwnerId(userId.getValue());

        return entities.stream()
                .map(entity -> {
                    Set<String> memberIds = jpaMemberRepository.findByChannelId(entity.getId()).stream()
                            .map(ChatChannelMemberEntity::getUserId)
                            .collect(Collectors.toSet());
                    return mapper.toDomain(entity, memberIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> findPublicChannels() {
        List<ChatChannelEntity> entities = jpaChannelRepository.findByChannelTypeAndActive(ChannelType.PUBLIC, true);

        return entities.stream()
                .map(entity -> {
                    Set<String> memberIds = jpaMemberRepository.findByChannelId(entity.getId()).stream()
                            .map(ChatChannelMemberEntity::getUserId)
                            .collect(Collectors.toSet());
                    return mapper.toDomain(entity, memberIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ChannelId id) {
        jpaChannelRepository.deleteById(id.getValue());
        // 멤버는 FK로 cascade 삭제되도록 DB 설정하거나, 여기서 명시적으로 삭제
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ChannelId id) {
        return jpaChannelRepository.existsById(id.getValue());
    }
}
