package com.example.chat.auth.server.rest.dto.response;
import com.example.chat.auth.server.profile.domain.UserProfile;
import java.time.Instant;
import java.util.UUID;
public record ProfileResponse(UUID principalId,String nickname,String avatarUrl,String phoneNumber,String bio,Instant createdAt,Instant updatedAt){
public static ProfileResponse from(UserProfile p){return new ProfileResponse(p.getPrincipalId(),p.getNickname(),p.getAvatarUrl(),p.getPhoneNumber(),p.getBio(),p.getCreatedAt(),p.getUpdatedAt());}
}
