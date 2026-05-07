package com.fitally.backend.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VerifyPasswordResetCodeResponse {

    private final String message;
    private final String verifyToken;
}
