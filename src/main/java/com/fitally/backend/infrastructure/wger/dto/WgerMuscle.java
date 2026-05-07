package com.fitally.backend.infrastructure.wger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WgerMuscle {
    private int id;
    private String name;

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("is_front")
    private boolean isFront;

    @JsonProperty("image_url_main")
    private String imageUrlMain;

    @JsonProperty("image_url_secondary")
    private String imageUrlSecondary;
}
