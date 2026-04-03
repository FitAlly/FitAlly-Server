package com.fitally.backend.infrastructure.wger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WgerTranslation {
    private Long id;
    private String name;
    private String description;
    private int language;

    @JsonProperty("exercise_base")
    private Long exerciseBase;

    /** 운동 별칭 목록 */
    private List<WgerAlias> aliases;

    /** 주의사항 / 팁 목록 */
    private List<WgerNote> notes;
}
