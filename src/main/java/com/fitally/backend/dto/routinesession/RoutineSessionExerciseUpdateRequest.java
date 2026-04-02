package com.fitally.backend.dto.routinesession;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutineSessionExerciseUpdateRequest {
    private int targetSets;
    private int targetReps;
    private int restSeconds;
}
