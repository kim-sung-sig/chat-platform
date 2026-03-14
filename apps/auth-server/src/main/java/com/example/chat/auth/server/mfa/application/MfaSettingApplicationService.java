package com.example.chat.auth.server.mfa.application;

import com.example.chat.auth.server.rest.dto.response.MfaSettingResponse;
import com.example.chat.auth.server.rest.dto.response.TotpSetupResponse;
import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.mfa.domain.MfaType;
import com.example.chat.auth.server.mfa.domain.mfa.MfaSetting;
import com.example.chat.auth.server.profile.domain.UserProfile;
import com.example.chat.auth.server.mfa.domain.repository.MfaSettingRepository;
import com.example.chat.auth.server.profile.domain.repository.UserProfileRepository;
import com.example.chat.auth.server.mfa.domain.service.TotpAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * MFA 설정 Application Service
 * 등록된 MFA 수단 목록 조회, TOTP 초기화/활성화/비활성화를 오케스트레이션한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MfaSettingApplicationService {

	private final MfaSettingRepository mfaSettingRepository;
	private final UserProfileRepository userProfileRepository;
	private final TotpAuthService totpAuthService;

	/** 등록된 MFA 수단 목록 조회 */
	@Transactional(readOnly = true)
	public List<MfaSettingResponse> listMfaSettings(UUID principalId) {
		return mfaSettingRepository.findAllByPrincipalId(principalId)
				.stream()
				.map(MfaSettingResponse::from)
				.toList();
	}

	/**
	 * TOTP 설정 초기화
	 * secret 을 생성하고 QR 코드 URL 을 반환한다. 아직 활성화되지 않은 상태.
	 * 이미 활성화된 TOTP 가 있으면 예외 발생.
	 */
	@Transactional
	public TotpSetupResponse initiateTotpSetup(UUID principalId) {
		boolean alreadyEnabled = mfaSettingRepository
				.existsByPrincipalIdAndTypeAndEnabled(principalId, MfaType.TOTP, true);
		if (alreadyEnabled) {
			throw new AuthException(AuthServerErrorCode.MFA_ALREADY_ENABLED);
		}

		// 기존 미완료 설정이 있으면 재사용, 없으면 신규 생성
		MfaSetting setting = mfaSettingRepository
				.findByPrincipalIdAndType(principalId, MfaType.TOTP)
				.orElseGet(() -> {
					String secret = totpAuthService.generateSecret();
					MfaSetting newSetting = MfaSetting.initTotp(principalId, secret);
					return mfaSettingRepository.save(newSetting);
				});

		String accountName = resolveAccountName(principalId);
		String qrCodeUrl = totpAuthService.generateQrCodeUrl(accountName, setting.getTotpSecret());

		log.info("TOTP setup initiated for principal: {}", principalId);
		return new TotpSetupResponse(setting.getTotpSecret(), qrCodeUrl, setting.getTotpSecret());
	}

	/**
	 * TOTP 활성화 확인
	 * 사용자가 Authenticator 앱에 등록 후 코드를 입력하면 활성화 완료.
	 */
	@Transactional
	public MfaSettingResponse activateTotp(UUID principalId, String code) {
		MfaSetting setting = mfaSettingRepository
				.findByPrincipalIdAndType(principalId, MfaType.TOTP)
				.orElseThrow(() -> new AuthException(AuthServerErrorCode.TOTP_SETUP_REQUIRED));

		totpAuthService.verify(setting.getTotpSecret(), code);
		setting.activateTotp();

		MfaSetting saved = mfaSettingRepository.save(setting);
		log.info("TOTP activated for principal: {}", principalId);
		return MfaSettingResponse.from(saved);
	}

	/** MFA 수단 비활성화/제거 */
	@Transactional
	public void disableMfa(UUID principalId, MfaType mfaType) {
		MfaSetting setting = mfaSettingRepository
				.findByPrincipalIdAndType(principalId, mfaType)
				.orElseThrow(() -> new AuthException(AuthServerErrorCode.MFA_NOT_FOUND));

		mfaSettingRepository.delete(setting);
		log.info("MFA type {} disabled for principal: {}", mfaType, principalId);
	}

	private String resolveAccountName(UUID principalId) {
		return userProfileRepository.findByPrincipalId(principalId)
				.map(UserProfile::getNickname)
				.orElse(principalId.toString());
	}
}
