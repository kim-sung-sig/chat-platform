package com.example.chat.domain.service.register;

import java.util.Set;

/**
 * Domain-level ChatRoomRegister: 채팅방-참여자 매핑 관리를 위한 포트
 */
public interface ChatRoomRegister {
    void addUserToChannel(String channelId, String userId, String sessionMeta);
    void removeUserFromChannel(String channelId, String userId);
    Set<String> listUsers(String channelId);
}