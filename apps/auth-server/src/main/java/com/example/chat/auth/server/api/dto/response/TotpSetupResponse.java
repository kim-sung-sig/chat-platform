package com.example.chat.auth.server.api.dto.response;
public record TotpSetupResponse(String secret,String qrCodeUrl,String manualEntryKey){}