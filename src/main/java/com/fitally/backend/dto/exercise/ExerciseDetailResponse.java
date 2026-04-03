package com.fitally.backend.dto.exercise;

import com.fitally.backend.entity.Exercise;
import lombok.Getter;

import java.util.List;

@Getter
public class ExerciseDetailResponse {
    private final Long exerciseId;
    private final String name;
    private final String category;
    private final String difficulty;
    private final String emoji;
    private final String description;
    private final List<String> muscles;
    private final List<String> musclesSecondary;
    private final List<String> equipment;
    private final List<String> images;
    private final List<String> aliases;
    private final List<String> notes;

    public ExerciseDetailResponse(Exercise exercise) {
        this.exerciseId = exercise.getExerciseId();
        this.name = exercise.getName();
        this.category = exercise.getCategory();
        this.difficulty = exercise.getDifficulty();
        this.emoji = exercise.getEmoji();
        this.description = exercise.getDescription();
        this.muscles = exercise.getMuscles();
        this.musclesSecondary = exercise.getMusclesSecondary();
        this.equipment = exercise.getEquipment();
        this.images = exercise.getImages();
        this.aliases = exercise.getAliases();
        this.notes = exercise.getNotes();
    }
}
