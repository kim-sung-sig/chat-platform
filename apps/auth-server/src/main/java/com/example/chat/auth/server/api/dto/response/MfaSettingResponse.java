package com.example.chat.auth.server.api.dto.response;
import com.example.chat.auth.server.core.domain.mfa.MfaSetting;
import java.time.Instant;
public record MfaSettingResponse(String mfaType,boolean enabled,Instant createdAt){
public static MfaSettingResponse from(MfaSetting s){return new MfaSettingResponse(s.getMfaType().name(),s.isEnabled(),s.getCreatedAt());}
}