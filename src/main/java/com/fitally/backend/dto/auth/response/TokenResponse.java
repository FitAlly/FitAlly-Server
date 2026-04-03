package com.fitally.backend.dto.auth.response;

import lombok.Getter;

@Getter
public class TokenResponse {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;

    public TokenResponse(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
