package com.example.chat.auth.core.util;

import com.example.chat.auth.core.model.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * JWT를 AuthenticatedUser로 변환하는 컨버터
 * @CurrentUser 어노테이션에서 사용됨
 */
@Component("authConverter")
public class AuthConverter {

    public AuthenticatedUser convert(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        return AuthenticatedUser.from(jwt);
    }
}

