package com.fitally.backend.entity;

import com.fitally.backend.common.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "adult_verification_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdultVerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_request_id")
    private Long verificationRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "merchant_uid", nullable = false, unique = true, length = 100)
    private String merchantUid;

    @Column(name = "imp_uid", length = 100)
    private String impUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Builder
    public AdultVerificationRequest(User user,
                                    String merchantUid,
                                    String impUid,
                                    VerificationStatus status,
                                    LocalDateTime requestedAt,
                                    LocalDateTime verifiedAt,
                                    String failureReason) {
        this.user = user;
        this.merchantUid = merchantUid;
        this.impUid = impUid;
        this.status = status;
        this.requestedAt = requestedAt;
        this.verifiedAt = verifiedAt;
        this.failureReason = failureReason;
    }

    public static AdultVerificationRequest create(User user, String merchantUid) {
        return AdultVerificationRequest.builder()
                .user(user)
                .merchantUid(merchantUid)
                .status(VerificationStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public void markVerified(String impUid) {
        this.impUid = impUid;
        this.status = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void markFailed(String impUid, String failureReason) {
        this.impUid = impUid;
        this.status = VerificationStatus.FAILED;
        this.failureReason = failureReason;
    }
}