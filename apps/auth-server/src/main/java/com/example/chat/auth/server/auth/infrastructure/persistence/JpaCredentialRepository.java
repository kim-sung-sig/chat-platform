package com.example.chat.auth.server.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCredentialRepository extends JpaRepository<CredentialEntity, UUID> {
	Optional<CredentialEntity> findByPrincipalIdAndType(UUID principalId, String type);

	void deleteByPrincipalIdAndType(UUID principalId, String type);
}
