package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * TOTP (Time-based One-Time Password) 서비스
 * Google Authenticator, Authy 등의 Authenticator 앱과 연동한다.
 */
@Service
@Slf4j
public class TotpAuthService {

    private final String issuerName;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public TotpAuthService(
            @Value("${auth.totp.issuer:ChatApplication}") String issuerName) {
        this.issuerName = issuerName;
        this.secretGenerator = new DefaultSecretGenerator(32);

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    /**
     * 새 TOTP secret 생성
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * QR 코드 URL 생성 (otpauth:// URI)
     * 클라이언트가 이 URL 을 QR 코드로 렌더링하여 Authenticator 앱에 스캔 등록한다.
     */
    public String generateQrCodeUrl(String accountName, String secret) {
        QrData qrData = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer(issuerName)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return qrData.getUri();
    }

    /**
     * TOTP 코드 검증
     * 앞뒤 1 time-step (30초) 허용 (시계 오차 보정)
     */
    public void verify(String secret, String code) {
        if (!codeVerifier.isValidCode(secret, code)) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOTP_CODE);
        }
    }

    /**
     * TOTP 코드 유효 여부만 반환 (예외 없이)
     */
    public boolean isValidCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
