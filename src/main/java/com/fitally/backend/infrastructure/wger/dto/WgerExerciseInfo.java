package com.fitally.backend.infrastructure.wger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WgerExerciseInfo {
    private Long id;
    private WgerCategory category;

    private List<WgerMuscle> muscles;

    @JsonProperty("muscles_secondary")
    private List<WgerMuscle> musclesSecondary;

    private List<WgerEquipment> equipment;
    private List<WgerImage> images;
    private List<WgerTranslation> translations;

    /** 변형 동작 Wger ID (없으면 null) */
    private Long variations;
}
