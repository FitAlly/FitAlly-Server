package com.fitally.backend.dto.auth.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignupResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String provider;
    private final LocalDate birthdate;

    public SignupResponse(Long userId, String email, String nickname, String provider, LocalDate birthdate) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.birthdate = birthdate;
    }
}
