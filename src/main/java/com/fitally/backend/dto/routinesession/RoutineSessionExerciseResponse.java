package com.fitally.backend.dto.routinesession;

import com.fitally.backend.entity.RoutineSessionExercise;
import lombok.Getter;

@Getter
public class RoutineSessionExerciseResponse {
    private final Long routineSessionExerciseId;
    private final String exerciseName;
    private final String emoji;
    private final int orderIndex;
    private final int targetSets;
    private final int targetReps;
    private final double weightKg;
    private final int restSeconds;
    private final int completedSets;

    public RoutineSessionExerciseResponse(RoutineSessionExercise e) {
        this.routineSessionExerciseId = e.getRoutineSessionExerciseId();
        this.exerciseName = e.getExercise().getName();
        this.emoji = e.getExercise().getEmoji();
        this.orderIndex = e.getOrderIndex();
        this.targetSets = e.getTargetSets();
        this.targetReps = e.getTargetReps();
        this.weightKg = e.getWeightKg();
        this.restSeconds = e.getRestSeconds();
        this.completedSets = e.getCompletedSets();
    }
}
