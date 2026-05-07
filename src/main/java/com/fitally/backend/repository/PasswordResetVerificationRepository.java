package com.fitally.backend.repository;

import com.fitally.backend.entity.PasswordResetVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetVerificationRepository extends JpaRepository<PasswordResetVerification, Long> {

    List<PasswordResetVerification> findAllByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    Optional<PasswordResetVerification> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    Optional<PasswordResetVerification> findByEmailAndVerifyTokenAndVerifiedTrueAndUsedFalse(String email, String verifyToken);
}
