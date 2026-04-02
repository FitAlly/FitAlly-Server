package com.fitally.backend.service;

import java.util.List;

/**
 * 번역 서비스 인터페이스
 *
 * 현재 구현체: MockTranslationService (키워드 사전 기반 Mock 번역)
 * 실제 번역 API 연동 시 이 인터페이스를 구현한 새 클래스를 만들고
 * MockTranslationService의 @Primary 어노테이션을 제거하면 됩니다.
 *
 * 지원 예정 구현체:
 * - PapagoTranslationService (네이버 파파고)
 * - GoogleTranslationService (구글 번역)
 */
public interface TranslationService {

    String translate(String text);

    List<String> translateAll(List<String> texts);

    default String translate(String text, String type) {
        return translate(text);
    }

    default List<String> translateAll(List<String> texts, String type) {
        return translateAll(texts);
    }
}
