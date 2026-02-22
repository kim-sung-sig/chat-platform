package com.example.chat.auth.server.core.domain;

import com.example.chat.auth.server.core.domain.credential.Device;
import java.time.Instant;

/**
 * 로그인 시도 한 번의 맥락
 */
public record AuthenticationContext(
        String ipAddress,
        String userAgent,
        String channel,
        Instant attemptTime,
        boolean suspiciousActivity) {
    public Device getDevice() {
        return Device.builder()
                .deviceId(ipAddress) // 임시로 IP를 ID로 사용
                .platform(channel)
                .browser(userAgent)
                .build();
    }
}
