package com.fitally.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_verifications")
public class PasswordResetVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_reset_verification_id")
    private Long passwordResetVerificationId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "verify_token", length = 255)
    private String verifyToken;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PasswordResetVerification() {
    }

    public PasswordResetVerification(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.used = false;
        this.verified = false;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getPasswordResetVerificationId() {
        return passwordResetVerificationId;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void verify(String verifyToken) {
        this.verified = true;
        this.verifyToken = verifyToken;
        this.verifiedAt = LocalDateTime.now();
    }

    public void markUsed() {
        this.used = true;
    }
}
