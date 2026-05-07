package com.fitally.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiTranslationService implements TranslationService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private static final String SINGLE_PROMPT = """
            당신은 피트니스 및 운동 전문 번역가입니다. 아래 영어 텍스트를 자연스러운 한국어로 번역하세요.

            규칙:
            1. 운동 이름은 한국에서 통용되는 명칭을 사용하세요. (예: Bench Press → 벤치 프레스, Squat → 스쿼트, Pull-up → 풀업)
            2. 근육 명칭은 해부학적 한국어 명칭을 사용하세요. (예: Pectoralis Major → 대흉근, Deltoid → 삼각근, Triceps → 삼두근, Biceps → 이두근, Latissimus Dorsi → 광배근)
            3. 기구 명칭은 한국에서 통용되는 명칭을 사용하세요. (예: Barbell → 바벨, Dumbbell → 덤벨, Cable → 케이블, Kettlebell → 케틀벨)
            4. 운동 부위 카테고리는 한국어로 번역하세요. (예: Chest → 가슴, Back → 등, Legs → 하체, Shoulders → 어깨, Arms → 팔, Abs → 복근, Calves → 종아리)
            5. 번역문만 출력하고 설명, 따옴표, 부가 내용은 절대 붙이지 마세요.

            번역할 텍스트:
            %s
            """;

    private static final String BATCH_PROMPT = """
            당신은 피트니스 및 운동 전문 번역가입니다. 아래 번호가 매겨진 영어 텍스트 목록을 한국어로 번역하세요.

            규칙:
            1. 운동 이름은 한국에서 통용되는 명칭을 사용하세요. (예: Bench Press → 벤치 프레스, Squat → 스쿼트, Pull-up → 풀업)
            2. 근육 명칭은 해부학적 한국어 명칭을 사용하세요. (예: Pectoralis Major → 대흉근, Deltoid → 삼각근, Triceps → 삼두근, Biceps → 이두근, Latissimus Dorsi → 광배근)
            3. 기구 명칭은 한국에서 통용되는 명칭을 사용하세요. (예: Barbell → 바벨, Dumbbell → 덤벨, Cable → 케이블, Kettlebell → 케틀벨)
            4. 운동 부위 카테고리는 한국어로 번역하세요. (예: Chest → 가슴, Back → 등, Legs → 하체, Shoulders → 어깨, Arms → 팔, Abs → 복근, Calves → 종아리)
            5. 반드시 "1. 번역결과\\n2. 번역결과" 형식으로 번호와 함께 출력하세요. 설명이나 부가 내용은 절대 붙이지 마세요.

            번역할 목록:
            %s
            """;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String translate(String text) {
        if (text == null || text.isBlank()) return text;
        return callGemini(String.format(SINGLE_PROMPT, text), text);
    }

    @Override
    public List<String> translateAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();

        // 빈 문자열은 그대로, 내용 있는 것만 번역
        List<Integer> nonBlankIndices = new ArrayList<>();
        List<String> nonBlankTexts = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i) != null && !texts.get(i).isBlank()) {
                nonBlankIndices.add(i);
                nonBlankTexts.add(texts.get(i));
            }
        }

        if (nonBlankTexts.isEmpty()) return texts;

        // 번호 목록 형식으로 한 번에 번역
        StringBuilder numbered = new StringBuilder();
        for (int i = 0; i < nonBlankTexts.size(); i++) {
            numbered.append(i + 1).append(". ").append(nonBlankTexts.get(i)).append("\n");
        }

        String prompt = String.format(BATCH_PROMPT, numbered.toString().trim());
        String response = callGemini(prompt, null);

        if (response == null) return texts;

        // 응답 파싱: "1. 번역\n2. 번역" → 리스트
        String[] lines = response.split("\n");
        List<String> translated = new ArrayList<>(texts); // 원본 복사
        int parsedCount = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // "1. 번역결과" 형식에서 번역 결과만 추출
            String content = line.replaceFirst("^\\d+\\.\\s*", "");
            if (parsedCount < nonBlankIndices.size()) {
                translated.set(nonBlankIndices.get(parsedCount), content);
                parsedCount++;
            }
        }

        return translated;
    }

    private String callGemini(String prompt, String fallback) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", prompt)
                                ))
                        )
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                        GEMINI_API_URL + apiKey,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

                if (response.getBody() == null) return fallback;

                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates == null || candidates.isEmpty()) return fallback;

                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts == null || parts.isEmpty()) return fallback;

                return parts.get(0).get("text").toString().trim();

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429 && attempt < MAX_RETRIES) {
                    log.warn("Gemini 429 Rate Limit - {}초 후 재시도 ({}/{})", RETRY_DELAY_MS / 1000, attempt, MAX_RETRIES);
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    log.warn("Gemini 번역 실패 ({}): {}", e.getStatusCode(), e.getMessage());
                    return fallback;
                }
            } catch (Exception e) {
                log.warn("Gemini 번역 실패 - 원문 반환", e);
                return fallback;
            }
        }
        return fallback;
    }
}
