package com.fitally.backend.dto.workout;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkoutStartRequest {
    private Long exerciseId;
    private int targetSets;
    private int targetReps;
    private double weightKg;
}
