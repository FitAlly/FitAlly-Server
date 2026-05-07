package com.fitally.backend.dto.exercise;

import com.fitally.backend.entity.Exercise;
import lombok.Getter;

@Getter
public class ExercisePreviewResponse {
    private final Long exerciseId;
    private final String name;
    private final String category;
    private final String emoji;

    public ExercisePreviewResponse(Exercise exercise) {
        this.exerciseId = exercise.getExerciseId();
        this.name = exercise.getName();
        this.category = exercise.getCategory();
        this.emoji = exercise.getEmoji();
    }
}
