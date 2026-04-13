package com.fitally.backend.entity;

import com.fitally.backend.common.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(name = "is_adult_verified")
    private Boolean isAdultVerified;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Builder
    public User(Long userId,
                String email,
                String passwordHash,
                String provider,
                String providerId,
                String nickname,
                LocalDate birthdate,
                String profileImageUrl,
                Boolean isAdultVerified,
                String bio,
                String location,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                LocalDateTime deletedAt,
                String fcmToken,
                Role role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.birthdate = birthdate;
        this.profileImageUrl = profileImageUrl;
        this.isAdultVerified = isAdultVerified;
        this.bio = bio;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.fcmToken = fcmToken;
        this.role = role;
    }

    public static User createEmailUser(String email,
                                       String encodedPassword,
                                       String nickname,
                                       LocalDate birthdate) {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .provider("email")
                .providerId(null)
                .nickname(nickname)
                .birthdate(birthdate)
                .profileImageUrl(null)
                .isAdultVerified(false)
                .bio(null)
                .location(null)
                .createdAt(now)
                .updatedAt(now)
                .fcmToken(null)
                .deletedAt(null)
                .role(Role.USER)
                .build();
    }

    public static User createSocialUser(String email,
                                        String provider,
                                        String providerId,
                                        String nickname,
                                        String profileImageUrl) {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .email(email)
                .passwordHash(null)
                .provider(provider)
                .providerId(providerId)
                .nickname(nickname)
                .birthdate(null)
                .profileImageUrl(profileImageUrl)
                .isAdultVerified(false)
                .bio(null)
                .location(null)
                .createdAt(now)
                .updatedAt(now)
                .fcmToken(null)
                .deletedAt(null)
                .role(Role.USER)
                .build();
    }

    public void verifyAdult(LocalDate birthdate) {
        this.isAdultVerified = true;
        this.birthdate = birthdate;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEmailUser() {
        return provider == null || "email".equalsIgnoreCase(provider);
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
