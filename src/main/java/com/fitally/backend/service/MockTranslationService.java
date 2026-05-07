package com.fitally.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mock 번역 구현체 - 사전(dictionary) 기반 키워드 치환 방식
 *
 * 실제 번역 API (파파고/구글) 연동 시:
 * 1. 새 구현체 작성 (예: PapagoTranslationService implements TranslationService)
 * 2. 해당 클래스에 @Primary 추가
 * 3. 이 클래스의 @Primary 제거
 */
@Slf4j
@Service
public class MockTranslationService implements TranslationService {

    private static final Map<String, String> DICTIONARY = Map.ofEntries(
            // 운동 카테고리
            Map.entry("Abs", "복근"),
            Map.entry("Arms", "팔"),
            Map.entry("Back", "등"),
            Map.entry("Calves", "종아리"),
            Map.entry("Chest", "가슴"),
            Map.entry("Legs", "하체"),
            Map.entry("Shoulders", "어깨"),
            Map.entry("Glutes", "둔근"),
            Map.entry("Core", "코어"),
            Map.entry("Full Body", "전신"),
            // 기구
            Map.entry("Barbell", "바벨"),
            Map.entry("Dumbbell", "덤벨"),
            Map.entry("Kettlebell", "케틀벨"),
            Map.entry("Cable", "케이블"),
            Map.entry("Machine", "머신"),
            Map.entry("Body weight", "맨몸"),
            Map.entry("Resistance Band", "저항 밴드"),
            Map.entry("Bench", "벤치"),
            Map.entry("Pull-up bar", "풀업 바"),
            // 운동 이름
            Map.entry("Push-up", "푸시업"),
            Map.entry("Pushup", "푸시업"),
            Map.entry("Pull-up", "풀업"),
            Map.entry("Squat", "스쿼트"),
            Map.entry("Deadlift", "데드리프트"),
            Map.entry("Lunge", "런지"),
            Map.entry("Plank", "플랭크"),
            Map.entry("Crunch", "크런치"),
            Map.entry("Press", "프레스"),
            Map.entry("Row", "로우"),
            Map.entry("Curl", "컬"),
            Map.entry("Extension", "익스텐션"),
            Map.entry("Raise", "레이즈"),
            Map.entry("Fly", "플라이"),
            Map.entry("Dip", "딥스")
    );

    @Override
    public String translate(String text) {
        if (text == null || text.isBlank()) return text;

        String result = text;
        for (Map.Entry<String, String> entry : DICTIONARY.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<String> translateAll(List<String> texts) {
        if (texts == null) return List.of();
        return texts.stream().map(this::translate).collect(Collectors.toList());
    }
}
