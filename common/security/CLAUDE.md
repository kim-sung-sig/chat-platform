# common/security - CLAUDE.md

Shared library for **security utilities & encryption**.

## Overview

**Purpose**: Password hashing, encryption, token utilities, S2S auth  
**Used by**: auth-server, chat, push-service  
**Stack**: Spring Security (BCrypt), Nimbus JOSE (JWT), TweetNaCl (symmetric encryption)

## Package Structure

```
common:security/src/main/java/com/example/chat/common/
├── security/
│   ├── password/
│   │   ├── PasswordEncryptor.java   # BCrypt wrapper
│   │   └── PasswordValidator.java   # Strength check
│   ├── crypto/
│   │   ├── AesEncryptor.java        # AES-256 GCM
│   │   ├── HmacSigner.java          # HMAC-SHA256
│   │   └── SecureRandom.java        # Random token gen
│   ├── jwt/
│   │   ├── JwtTokenProvider.java    # Create/validate JWT
│   │   ├── JwtClaims.java           # Standard claims
│   │   └── RefreshTokenProvider.java
│   ├── s2s/
│   │   └── ServiceAuthenticator.java # Verify S2S JWT
│   └── config/
│       └── SecurityAutoConfiguration.java
└── resources/
    └── application-security.yml
```

## Key Components

### 1. Password Encryption
- **Algorithm**: BCrypt (Spring Security default)
- **Strength**: 12 rounds
- **Usage**: Auth server only (DO NOT store plaintext passwords)

```java
PasswordEncryptor enc = new PasswordEncryptor();
String hashed = enc.hash("myPassword123!");
boolean match = enc.matches("myPassword123!", hashed);
```

### 2. JWT Token Provider
- **Header**: `{"alg":"HS256","typ":"JWT"}`
- **Claims**: `sub`, `email`, `roles`, `exp`, `iat`, `jti`
- **Signing**: HMAC-SHA256 (secret key from environment)
- **Expiry**: Access token 15m, refresh token 7d

```java
JwtTokenProvider provider = new JwtTokenProvider(secret);
String token = provider.generateAccessToken(userId, email, roles);
JwtClaims claims = provider.validateAndParse(token);
```

### 3. S2S Authentication
- **Pattern**: Service-to-service JWT exchange
- **Use case**: Push-service → Auth-server token validation
- **Header**: `Authorization: Bearer <S2S_JWT>`

```java
ServiceAuthenticator auth = new ServiceAuthenticator(s2sSecret);
boolean valid = auth.verifyServiceToken(token);
```

### 4. Encryption
- **Symmetric**: AES-256 GCM (for sensitive PII)
- **Use case**: Store phone number, SSN (if needed)
- **Key**: Derived from environment secret

```java
AesEncryptor enc = new AesEncryptor(masterKey);
String encrypted = enc.encrypt("5551234567");
String decrypted = enc.decrypt(encrypted);
```

## Configuration

**environment.env** or **secrets manager**:
```
JWT_SECRET=very-long-random-key-min-256-bits
JWT_EXPIRY_MINUTES=15
JWT_REFRESH_EXPIRY_DAYS=7
S2S_SECRET=another-random-key
ENCRYPTION_MASTER_KEY=master-key-for-aes
```

**application-security.yml**:
```yaml
security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry-minutes: 15
    refresh-token-expiry-days: 7
  password:
    bcrypt-strength: 12
```

## Build & Test

```bash
./gradlew :common:security:clean build
```

## Testing

- Unit tests for encryption/decryption
- Test BCrypt hash verification
- Test JWT validation (expiry, signature)
- Use OWASP recommended test vectors

## Usage in Other Modules

```java
// In auth-server domain service
@Component
public class PasswordEncryptionService {
  private final PasswordEncryptor encryptor;
  
  public String hashPassword(String plaintext) {
    return encryptor.hash(plaintext);
  }
  
  public boolean verifyPassword(String plaintext, String hash) {
    return encryptor.matches(plaintext, hash);
  }
}

// In auth-server application service
public class TokenApplicationService {
  private final JwtTokenProvider jwtProvider;
  
  public TokenResponse generateToken(User user) {
    String accessToken = jwtProvider.generateAccessToken(
      user.getId().toString(),
      user.getEmail(),
      user.getRoles()
    );
    return new TokenResponse(accessToken, "Bearer");
  }
}
```

## References

**Used by**: auth-server, chat, push-service  
**Parent**: `../../CLAUDE.md`  
**Depends on**: common:core

---
**Last Updated**: 2026-04-08 | **Scope**: shared security library
