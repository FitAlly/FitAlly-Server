package com.fitally.backend.dto.exercise;

import com.fitally.backend.entity.Exercise;
import lombok.Getter;

@Getter
public class ExerciseListResponse {
    private final Long exerciseId;
    private final String name;
    private final String category;
    private final String difficulty;
    private final String emoji;
    private final String imageUrl;
    private final int estimatedMinutes;

    public ExerciseListResponse(Exercise exercise) {
        this.exerciseId = exercise.getExerciseId();
        this.name = exercise.getName();
        this.category = exercise.getCategory();
        this.difficulty = exercise.getDifficulty();
        this.emoji = exercise.getEmoji();
        this.imageUrl = (exercise.getImages() != null && !exercise.getImages().isEmpty())
                ? exercise.getImages().get(0) : null;
        this.estimatedMinutes = estimateMinutes(exercise.getDifficulty());
    }

    private int estimateMinutes(String difficulty) {
        if (difficulty == null) return 20;
        return switch (difficulty) {
            case "초급" -> 15;
            case "고급" -> 30;
            default -> 20;
        };
    }
}
