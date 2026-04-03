package com.fitally.backend.dto.workout;

import com.fitally.backend.entity.WorkoutSession;
import lombok.Getter;

@Getter
public class WorkoutFinishResponse {
    private final Long sessionId;
    private final String exerciseName;
    private final int completedSets;
    private final int targetReps;
    private final int totalReps;
    private final double weightKg;
    private final int totalSeconds;
    private final int calories;

    public WorkoutFinishResponse(WorkoutSession session) {
        this.sessionId = session.getSessionId();
        this.exerciseName = session.getExercise().getName();
        this.completedSets = session.getCompletedSets();
        this.targetReps = session.getTargetReps();
        this.totalReps = session.getCompletedSets() * session.getTargetReps();
        this.weightKg = session.getWeightKg();
        this.totalSeconds = session.getTotalSeconds();
        this.calories = session.getCalories();
    }
}
