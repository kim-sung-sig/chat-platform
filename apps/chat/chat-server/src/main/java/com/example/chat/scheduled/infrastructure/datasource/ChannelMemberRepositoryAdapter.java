package com.example.chat.scheduled.infrastructure.datasource;

import com.example.chat.scheduled.domain.repository.ChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ChannelMemberRepository 도메인 인터페이스 구현체
 *
 * scheduled bounded context에서 채널 멤버 여부 확인을 위한 어댑터.
 * JpaChannelMemberRepository(storage 모듈)를 인프라 레이어에서 위임 호출한다.
 */
@Repository
@RequiredArgsConstructor
public class ChannelMemberRepositoryAdapter implements ChannelMemberRepository {

    private final JpaChannelMemberRepository jpaRepository;

    @Override
    public boolean existsByChannelIdAndUserId(String channelId, String userId) {
        return jpaRepository.existsByChannelIdAndUserId(channelId, userId);
    }
}
