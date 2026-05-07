package com.fitally.backend.security.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class OAuth2Attributes {

    private final String provider;
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;

    public static OAuth2Attributes of(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null
                ? null
                : (Map<String, Object>) kakaoAccount.get("profile");

        String providerId = attributes.get("id") == null ? null : String.valueOf(attributes.get("id"));
        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
        String nickname = profile == null ? null : (String) profile.get("nickname");
        String profileImageUrl = profile == null ? null : (String) profile.get("profile_image_url");

        return new OAuth2Attributes(
                "kakao",
                providerId,
                email,
                nickname,
                profileImageUrl
        );
    }
}
