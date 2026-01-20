package com.example.chat.auth.server.core.service.oauth;

public interface OAuth2Data {

    String getProvider(); // 제공자 (Ex. naver, google, ...)

    String getProviderId(); // 제공자에서 발급해주는 아이디(번호)

    String getEmail(); // 이메일

    String getName(); // 사용자 실명 (설정한 이름)

    String getNickName(); // 사용자 닉네임

}
