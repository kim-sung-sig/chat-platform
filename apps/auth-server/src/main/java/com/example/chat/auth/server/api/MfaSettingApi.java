package com.example.chat.auth.server.api;

import com.example.chat.auth.server.api.dto.request.VerifyTotpRequest;
import com.example.chat.auth.server.api.dto.response.MfaSettingResponse;
import com.example.chat.auth.server.api.dto.response.TotpSetupResponse;
import com.example.chat.auth.server.application.service.MfaSettingApplicationService;
import com.example.chat.auth.server.common.security.CurrentPrincipalResolver;
import com.example.chat.auth.server.core.domain.MfaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * MFA 설정 관리 API
 * GET    /api/v1/me/mfa               - 등록된 MFA 수단 목록
 * POST   /api/v1/me/mfa/totp/setup    - TOTP 설정 초기화
 * POST   /api/v1/me/mfa/totp/activate - TOTP 활성화
 * DELETE /api/v1/me/mfa/{type}        - MFA 수단 제거
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/me/mfa")
@RequiredArgsConstructor
public class MfaSettingApi {

    private final MfaSettingApplicationService mfaSettingService;
    private final CurrentPrincipalResolver principalResolver;

    @GetMapping
    public ResponseEntity<List<MfaSettingResponse>> listMfaSettings(HttpServletRequest request) {
        UUID principalId = principalResolver.resolve(request);
        return ResponseEntity.ok(mfaSettingService.listMfaSettings(principalId));
    }

    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(HttpServletRequest request) {
        UUID principalId = principalResolver.resolve(request);
        return ResponseEntity.ok(mfaSettingService.initiateTotpSetup(principalId));
    }

    @PostMapping("/totp/activate")
    public ResponseEntity<MfaSettingResponse> activateTotp(
            @Valid @RequestBody VerifyTotpRequest body,
            HttpServletRequest request) {
        UUID principalId = principalResolver.resolve(request);
        return ResponseEntity.ok(mfaSettingService.activateTotp(principalId, body.code()));
    }

    @DeleteMapping("/{type}")
    public ResponseEntity<Void> disableMfa(
            @PathVariable String type,
            HttpServletRequest request) {
        UUID principalId = principalResolver.resolve(request);
        MfaType mfaType = MfaType.valueOf(type.toUpperCase());
        mfaSettingService.disableMfa(principalId, mfaType);
        return ResponseEntity.noContent().build();
    }
}
