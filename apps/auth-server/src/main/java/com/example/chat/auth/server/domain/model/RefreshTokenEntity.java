package com.example.chat.auth.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "auth_refresh_token")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RefreshTokenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_refresh_token_id_gen")
	@SequenceGenerator(name = "auth_refresh_token_id_gen", sequenceName = "auth_refresh_token_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_value", length = 512, nullable = false)
	private String tokenValue;

	@Column(name = "expiry_at")
	private Instant expiryAt;

	@Column(name = "create_by")
	private Instant createBy;

	@Column(name = "last_used_at")
	private Instant lastUsedAt;

	public static RefreshTokenEntity from(User user, Token token) {
		Instant now = Instant.now();

		return RefreshTokenEntity.builder()
				.user(user)
				.tokenValue(token.token())
				.expiryAt(token.expiry())
				.createBy(now)
				.lastUsedAt(now)
				.build();
	}

	/**
	 * 갱신이 필요한지 여부
	 * @return 갱신 필요 여부
	 */
	public boolean shouldRefresh() {
		// 만료 3일 이내면 갱신 // TODO 임시값
		return isExpiringWithin(Duration.ofDays(3));
	}

	private boolean isExpiringWithin(Duration duration) {
		return expiryAt.isBefore(Instant.now().plus(duration));
	}

	/**
	 * 토큰 갱신
	 * @param newToken 새로운 토큰 정보
	 */
	public void refresh(Token newToken) {
		Instant now = Instant.now();

		this.tokenValue = newToken.token();
		this.expiryAt = newToken.expiry();
		this.createBy = now;
		this.lastUsedAt = now;
	}

	/**
	 * 토큰 사용 처리
	 */
	public void used() {
		this.lastUsedAt = Instant.now();
	}

	@PrePersist
	private void onCreate(){

	}

}
