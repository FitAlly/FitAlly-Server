package com.fitally.backend.external.portone;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.external.portone.dto.PortOneCertificationResponse;
import com.fitally.backend.external.portone.dto.PortOneTokenRequest;
import com.fitally.backend.external.portone.dto.PortOneTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PortOneClient {

    private final RestClient restClient;
    private final PortOneProperties portOneProperties;

    public PortOneClient(PortOneProperties portOneProperties) {
        this.portOneProperties = portOneProperties;
        this.restClient = RestClient.builder()
                .baseUrl(portOneProperties.getBaseUrl())
                .build();
    }

    public PortOneCertificationResponse.CertificationData getCertification(String impUid) {
        String accessToken = getAccessToken();

        PortOneCertificationResponse response = restClient.get()
                .uri("/certifications/{impUid}", impUid)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .body(PortOneCertificationResponse.class);

        if (response == null || response.getResponse() == null) {
            throw new BusinessException(ErrorCode.ADULT_VERIFICATION_FAILED);
        }

        return response.getResponse();
    }

    private String getAccessToken() {
        PortOneTokenResponse response = restClient.post()
                .uri("/users/getToken")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PortOneTokenRequest(
                        portOneProperties.getApiKey(),
                        portOneProperties.getApiSecret()
                ))
                .retrieve()
                .body(PortOneTokenResponse.class);

        if (response == null
                || response.getResponse() == null
                || response.getResponse().getAccessToken() == null) {
            throw new BusinessException(ErrorCode.ADULT_VERIFICATION_FAILED);
        }

        return response.getResponse().getAccessToken();
    }
}