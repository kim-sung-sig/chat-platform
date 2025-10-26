package com.example.chat.system.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 구독 엔티티
 * 고객과 채널의 다대다 관계를 관리
 */
@Entity
@Table(name = "channel_subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_channel", columnNames = {"customer_id", "channel_id"})
    },
    indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_channel_id", columnList = "channel_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChannelSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_channel"))
    private Channel channel;

    @Column(name = "is_subscribed", nullable = false)
    private Boolean isSubscribed;

    /**
     * 구독
     */
    public void subscribe() {
        this.isSubscribed = true;
    }

    /**
     * 구독 해지
     */
    public void unsubscribe() {
        this.isSubscribed = false;
    }
}