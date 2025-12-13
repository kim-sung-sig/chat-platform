package com.example.chat.storage.repository;

import com.example.chat.storage.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 JPA Repository
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, String> {

	/**
	 * Username으로 사용자 조회
	 */
	Optional<UserEntity> findByUsername(String username);

	/**
	 * Email로 사용자 조회
	 */
	Optional<UserEntity> findByEmail(String email);

	/**
	 * Username 존재 여부 확인
	 */
	boolean existsByUsername(String username);

	/**
	 * Email 존재 여부 확인
	 */
	boolean existsByEmail(String email);
}
