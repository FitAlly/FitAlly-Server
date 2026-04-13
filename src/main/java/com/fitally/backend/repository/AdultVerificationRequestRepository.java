package com.fitally.backend.repository;

import com.fitally.backend.entity.AdultVerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdultVerificationRequestRepository extends JpaRepository<AdultVerificationRequest, Long> {

    Optional<AdultVerificationRequest> findByMerchantUid(String merchantUid);
}
