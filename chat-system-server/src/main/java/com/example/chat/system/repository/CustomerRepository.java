package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 고객 Repository
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 이메일로 조회
     */
    Optional<Customer> findByEmail(String email);

    /**
     * 활성화된 고객 조회
     */
    List<Customer> findByIsActiveTrue();

    /**
     * 마케팅 수신 동의 고객 조회
     */
    List<Customer> findByIsMarketingAgreedTrueAndIsActiveTrue();

    /**
     * 고객명 검색 (페이징)
     */
    @Query("SELECT c FROM Customer c WHERE c.customerName LIKE %:keyword%")
    Page<Customer> searchByCustomerName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 채널 구독 고객 조회
     */
    @Query("SELECT cs.customer FROM ChannelSubscription cs " +
           "WHERE cs.channel.id = :channelId " +
           "AND cs.isSubscribed = true " +
           "AND cs.customer.isActive = true")
    List<Customer> findSubscribedCustomersByChannelId(@Param("channelId") Long channelId);
}