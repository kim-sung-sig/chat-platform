package com.example.chat.common.auth.model;

import lombok.Value;

import java.io.Serializable;

/**
 * 사용자 ID Value Object
 * 불변 객체로 설계하여 도메인 모델의 식별자로 사용
 */
@Value(staticConstructor = "of")
public class UserId implements Serializable {

    Long value;

    public static UserId from(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return UserId.of(id);
    }

    public static UserId from(String idString) {
        try {
            Long id = Long.parseLong(idString);
            return from(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + idString, e);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
