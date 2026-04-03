package com.fitally.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Service
public class GptTranslationService implements TranslationService {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    // 🚀 1. 배치 요청용 고정 프롬프트 (데이터 안 밀리게 ||| 사용)
    private static final String BATCH_USER_PROMPT = """
            아래 제공된 텍스트들을 지침에 맞게 한국어로 번역하세요.
            입력 데이터는 "|||" 기호로 구분되어 있습니다.
            번역 결과도 반드시 동일하게 "|||" 기호로만 구분해서 출력하세요. (설명 안에 줄바꿈이 있더라도 절대 임의로 분리하지 마세요!)

            %s
            """;

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GptTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 🚀 데이터 타입별로 AI의 자아(최면)를 완벽하게 통제하는 로직
    private String getSystemPrompt(String type) {
        // 1. 이름(name) & 별칭(aliases): 뜻 번역 절대 금지! 오직 음역(소리 나는 대로 표기)!
        if ("NAME".equals(type) || "ALIAS".equals(type)) {
            return """
                [🚨 가장 강력한 절대 규칙: 뜻 번역 금지, 오직 음역(Transliteration)만 할 것 🚨]
                당신은 영어 단어의 뜻을 풀이하는 번역가가 아닙니다! 영어를 한국어 발음으로 소리 나는 대로 적어주는 '한글 표기 변환기'입니다.
                입력된 단어의 뜻을 절대 한국어로 번역하지 마세요. (예: 걷기, 잡기, 누르기, 벌레 등의 단어 절대 사용 금지)
                오직 '영어 발음 그대로' 외래어 표기법에 맞춰 한글로 적으세요.
                
                [올바른 변환 예시 - 반드시 참고할 것]
                - Bear Walk → 베어 워크 (O) / 곰 걷기 (X)
                - Chair Step → 체어 스텝 (O) / 의자 스텝 (X)
                - Woodchopper → 우드찹 (O) / 도끼 잡기, 나무꾼 (X)
                - Deadbug → 데드버그 (O) / 죽은 벌레 (X)
                - Lat Pulldown → 랫풀다운 (O) / 광배근 당기기 (X)
                
                무조건 입력된 영어의 발음 표기로만 응답하세요. 다른 부가 설명, 마침표, 따옴표는 절대 금지입니다.
                """;
        }
        // 2. 설명(description): PT 쌤 빙의해서 찰떡같이 친절하게!
        else if ("DESCRIPTION".equals(type)) {
            return """
                당신은 10년 차 친절한 한국 헬스장 PT 트레이너입니다.
                사용자가 이 운동 설명을 듣고 바로 헬스장에서 따라 할 수 있도록, 아주 이해하기 쉽고 자연스러운 존댓말(~해요, ~습니다)로 설명해 주세요.
                '의자를 향해 서기. 동작: 의자에 올라가기.' 같은 기계적인 번역투를 완전히 버리세요!
                "벤치(또는 의자) 앞에 서주세요. 한 발을 벤치 위로 올리고~" 처럼 초보자에게 설명하듯 부드럽게 의역하세요.
                번역된 텍스트만 출력하고 부가 설명은 빼주세요.
                """;
        }
        else if ("DIFFICULTY".equals(type)) {
            return """
                당신은 피트니스 전문가입니다. 운동 이름을 보고 아래 기준에 따라 난이도를 판별하세요.

                [초급] 궤적이 고정되거나 동작이 단순해 초보자도 쉽게 따라할 수 있는 운동
                - 머신 운동: Leg Press, Chest Press Machine, Leg Extension, Lat Pulldown Machine
                - 케이블 운동: Cable Curl, Cable Pushdown, Cable Fly
                - 단일 관절 프리웨이트: Dumbbell Curl, Lateral Raise, Dumbbell Fly
                - 쉬운 맨몸: Crunch, Glute Bridge, Knee Push-up, Lunge

                [중급] 코어 안정화와 다관절 협응이 필요한 운동
                - 다관절 프리웨이트: Barbell Squat, Deadlift, Bench Press, Overhead Press, Barbell Row
                - 덤벨 복합 운동: Dumbbell Squat, Romanian Deadlift, Dumbbell Press
                - 표준 맨몸: Pull-up, Push-up, Dip, Plank

                [고급] 폭발적 순발력, 극도의 밸런스, 고난도 기술이 필요한 운동
                - 올림픽 리프팅: Power Clean, Hang Clean, Snatch, Clean and Jerk
                - 고난도 맨몸: Muscle-up, Front Lever, Planche, Pistol Squat, Handstand Push-up
                - 고난도 밸런스: Single-leg Deadlift (unilateral free weight)

                🚨 규칙: 반드시 "초급", "중급", "고급" 중 하나의 단어만 출력하세요. 설명, 마침표, 따옴표 모두 금지합니다.
                """;
        }
        // 3. 기구(equipment) & 기타: 해부학 및 헬스장 통용 명칭!
        else {
            return """
                당신은 피트니스 전문 번역가입니다.
                [🚨 절대 규칙]
                1. 근육 명칭은 한국 해부학적 명칭(대흉근, 광배근, 대퇴사두근 등)을 사용하세요.
                2. 기구 명칭은 한국 헬스장에서 쓰는 외래어 명칭(바벨, 덤벨, 체어, 벤치, 케이블 등)을 사용하세요.
                3. 부위 카테고리는 자연스러운 한국어로 번역하세요. (Chest → 가슴, Legs → 하체)
                번역 결과 외에 다른 말은 절대 출력하지 마세요.
                """;

        }
    }

