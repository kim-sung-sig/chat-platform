package com.example.chat.friendship.application.service;
import com.example.chat.common.core.enums.FriendshipStatus;
import com.example.chat.friendship.rest.dto.response.FriendshipResponse;
import com.example.chat.storage.domain.repository.JpaFriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 친구 관계 Query Service (Phase 2: JPA Entity 직접 사용)
 */
@Service
@Transactional(readOnly = true)
public class FriendshipQueryService {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipQueryService.class);
    private final JpaFriendshipRepository friendshipRepository;
    public FriendshipQueryService(JpaFriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }
    public List<FriendshipResponse> getFriendList(String userId) {
        logger.debug("Getting friend list for user: {}", userId);
        return friendshipRepository.findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc(userId, FriendshipStatus.ACCEPTED).stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }
    public List<FriendshipResponse> getPendingRequests(String userId) {
        logger.debug("Getting pending requests for user: {}", userId);
        return friendshipRepository.findByFriendIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING).stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }
    public List<FriendshipResponse> getSentRequests(String userId) {
        logger.debug("Getting sent requests for user: {}", userId);
        return friendshipRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING).stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }
    public List<FriendshipResponse> getFavoriteFriends(String userId) {
        logger.debug("Getting favorite friends for user: {}", userId);
        return friendshipRepository.findByUserIdAndStatusAndFavoriteTrueOrderByUpdatedAtDesc(userId, FriendshipStatus.ACCEPTED).stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
