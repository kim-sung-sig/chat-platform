package com.example.chat.system.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객 엔티티
 * 채널 메시지 수신에 동의한 고객
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone_number", columnList = "phone_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_marketing_agreed", nullable = false)
    private Boolean isMarketingAgreed;

    /**
     * 마케팅 수신 동의
     */
    public void agreeToMarketing() {
        this.isMarketingAgreed = true;
    }

    /**
     * 마케팅 수신 거부
     */
    public void disagreeToMarketing() {
        this.isMarketingAgreed = false;
    }

    /**
     * 고객 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 고객 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 고객 정보 수정
     */
    public void updateInfo(String customerName, String phoneNumber) {
        if (customerName != null && !customerName.isBlank()) {
            this.customerName = customerName;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }


}