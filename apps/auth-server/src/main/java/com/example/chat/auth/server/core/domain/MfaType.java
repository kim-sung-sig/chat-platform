package com.example.chat.auth.server.core.domain;

/**
 * MFA 방식의 유형
 */
public enum MfaType {
    OTP, // SMS, Email, Authenticator App
    BACKUP_CODE
}
