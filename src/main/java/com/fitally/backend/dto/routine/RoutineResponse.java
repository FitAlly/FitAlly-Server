package com.fitally.backend.dto.routine;

import com.fitally.backend.entity.Routine;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoutineResponse {
    private final Long routineId;
    private final String name;
    private final boolean aiGenerated;
    private final int workoutCount;
    private final List<RoutineWorkoutResponse> workouts;

    public RoutineResponse(Routine routine) {
        this.routineId = routine.getRoutineId();
        this.name = routine.getName();
        this.aiGenerated = routine.isAiGenerated();
        this.workoutCount = routine.getRoutineWorkouts().size();
        this.workouts = routine.getRoutineWorkouts().stream()
                .map(RoutineWorkoutResponse::new)
                .collect(Collectors.toList());
    }
}
