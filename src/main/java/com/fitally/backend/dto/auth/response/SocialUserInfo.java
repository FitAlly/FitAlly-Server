package com.fitally.backend.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SocialUserInfo {

    private final String provider;
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
}
