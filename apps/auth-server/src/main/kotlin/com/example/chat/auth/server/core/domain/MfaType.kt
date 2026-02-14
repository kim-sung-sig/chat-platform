
package com.example.chat.auth.server.core.domain

/**
 * MFA 방식의 유형
 * - OTP: 일회용 비밀번호 (SMS, Email, Authenticator)
 * - BACKUP_CODE: 백업 코드
 */
enum class MfaType {
    OTP,           // SMS, Email, Authenticator App
    BACKUP_CODE
}
