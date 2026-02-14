
package com.example.chat.auth.server.core.domain

/**
 * 자격 증명의 유형
 * - PASSWORD: 비밀번호
 * - SOCIAL: OAuth (Google, Kakao 등)
 * - PASSKEY: WebAuthn 패스키
 * - OTP: 일회용 비밀번호
 */
enum class CredentialType {
    PASSWORD,
    SOCIAL,
    PASSKEY,
    OTP
}
