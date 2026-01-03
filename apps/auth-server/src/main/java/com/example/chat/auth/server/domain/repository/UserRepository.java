package com.example.chat.auth.server.domain.repository;

import com.example.chat.auth.server.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

    boolean existsById(@NonNull Long id);

}
