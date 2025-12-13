package com.example.chat.domain.user;

import java.util.Optional;

/**
 * 사용자 Repository 인터페이스 (포트)
 */
public interface UserRepository {

	/**
	 * 사용자 저장
	 */
	User save(User user);

	/**
	 * ID로 사용자 조회
	 */
	Optional<User> findById(UserId id);

	/**
	 * 사용자 존재 여부 확인
	 */
	boolean existsById(UserId id);

	/**
	 * Username으로 사용자 조회
	 */
	Optional<User> findByUsername(String username);

	/**
	 * Email로 사용자 조회
	 */
	Optional<User> findByEmail(String email);
}
