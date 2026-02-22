package com.example.chat.auth.server.core.service.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {
    KAKAO("kakao"),
    NAVER("naver");

    private final String type;
}
