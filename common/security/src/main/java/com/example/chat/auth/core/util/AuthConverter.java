package com.example.chat.auth.core.util;

import com.example.chat.auth.core.model.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * JWT → AuthenticatedUser 변환기
 * {@link com.example.chat.auth.core.annotation.CurrentUser} 어노테이션에서 사용
 */
@Component("authConverter")
public class AuthConverter {

    public AuthenticatedUser convert(Jwt jwt) {
        return AuthenticatedUser.from(jwt);
    }
}
