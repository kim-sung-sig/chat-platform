package com.example.chat.auth.server.core.service.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SocialType {
    KAKAO("kakao"),
    NAVER("naver"),
    // GOOGLE("google"),
    // APPLE("apple"),
    ;

    private final String type;

}
