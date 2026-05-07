package com.fitally.backend.infrastructure.wger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WgerImage {
    private Long id;
    private String image;

    @JsonProperty("is_main")
    private boolean isMain;

    @JsonProperty("exercise_base")
    private Long exerciseBase;
}
