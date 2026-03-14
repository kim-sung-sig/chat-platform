package com.example.chat.auth.server.mfa.domain;

/**
 * MFA 방식의 유형
 */
public enum MfaType {
    OTP,         // SMS, Email OTP
    TOTP,        // Time-based OTP (Google Authenticator, Authy)
    BACKUP_CODE  // 백업 코드
}
