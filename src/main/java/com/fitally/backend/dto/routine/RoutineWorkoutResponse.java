package com.fitally.backend.dto.routine;

import com.fitally.backend.entity.RoutineWorkout;
import lombok.Getter;

@Getter
public class RoutineWorkoutResponse {
    private final Long exerciseId;
    private final String exerciseName;
    private final String emoji;
    private final int orderIndex;
    private final int targetSets;
    private final int targetReps;
    private final double weightKg;
    private final int restSeconds;

    public RoutineWorkoutResponse(RoutineWorkout rw) {
        this.exerciseId = rw.getExercise().getExerciseId();
        this.exerciseName = rw.getExercise().getName();
        this.emoji = rw.getExercise().getEmoji();
        this.orderIndex = rw.getOrderIndex();
        this.targetSets = rw.getTargetSets();
        this.targetReps = rw.getTargetReps();
        this.weightKg = rw.getWeightKg();
        this.restSeconds = rw.getRestSeconds();
    }
}
