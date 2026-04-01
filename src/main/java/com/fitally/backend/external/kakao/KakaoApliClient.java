package com.fitally.backend.external.kakao;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.auth.response.KakaoUserInfo;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class KakaoApliClient {

    private static final String KAKAO_USER_INFO_URL = "https:/kapi.kakao.com/v2/user/me";
    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response;

        try {
            response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String providerId = body.get("id") == null ? null : String.valueOf(body.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null ? null : (Map<String, Object>) kakaoAccount.get("profile");

        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
        String nickname = profile == null ? null : (String) profile.get("nickname");
        String profileImageUrl = profile == null ? null : (String) profile.get("profile_image_url");

        return new KakaoUserInfo(providerId, email, nickname, profileImageUrl);
    }
}
