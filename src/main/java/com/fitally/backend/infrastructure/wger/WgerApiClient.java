package com.fitally.backend.infrastructure.wger;

import com.fitally.backend.infrastructure.wger.dto.WgerExerciseInfo;
import com.fitally.backend.infrastructure.wger.dto.WgerPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class WgerApiClient {

    private static final String BASE_URL = "https://wger.de/api/v2/exerciseinfo/";
    private static final int ENGLISH_LANGUAGE_ID = 2;

    private final RestTemplate restTemplate;

    public WgerApiClient(@Qualifier("wgerRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Wger API에서 운동 목록을 비동기로 조회
     *
     * @param limit  한 번에 가져올 개수
     * @param offset 오프셋 (페이지네이션)
     */
    @Async("wgerTaskExecutor")
    public CompletableFuture<List<WgerExerciseInfo>> fetchExerciseInfoAsync(int limit, int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("language", ENGLISH_LANGUAGE_ID)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        log.info("Wger API 호출: {}", url);

        try {
            ResponseEntity<WgerPageResponse<WgerExerciseInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<WgerPageResponse<WgerExerciseInfo>>() {}
            );

            if (response.getBody() == null || response.getBody().getResults() == null) {
                log.warn("Wger API 응답이 비어있습니다.");
                return CompletableFuture.completedFuture(Collections.emptyList());
            }

            List<WgerExerciseInfo> results = response.getBody().getResults();
            log.info("Wger API 응답 수신: {}개", results.size());
            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            log.error("Wger API 호출 실패 - url: {}", url, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("wgerTaskExecutor")
    public CompletableFuture<List<WgerExerciseInfo>> fetchAllExercisesAsync() {
        List<WgerExerciseInfo> all = new ArrayList<>();
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("language", ENGLISH_LANGUAGE_ID)
                .queryParam("limit", 100)
                .toUriString();

        while (url != null) {
            log.info("Wger API 호출: {}", url);
            try {
                ResponseEntity<WgerPageResponse<WgerExerciseInfo>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<WgerPageResponse<WgerExerciseInfo>>() {}
                );

                if (response.getBody() == null || response.getBody().getResults() == null) break;

                all.addAll(response.getBody().getResults());
                url = response.getBody().getNext();
                log.info("누적 {}개 수신, 다음 페이지: {}", all.size(), url != null ? "있음" : "없음");

            } catch (Exception e) {
                log.error("Wger API 호출 실패 - url: {}", url, e);
                return CompletableFuture.failedFuture(e);
            }
        }

        log.info("Wger API 전체 수신 완료: {}개", all.size());
        return CompletableFuture.completedFuture(all);
    }
}
