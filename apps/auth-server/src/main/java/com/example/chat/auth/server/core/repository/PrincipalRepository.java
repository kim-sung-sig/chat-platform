package com.example.chat.auth.server.core.repository;

import com.example.chat.auth.server.core.domain.Principal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Principal JPA Repository
 */
@Repository
public interface PrincipalRepository extends JpaRepository<Principal, UUID> {
    /** 식별자(이메일, 사용자명)로 주체 조회 */
    Optional<Principal> findByIdentifier(String identifier);

    /** 활성 주체인지 확인 */
    boolean existsByIdAndActiveTrue(UUID id);
}
