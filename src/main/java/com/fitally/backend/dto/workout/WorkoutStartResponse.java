package com.fitally.backend.dto.workout;

import com.fitally.backend.entity.WorkoutSession;
import lombok.Getter;

@Getter
public class WorkoutStartResponse {
    private final Long sessionId;
    private final String exerciseName;
    private final int targetSets;
    private final int targetReps;
    private final double weightKg;
    private final int restSeconds;

    public WorkoutStartResponse(WorkoutSession session) {
        this.sessionId = session.getSessionId();
        this.exerciseName = session.getExercise().getName();
        this.targetSets = session.getTargetSets();
        this.targetReps = session.getTargetReps();
        this.weightKg = session.getWeightKg();
        this.restSeconds = 90;
    }
}