    // 기존 인터페이스 호환을 위한 기본 메서드 (기본은 EQUIPMENT 룰 사용)
    @Override
    public String translate(String text) {
        return translate(text, "DEFAULT");
    }

    @Override
    public List<String> translateAll(List<String> texts) {
        return translateAll(texts, "DEFAULT");
    }

    // 🚀 3. 타입(type)을 받아서 맞춤형 번역을 수행하는 새로운 메서드
    public String translate(String text, String type) {
        if (text == null || text.isBlank()) return text;
        List<String> result = translateAll(List.of(text), type);
        return result.isEmpty() ? text : result.get(0);
    }

    public List<String> translateAll(List<String> texts, String type) {
        if (texts == null || texts.isEmpty()) return List.of();

        List<Integer> nonBlankIndices = new ArrayList<>();
        List<String> nonBlankTexts = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i) != null && !texts.get(i).isBlank()) {
                nonBlankIndices.add(i);
                nonBlankTexts.add(texts.get(i));
            }
        }

        if (nonBlankTexts.isEmpty()) return texts;

        // ⭐ 데이터 밀림 방지: 엔터 대신 ||| 로 묶기
        String joinedTexts = String.join(" ||| ", nonBlankTexts);

        String userPrompt = nonBlankTexts.size() == 1
                ? nonBlankTexts.get(0)
                : String.format(BATCH_USER_PROMPT, joinedTexts);

        // ⭐ System Prompt를 type에 맞게 가져와서 전달
        String response = callGpt(userPrompt, getSystemPrompt(type));
        if (response == null) return texts;

        List<String> translated = new ArrayList<>(texts);

        if (nonBlankTexts.size() == 1) {
            translated.set(nonBlankIndices.get(0), response);
        } else {
            // ⭐ 응답 자를 때도 무조건 ||| 로만 자르기 (엔터 무시)
            String[] parsedParts = response.split("\\|\\|\\|");

            int parsedCount = 0;
            for (String part : parsedParts) {
                String content = part.trim();
                if (content.isEmpty()) continue;

                if (parsedCount < nonBlankIndices.size()) {
                    translated.set(nonBlankIndices.get(parsedCount), content);
                    parsedCount++;
                }
            }
        }

        return translated;
    }

    // callGpt 메서드도 systemPrompt를 받도록 수정
    private String callGpt(String userPrompt, String systemPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(GPT_API_URL, HttpMethod.POST, request, Map.class);

            if (response.getBody() == null) return null;

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return message.get("content").toString().trim();

        } catch (Exception e) {
            log.warn("GPT 번역 실패 - 원문 반환: {}", e.getMessage());
            return null;
        }
    }
}