package com.example.chat.auth.server.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 사용자 프로필 - 인증 주체(Principal)의 표시 정보
 * <p>
 * Principal 이 "누구인가(인증)"를 담당한다면,
 * UserProfile 은 "어떻게 보이는가(표현)"를 담당한다.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @Column(name = "principal_id", columnDefinition = "uuid")
    private UUID principalId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "bio", length = 200)
    private String bio;

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

    /** 팩토리 메서드 - 신규 프로필 생성 */
    public static UserProfile create(UUID principalId, String nickname) {
        UserProfile profile = new UserProfile();
        profile.principalId = principalId;
        profile.nickname = Objects.requireNonNull(nickname, "nickname must not be null");
        return profile;
    }

    /** 닉네임 변경 */
    public void changeNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 공백일 수 없습니다");
        }
        if (newNickname.length() > 50) {
            throw new IllegalArgumentException("닉네임은 50자를 초과할 수 없습니다");
        }
        this.nickname = newNickname;
    }

    /** 아바타 URL 변경 */
    public void changeAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /** 전화번호 등록/변경 */
    public void registerPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** 자기소개 변경 */
    public void updateBio(String bio) {
        if (bio != null && bio.length() > 200) {
            throw new IllegalArgumentException("자기소개는 200자를 초과할 수 없습니다");
        }
        this.bio = bio;
    }

    /** 전화번호 등록 여부 */
    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile that)) return false;
        return Objects.equals(principalId, that.principalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalId);
    }
}
