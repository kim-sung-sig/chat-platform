package com.example.chat.system.application.service

import com.example.chat.domain.channel.Channel
import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.channel.ChannelRepository
import com.example.chat.domain.channel.ChannelType
import com.example.chat.domain.channel.metadata.ChannelMetadata
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository
import com.example.chat.domain.message.MessageRepository
import com.example.chat.domain.user.UserId
import com.example.chat.domain.user.UserRepository
import com.example.chat.system.application.dto.response.ChannelListItem
import com.example.chat.system.application.query.ChannelListQuery
import com.example.chat.system.application.query.ChannelSortBy
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 채팅방 조회 Query Service
 *
 * CQRS Query Side:
 * - 복잡한 조회 로직
 * - 여러 Aggregate 조인
 * - 필터링/정렬/페이징
 */
@Service
@Transactional(readOnly = true)
class ChannelQueryService(
	private val channelRepository: ChannelRepository,
	private val metadataRepository: ChannelMetadataRepository,
	private val messageRepository: MessageRepository,
	private val userRepository: UserRepository
) {

	fun getChannelList(query: ChannelListQuery): Page<ChannelListItem> {
		logger.debug { "Getting channel list: userId=${query.userId}, filters=$query" }

		val userId = UserId.of(query.userId)

		// 1. 사용자의 채널 목록 조회
		val channels = channelRepository.findByMemberId(userId)
		if (channels.isEmpty()) {
			return Page.empty()
		}

		// 2. 채널 ID 리스트 추출
		val channelIds = channels.map { it.id }

		// 3. 메타데이터 배치 조회
		val metadataMap = metadataRepository.findByChannelIdsAndUserId(channelIds, userId)

		// 4. 마지막 메시지 배치 조회
		val lastMessageMap = messageRepository.findLastMessageByChannelIds(channelIds)

		// 5. ChannelListItem 변환
		var items = channels.map { channel ->
			buildChannelListItem(
				channel,
				metadataMap[channel.id],
				lastMessageMap[channel.id],
				userId
			)
		}

		// 6. 필터링
		items = applyFilters(items, query)

		// 7. 정렬
		items = applySorting(items, query)

		// 8. 페이징
		return applyPagination(items, query)
	}

	private fun buildChannelListItem(
		channel: Channel,
		metadata: ChannelMetadata?,
		lastMessage: com.example.chat.domain.message.Message?,
		currentUserId: UserId
	): ChannelListItem {
		// 메타데이터 정보
		val unreadCount = metadata?.unreadCount ?: 0
		val favorite = metadata?.favorite ?: false
		val pinned = metadata?.pinned ?: false
		val notificationEnabled = metadata?.notificationEnabled ?: true
		val lastReadAt = metadata?.lastReadAt
		val lastActivityAt = metadata?.lastActivityAt

		// 마지막 메시지 정보
		val lastMessageId = lastMessage?.id?.value
		val lastMessageContent = lastMessage?.content?.text
		val lastMessageSenderId = lastMessage?.senderId?.value
		val lastMessageTime = lastMessage?.createdAt

		val lastMessageSenderName = lastMessage?.let { msg ->
			userRepository.findById(msg.senderId)?.username
		}

		// DIRECT 채널인 경우 상대방 정보
		val otherUser = if (channel.type == ChannelType.DIRECT) {
			val otherId = getOtherUserId(channel, currentUserId)
			userRepository.findById(otherId)?.let { user ->
				Triple(otherId.value, user.username, user.email)
			}
		} else null

		// GROUP 채널인 경우 소유자 정보
		val owner = if (channel.type == ChannelType.GROUP) {
			userRepository.findById(channel.ownerId)?.let { user ->
				Pair(channel.ownerId.value, user.username)
			}
		} else null

		return ChannelListItem(
			channelId = channel.id.value,
			channelName = channel.name,
			channelDescription = channel.description,
			channelType = channel.type,
			active = channel.isActive,
			lastMessageId = lastMessageId,
			lastMessageContent = lastMessageContent,
			lastMessageSenderId = lastMessageSenderId,
			lastMessageSenderName = lastMessageSenderName,
			lastMessageTime = lastMessageTime,
			unreadCount = unreadCount,
			favorite = favorite,
			pinned = pinned,
			notificationEnabled = notificationEnabled,
			lastReadAt = lastReadAt,
			lastActivityAt = lastActivityAt,
			memberCount = channel.memberIds.size,
			otherUserId = otherUser?.first,
			otherUserName = otherUser?.second,
			otherUserEmail = otherUser?.third,
			ownerUserId = owner?.first,
			ownerUserName = owner?.second,
			createdAt = channel.createdAt
		)
	}

	private fun getOtherUserId(channel: Channel, myId: UserId): UserId {
		return channel.memberIds.firstOrNull { it != myId } ?: myId
	}

	private fun applyFilters(items: List<ChannelListItem>, query: ChannelListQuery): List<ChannelListItem> {
		return items
			.filter { query.type == null || it.channelType == query.type }
			.filter { query.onlyFavorites != true || it.favorite }
			.filter { query.onlyUnread != true || it.unreadCount > 0 }
			.filter { query.onlyPinned != true || it.pinned }
			.filter { keyword ->
				query.searchKeyword.isNullOrBlank() ||
						keyword.channelName?.lowercase()?.contains(query.searchKeyword.lowercase()) == true ||
						keyword.otherUserName?.lowercase()?.contains(query.searchKeyword.lowercase()) == true
			}
	}

	private fun applySorting(items: List<ChannelListItem>, query: ChannelListQuery): List<ChannelListItem> {
		val sortBy = query.sortBy

		val comparator: Comparator<ChannelListItem> = when (sortBy) {
			ChannelSortBy.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.channelName ?: "" }
			ChannelSortBy.UNREAD_COUNT -> compareBy { it.unreadCount }
			ChannelSortBy.CREATED_AT -> compareBy(nullsLast()) { it.createdAt }
			ChannelSortBy.LAST_ACTIVITY -> {
				compareByDescending<ChannelListItem> { it.pinned }
					.thenBy(nullsLast<java.time.Instant>().reversed()) {
						it.lastActivityAt ?: it.lastMessageTime ?: it.createdAt
					}
			}
		}

		val finalComparator = if (query.direction == ChannelListQuery.SortDirection.ASC) {
			comparator.reversed()
		} else {
			comparator
		}

		return items.sortedWith(finalComparator)
	}

	private fun applyPagination(items: List<ChannelListItem>, query: ChannelListQuery): Page<ChannelListItem> {
		val start = query.page * query.size
		val end = minOf(start + query.size, items.size)

		if (start >= items.size) {
			return PageImpl(
				emptyList(),
				PageRequest.of(query.page, query.size),
				items.size.toLong()
			)
		}

		val pageItems = items.subList(start, end)
		return PageImpl(
			pageItems,
			PageRequest.of(query.page, query.size),
			items.size.toLong()
		)
	}
}
