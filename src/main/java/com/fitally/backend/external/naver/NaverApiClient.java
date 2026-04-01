package com.fitally.backend.external.naver;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.response.SocialUserInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NaverApiClient {

    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";
    private final RestTemplate restTemplate = new RestTemplate();

    public SocialUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    NAVER_PROFILE_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("response") == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Map<String, Object> profile = (Map<String, Object>) body.get("response");

        String providerId = profile.get("id") == null ? null : String.valueOf(profile.get("id"));
        String email = profile.get("email") == null ? null : String.valueOf(profile.get("email"));
        String nickname = profile.get("nickname") == null ? null : String.valueOf(profile.get("nickname"));
        String profileImageUrl = profile.get("profile_image") == null ? null : String.valueOf(profile.get("profile_image"));

        return new SocialUserInfo(
                "naver",
                providerId,
                email,
                nickname,
                profileImageUrl
        );
    }
}
