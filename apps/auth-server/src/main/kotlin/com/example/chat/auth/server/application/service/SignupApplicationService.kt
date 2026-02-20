package com.example.chat.auth.server.application.service

import com.example.chat.auth.server.api.dto.request.SignupRequest
import com.example.chat.auth.server.api.dto.response.SignupResponse
import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.common.exception.AuthServerErrorCode
import com.example.chat.auth.server.core.domain.Principal
import com.example.chat.auth.server.core.domain.PrincipalType
import com.example.chat.auth.server.core.domain.credential.PasswordCredential
import com.example.chat.auth.server.core.repository.CredentialRepository
import com.example.chat.auth.server.core.repository.PrincipalRepository
import com.example.chat.auth.server.core.service.PasswordAuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/** 회원가입 Application Service */
@Service
class SignupApplicationService(
    private val principalRepository: PrincipalRepository,
    private val credentialRepository: CredentialRepository,
    private val passwordAuthService: PasswordAuthService
) {
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        log.info { "Signup request for identifier: ${request.email}" }

        // 1. 비밀번호 길이 검증
        if (request.password.length < 8) {
            throw AuthException(AuthServerErrorCode.WEAK_PASSWORD)
        }

        // 2. 중복 이메일 확인
        if (principalRepository.findByIdentifier(request.email).isPresent) {
            throw AuthException(AuthServerErrorCode.DUPLICATE_IDENTIFIER)
        }

        // 3. Principal 생성 (identifier = email)
        val principal = Principal(
            id = UUID.randomUUID(),
            identifier = request.email,
            type = PrincipalType.USER,
            active = true
        )
        principalRepository.save(principal)

        // 4. 비밀번호 해싱 후 Credential 저장
        val hashedPassword = passwordAuthService.hashPassword(request.password)
        val credential = PasswordCredential(hashedPassword, true)
        credentialRepository.save(principal.id, credential)

        log.info { "Signup completed for principal: ${principal.id}" }

        return SignupResponse(
            principalId = principal.id,
            identifier = principal.identifier
        )
    }
}
