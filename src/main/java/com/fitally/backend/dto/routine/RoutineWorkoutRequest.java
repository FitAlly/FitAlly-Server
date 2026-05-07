package com.fitally.backend.dto.routine;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutineWorkoutRequest {
    private Long exerciseId;
    private int targetSets;
    private int targetReps;
    private double weightKg;
    private int restSeconds;
}
