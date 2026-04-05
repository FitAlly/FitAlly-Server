package com.fitally.backend.external.portone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneTokenResponse {

    private int code;
    private String message;
    private TokenData response;

    @Getter
    @Setter
    public static class TokenData {

        @JsonProperty("access_token")
        private String accessToken;

        private long now;

        @JsonProperty("expired_at")
        private long expiredAt;

    }
}