package com.example.chat.auth.server.infrastructure.persistence

import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.credential.*
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "credentials")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "credential_type", discriminatorType = DiscriminatorType.STRING)
abstract class CredentialEntity(
        @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
        @Column(name = "principal_id", nullable = false) val principalId: UUID,
        @Column(name = "is_verified", nullable = false) val isVerified: Boolean = false,
        @Column(name = "created_at", nullable = false, updatable = false)
        val createdAt: Instant = Instant.now()
) {
    @Column(name = "credential_type", insertable = false, updatable = false)
    val type: String? = null

    abstract fun toDomain(): Credential

    companion object {
        fun fromDomain(principalId: UUID, domain: Credential): CredentialEntity {
            return when (domain) {
                is PasswordCredential ->
                        PasswordCredentialEntity(
                                principalId = principalId,
                                hashedPassword = domain.hashedPassword ?: "",
                                isVerified = domain.isVerified
                        )
                is SocialCredential ->
                        SocialCredentialEntity(
                                principalId = principalId,
                                provider = domain.provider,
                                socialUserId = domain.socialUserId,
                                email = domain.email,
                                isVerified = domain.isVerified
                        )
                is PasskeyCredential ->
                        PasskeyCredentialEntity(
                                principalId = principalId,
                                credentialId = domain.credentialId,
                                publicKey = domain.publicKey,
                                authenticatorName = domain.authenticatorName,
                                isVerified = domain.isVerified
                        )
                is OtpCredential ->
                        OtpCredentialEntity(
                                principalId = principalId,
                                code = domain.code,
                                deliveryMethod = domain.deliveryMethod
                        )
                else ->
                        throw IllegalArgumentException(
                                "Unsupported credential type: ${domain.type}"
                        )
            }
        }
    }
}

@Entity
@DiscriminatorValue("PASSWORD")
class PasswordCredentialEntity(
        principalId: UUID,
        @Column(name = "hashed_password") val hashedPassword: String,
        isVerified: Boolean
) : CredentialEntity(principalId = principalId, isVerified = isVerified) {
    override fun toDomain(): Credential = PasswordCredential(hashedPassword, isVerified)
}

@Entity
@DiscriminatorValue("SOCIAL")
class SocialCredentialEntity(
        principalId: UUID,
        @Column(name = "provider") val provider: String,
        @Column(name = "social_user_id") val socialUserId: String,
        @Column(name = "email") val email: String?,
        isVerified: Boolean
) : CredentialEntity(principalId = principalId, isVerified = isVerified) {
    override fun toDomain(): Credential =
            SocialCredential(provider, socialUserId, email, isVerified)
}

@Entity
@DiscriminatorValue("PASSKEY")
class PasskeyCredentialEntity(
        principalId: UUID,
        @Column(name = "credential_id") val credentialId: String,
        @Column(name = "public_key", length = 1024) val publicKey: String,
        @Column(name = "authenticator_name") val authenticatorName: String?,
        isVerified: Boolean
) : CredentialEntity(principalId = principalId, isVerified = isVerified) {
    override fun toDomain(): Credential =
            PasskeyCredential(credentialId, publicKey, authenticatorName, isVerified)
}

@Entity
@DiscriminatorValue("OTP")
class OtpCredentialEntity(
        principalId: UUID,
        @Column(name = "otp_code") val code: String,
        @Column(name = "delivery_method") val deliveryMethod: String
) : CredentialEntity(principalId = principalId, isVerified = false) {
    override fun toDomain(): Credential = OtpCredential(code, deliveryMethod)
}
