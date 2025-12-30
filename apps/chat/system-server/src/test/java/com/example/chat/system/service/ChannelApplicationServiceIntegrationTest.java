package com.example.chat.system.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.domain.user.UserStatus;
import com.example.chat.system.application.service.ChannelApplicationService;
import com.example.chat.system.base.IntegrationTestBase;
import com.example.chat.system.dto.response.ChannelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChannelApplicationService 통합 테스트
 * Testcontainers + PostgreSQL 환경에서 실제 DB 연동 테스트
 */
@DisplayName("채널 애플리케이션 서비스 통합 테스트")
@Transactional
class ChannelApplicationServiceIntegrationTest extends IntegrationTestBase {

	@Autowired
	private ChannelApplicationService channelApplicationService;

	@Autowired
	private ChannelRepository channelRepository;

	@Autowired
	private UserRepository userRepository;

	private User testUser1;
	private User testUser2;

	@BeforeEach
	void setUp() {
		// 테스트 사용자 생성
		testUser1 = User.builder()
				.id(UserId.generate())
				.username("testuser1")
				.email("test1@example.com")
				.status(UserStatus.ACTIVE)
				.build();
		userRepository.save(testUser1);

		testUser2 = User.builder()
				.id(UserId.generate())
				.username("testuser2")
				.email("test2@example.com")
				.status(UserStatus.ACTIVE)
				.build();
		userRepository.save(testUser2);
	}

	@Test
	@DisplayName("공개 채널 목록 조회 - 성공")
	void getPublicChannels_Success() {
		// given: Channel.create 팩토리 메서드 사용
		Channel publicChannel = Channel.create("테스트 설명", ChannelType.PUBLIC, testUser1.getId());
		channelRepository.save(publicChannel);

		// when
		List<ChannelResponse> channels = channelApplicationService.getPublicChannels();

		// then
		assertThat(channels).isNotNull();
		assertThat(channels).hasSizeGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("채널 조회 - 성공")
	void getChannel_Success() {
		// given: Channel.create 팩토리 메서드 사용
		Channel channel = Channel.create("테스트 채널", ChannelType.GROUP, testUser1.getId());
		Channel savedChannel = channelRepository.save(channel);

		// when
		var result = channelApplicationService.getChannel(savedChannel.getId().getValue());

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(savedChannel.getId().getValue());
		assertThat(result.getName()).isEqualTo("테스트 채널");
	}
}
