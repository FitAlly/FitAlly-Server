package com.fitally.backend.service;

import com.fitally.backend.entity.Exercise;
import com.fitally.backend.infrastructure.wger.WgerApiClient;
import com.fitally.backend.infrastructure.wger.dto.WgerAlias;
import com.fitally.backend.infrastructure.wger.dto.WgerEquipment;
import com.fitally.backend.infrastructure.wger.dto.WgerExerciseInfo;
import com.fitally.backend.infrastructure.wger.dto.WgerImage;
import com.fitally.backend.infrastructure.wger.dto.WgerMuscle;
import com.fitally.backend.infrastructure.wger.dto.WgerNote;
import com.fitally.backend.infrastructure.wger.dto.WgerTranslation;
import com.fitally.backend.repository.ExerciseRepository;
import com.fitally.backend.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseInitializationService {

    private static final int ENGLISH_LANGUAGE_ID = 2;
    private static final int FETCH_LIMIT = 50;

    private final WgerApiClient wgerApiClient;
    private final ExerciseRepository exerciseRepository;
    private final TranslationService translationService;

    private static final Map<String, String> CATEGORY_EMOJI = Map.of(
            "Abs", "💪",
            "Arms", "🦾",
            "Back", "🔙",
            "Calves", "🦵",
            "Chest", "🏋️",
            "Legs", "🦿",
            "Shoulders", "⬆️"
    );

    @Transactional
    public void initializeExercises() {
        log.info("Wger API에서 전체 운동 데이터 fetch 시작...");

        try {
            List<WgerExerciseInfo> rawExercises = wgerApiClient
                    .fetchExerciseInfoAsync(FETCH_LIMIT, 0)
                    .get(10, TimeUnit.MINUTES);

            List<Exercise> exercises = rawExercises.stream()
                    .map(this::mapToExercise)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            exerciseRepository.saveAll(exercises);
            log.info("운동 데이터 {}개 DB 저장 완료", exercises.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("운동 데이터 초기화 중 인터럽트 발생", e);
        } catch (Exception e) {
            log.error("운동 데이터 초기화 실패 (Wger API 타임아웃 또는 네트워크 오류)", e);
        }
    }

    private Exercise mapToExercise(WgerExerciseInfo info) {
        WgerTranslation translation = info.getTranslations().stream()
                .filter(t -> t.getLanguage() == ENGLISH_LANGUAGE_ID)
                .findFirst()
                .orElse(null);

        if (translation == null || translation.getName() == null || translation.getName().isBlank()) {
            log.debug("번역 데이터 없음 - exerciseId: {}", info.getId());
            return null;
        }

        String categoryName = info.getCategory() != null ? info.getCategory().getName() : "Unknown";
        String cleanDescription = stripHtml(translation.getDescription());

        List<String> rawMuscles = extractMuscleNames(info.getMuscles());
        List<String> rawMusclesSecondary = extractMuscleNames(info.getMusclesSecondary());
        List<String> rawEquipment = extractEquipmentNames(info.getEquipment());
        List<String> rawAliases = translation.getAliases() != null
                ? translation.getAliases().stream().map(WgerAlias::getAlias).filter(Objects::nonNull).collect(Collectors.toList())
                : List.of();

        String translatedName = translationService.translate(translation.getName(), "NAME");
        String translatedCategory = translationService.translate(categoryName, "EQUIPMENT");
        String translatedDescription = translationService.translate(cleanDescription, "DESCRIPTION");
        List<String> translatedMuscles = translationService.translateAll(rawMuscles, "EQUIPMENT");
        List<String> translatedMusclesSecondary = translationService.translateAll(rawMusclesSecondary, "EQUIPMENT");
        List<String> translatedEquipment = translationService.translateAll(rawEquipment, "EQUIPMENT");
        List<String> translatedAliases = translationService.translateAll(rawAliases, "ALIAS");

        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        List<String> imageUrls = extractImageUrls(info.getImages());
        List<String> notes = translation.getNotes() != null
                ? translation.getNotes().stream().map(WgerNote::getNote).filter(Objects::nonNull).collect(Collectors.toList())
                : List.of();

        return Exercise.builder()
                .wgerId(info.getId())
                .name(translatedName)
                .category(translatedCategory)
                .difficulty("중급")
                .emoji(CATEGORY_EMOJI.getOrDefault(categoryName, "🏃"))
                .description(translatedDescription)
                .muscles(translatedMuscles)
                .musclesSecondary(translatedMusclesSecondary)
                .equipment(translatedEquipment)
                .images(imageUrls)
                .aliases(translatedAliases)
                .notes(notes)
                .variations(info.getVariations())
                .build();
    }

    private List<String> extractMuscleNames(List<WgerMuscle> muscles) {
        if (muscles == null || muscles.isEmpty()) return List.of();
        return muscles.stream()
                .map(m -> m.getNameEn() != null ? m.getNameEn() : m.getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> extractEquipmentNames(List<WgerEquipment> equipment) {
        if (equipment == null || equipment.isEmpty()) return List.of();
        return equipment.stream()
                .map(WgerEquipment::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> extractImageUrls(List<WgerImage> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .map(WgerImage::getImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * HTML 태그 제거 (Wger description 필드에 HTML이 포함되어 있음)
     */
    private String stripHtml(String html) {
        if (html == null || html.isBlank()) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
