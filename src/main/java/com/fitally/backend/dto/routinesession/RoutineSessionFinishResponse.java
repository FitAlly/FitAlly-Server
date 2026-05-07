package com.fitally.backend.dto.routinesession;

import com.fitally.backend.entity.RoutineSession;
import lombok.Getter;

@Getter
public class RoutineSessionFinishResponse {
    private final Long routineSessionId;
    private final String routineName;
    private final int totalSeconds;
    private final int completedExercises;
    private final int totalSets;
    private final int calories;

    public RoutineSessionFinishResponse(RoutineSession session) {
        this.routineSessionId = session.getRoutineSessionId();
        this.routineName = session.getRoutineName();
        this.totalSeconds = session.getTotalSeconds();
        this.completedExercises = (int) session.getExercises().stream()
                .filter(e -> e.getCompletedSets() > 0)
                .count();
        this.totalSets = session.getExercises().stream()
                .mapToInt(e -> e.getCompletedSets())
                .sum();
        this.calories = session.getCalories();
    }
}
