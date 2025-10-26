package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.ChannelSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채널 구독 Repository
 */
@Repository
public interface ChannelSubscriptionRepository extends JpaRepository<ChannelSubscription, Long> {

    /**
     * 고객 ID와 채널 ID로 구독 정보 조회
     */
    Optional<ChannelSubscription> findByCustomerIdAndChannelId(Long customerId, Long channelId);

    /**
     * 고객의 구독 채널 목록
     */
    @Query("SELECT cs FROM ChannelSubscription cs " +
           "WHERE cs.customer.id = :customerId " +
           "AND cs.isSubscribed = true")
    List<ChannelSubscription> findSubscribedChannelsByCustomerId(@Param("customerId") Long customerId);

    /**
     * 채널의 구독자 수
     */
    @Query("SELECT COUNT(cs) FROM ChannelSubscription cs " +
           "WHERE cs.channel.id = :channelId " +
           "AND cs.isSubscribed = true")
    Long countSubscribersByChannelId(@Param("channelId") Long channelId);
}