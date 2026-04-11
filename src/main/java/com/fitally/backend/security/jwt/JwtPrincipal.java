package com.fitally.backend.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JwtPrincipal {

    private final Long userId;
    private final String email;
    private final String role;
}
