package com.example.chat.auth.server.core.domain

import com.example.chat.auth.server.core.domain.credential.Device
import java.time.Instant

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
data class AuthenticationContext(
        val ipAddress: String,
        val userAgent: String,
        val channel: String,
        val attemptTime: Instant,
        val isSuspiciousActivity: Boolean
) {
    fun getDevice(): Device {
        return Device(
                deviceId = ipAddress, // 임시로 IP를 ID로 사용하거나, UserAgent 파싱 필요
                platform = channel,
                browser = userAgent
        )
    }
}
