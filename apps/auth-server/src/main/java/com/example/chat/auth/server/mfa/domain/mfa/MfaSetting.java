package com.example.chat.auth.server.mfa.domain.mfa;

import com.example.chat.auth.server.mfa.domain.MfaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * MFA 설정 - Principal 별 등록된 MFA 수단 관리
 * <p>
 * 각 Principal 은 MFA 수단별로 하나의 MfaSetting 을 가진다.
 * TOTP 의 경우 secret 이 저장되고, 인증 후 enabled 상태로 전환된다.
 */
@Entity
@Table(
    name = "mfa_settings",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_mfa_settings_principal_type",
        columnNames = {"principal_id", "mfa_type"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MfaSetting {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "principal_id", nullable = false, columnDefinition = "uuid")
    private UUID principalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_type", nullable = false, length = 30)
    private MfaType mfaType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * TOTP secret (Base32 인코딩된 값).
     * TOTP 타입일 때만 사용되며, 활성화 전까지는 임시 저장 상태.
     */
    @Column(name = "totp_secret", length = 100)
    private String totpSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /** 팩토리 메서드 - TOTP 설정 초기화 (아직 비활성 상태) */
    public static MfaSetting initTotp(UUID principalId, String totpSecret) {
        MfaSetting setting = new MfaSetting();
        setting.id = UUID.randomUUID();
        setting.principalId = Objects.requireNonNull(principalId);
        setting.mfaType = MfaType.TOTP;
        setting.totpSecret = Objects.requireNonNull(totpSecret, "totpSecret must not be null");
        setting.enabled = false;
        return setting;
    }

    /** 팩토리 메서드 - OTP(SMS/Email) 설정 활성화 */
    public static MfaSetting enableOtp(UUID principalId) {
        MfaSetting setting = new MfaSetting();
        setting.id = UUID.randomUUID();
        setting.principalId = Objects.requireNonNull(principalId);
        setting.mfaType = MfaType.OTP;
        setting.enabled = true;
        return setting;
    }

    /** TOTP 인증 코드 확인 후 활성화 */
    public void activateTotp() {
        if (this.mfaType != MfaType.TOTP) {
            throw new IllegalStateException("TOTP 타입만 이 방법으로 활성화할 수 있습니다");
        }
        if (this.totpSecret == null) {
            throw new IllegalStateException("TOTP secret 이 설정되어 있지 않습니다");
        }
        this.enabled = true;
    }

    /** MFA 비활성화 */
    public void disable() {
        this.enabled = false;
    }

    /** TOTP 설정 여부 (활성화와 무관하게 secret 이 있는지) */
    public boolean hasTotpSecret() {
        return totpSecret != null && !totpSecret.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MfaSetting that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
