package com.example.chat.auth.server.api

import com.example.chat.auth.server.api.dto.factory.CredentialFactory
import com.example.chat.auth.server.api.dto.request.AuthenticateRequest
import com.example.chat.auth.server.api.dto.request.CompleteMfaRequest
import com.example.chat.auth.server.api.dto.response.AuthResponse
import com.example.chat.auth.server.application.service.AuthenticationApplicationService
import com.example.chat.auth.server.application.service.MfaApplicationService
import com.example.chat.auth.server.core.domain.AuthenticationContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** Authentication Entry Point */
@RestController
@RequestMapping("/api/v1/auth")
class AuthApi(
        private val authenticationService: AuthenticationApplicationService,
        private val mfaService: MfaApplicationService,
        private val credentialFactory: CredentialFactory
) {
    private val log = LoggerFactory.getLogger(AuthApi::class.java)

    companion object {
        private const val REFRESH_TOKEN_COOKIE = "refresh_token"
    }

    /** 인증 실행 (Login) */
    @PostMapping("/authenticate")
    fun authenticate(
            @Valid @RequestBody requestDto: AuthenticateRequest,
            request: HttpServletRequest,
            response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {

        log.info(
                "Authentication request for identifier: {}, type: {}",
                requestDto.identifier,
                requestDto.credentialType
        )

        val providedCredential = credentialFactory.createFromRequest(requestDto)
        val context = createAuthContext(request)

        val result =
                authenticationService.authenticate(
                        requestDto.identifier!!,
                        requestDto.credentialType!!,
                        providedCredential,
                        context
                )

        if (result.authResult.isAuthenticated) {
            result.token?.refreshToken?.let { setRefreshTokenCookie(response, it) }
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult, result.token))
    }

    /** MFA 완료 */
    @PostMapping("/mfa/complete")
    fun completeMfa(
            @Valid @RequestBody requestDto: CompleteMfaRequest,
            request: HttpServletRequest,
            response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {

        log.info("MFA completion request for session: {}", requestDto.mfaSessionId)

        val result =
                mfaService.completeMfa(
                        requestDto.mfaToken!!,
                        requestDto.mfaSessionId!!,
                        requestDto.mfaMethod!!,
                        requestDto.otpCode!!,
                        createAuthContext(request).getDevice()
                )

        if (result.authResult.isAuthenticated) {
            result.token?.refreshToken?.let { setRefreshTokenCookie(response, it) }
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult, result.token))
    }

    /** 토큰 갱신 (Refresh Token Rotation) */
    @PostMapping("/refresh")
    fun refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) refreshToken: String?,
            request: HttpServletRequest,
            response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {

        log.info("Token refresh request")

        if (refreshToken.isNullOrEmpty()) {
            return ResponseEntity.status(401).build()
        }

        val result =
                authenticationService.refreshToken(
                        refreshToken,
                        createAuthContext(request).getDevice()
                )

        if (result.authResult.isAuthenticated) {
            result.token?.refreshToken?.let { setRefreshTokenCookie(response, it) }
        } else {
            return ResponseEntity.status(401).body(AuthResponse.from(result.authResult, null))
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult, result.token))
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    fun logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) refreshToken: String?,
            response: HttpServletResponse
    ): ResponseEntity<Void> {

        log.info("Logout request")

        authenticationService.logout(refreshToken)
        deleteRefreshTokenCookie(response)

        return ResponseEntity.noContent().build()
    }

    private fun createAuthContext(request: HttpServletRequest): AuthenticationContext {
        val ipAddress = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        var channel = request.getHeader("X-Channel")
        if (channel == null) channel = "WEB"

        return AuthenticationContext(
                ipAddress = ipAddress,
                userAgent = userAgent ?: "Unknown",
                channel = channel,
                attemptTime = Instant.now(),
                isSuspiciousActivity = false
        )
    }

    private fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                        .httpOnly(true)
                        .secure(true) // Production에서는 true
                        .path("/")
                        .maxAge(7 * 24 * 60 * 60) // 7일
                        .sameSite("Strict")
                        .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .path("/")
                        .maxAge(0)
                        .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
