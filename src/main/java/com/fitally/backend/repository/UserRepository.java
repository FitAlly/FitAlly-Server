package com.fitally.backend.repository;

import com.fitally.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<User> findByProviderAndProviderIdAndDeletedAtIsNull(String provider, String providerId);

    Optional<User> findByEmail(String email);
}
