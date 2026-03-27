package com.example.chat.scheduled.domain.repository;

/**
 * 채널 멤버 조회 Port (Domain Interface)
 *
 * scheduled bounded context에서 채널 멤버 여부만 확인하는 최소 인터페이스.
 * infrastructure 레이어의 JpaChannelMemberRepository를 직접 참조하지 않기 위해 분리.
 */
public interface ChannelMemberRepository {

    boolean existsByChannelIdAndUserId(String channelId, String userId);
}
