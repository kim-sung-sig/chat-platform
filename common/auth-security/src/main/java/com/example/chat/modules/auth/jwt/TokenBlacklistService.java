package com.example.chat.modules.auth.jwt;

public interface TokenBlacklistService {
    boolean isBlacklisted(String token);
}
