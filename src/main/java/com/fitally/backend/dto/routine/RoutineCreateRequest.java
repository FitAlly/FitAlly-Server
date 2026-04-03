package com.fitally.backend.dto.routine;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RoutineCreateRequest {
    private String name;
    private List<RoutineWorkoutRequest> workouts;
}
