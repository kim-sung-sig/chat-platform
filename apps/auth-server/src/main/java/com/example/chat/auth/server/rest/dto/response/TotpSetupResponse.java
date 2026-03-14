package com.example.chat.auth.server.rest.dto.response;
public record TotpSetupResponse(String secret,String qrCodeUrl,String manualEntryKey){}
