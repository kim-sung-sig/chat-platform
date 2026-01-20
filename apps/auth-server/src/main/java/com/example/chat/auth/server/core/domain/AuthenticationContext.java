package com.example.chat.auth.server.core.domain;

import java.time.Instant;
import java.util.Objects;

import com.example.chat.auth.server.core.domain.credential.Device;

/**
 * 로그인 시도 한 번의 맥락
 * - IP 주소
 * - 디바이스 정보
 * - 위험 신호
 * - 채널 (웹, 모바일 앱 등)
 * - 시간대
 *
 * 이 정보는 정책 판단에 사용됨
 */
public class AuthenticationContext {

    private final String ipAddress;
    private final String userAgent;
    private final String channel; // WEB, MOBILE_APP, etc
    private final Instant attemptTime;
    private final boolean suspiciousActivity;

    public AuthenticationContext(String ipAddress, String userAgent, String channel,
            Instant attemptTime, boolean suspiciousActivity) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "ipAddress cannot be null");
        this.userAgent = Objects.requireNonNull(userAgent, "userAgent cannot be null");
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
        this.attemptTime = Objects.requireNonNull(attemptTime, "attemptTime cannot be null");
        this.suspiciousActivity = suspiciousActivity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getChannel() {
        return channel;
    }

    public Instant getAttemptTime() {
        return attemptTime;
    }

    public boolean isSuspiciousActivity() {
        return suspiciousActivity;
    }

    public Device getDevice() {
        return Device.builder()
                .deviceId(ipAddress) // 임시로 IP를 ID로 사용하거나, UserAgent 파싱 필요
                .platform(channel)
                .browser(userAgent)
                .build();
    }

    @Override
    public String toString() {
        return "AuthenticationContext{" +
                "ipAddress='" + ipAddress + '\'' +
                ", channel='" + channel + '\'' +
                ", attemptTime=" + attemptTime +
                ", suspiciousActivity=" + suspiciousActivity +
                '}';
    }
}
