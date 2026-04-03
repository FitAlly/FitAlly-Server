package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routine_workouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routineWorkoutId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private int orderIndex;
    private int targetSets;
    private int targetReps;
    private double weightKg;
    private int restSeconds;

    @Builder
    public RoutineWorkout(Routine routine, Exercise exercise, int orderIndex,
                          int targetSets, int targetReps, double weightKg, int restSeconds) {
        this.routine = routine;
        this.exercise = exercise;
        this.orderIndex = orderIndex;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.weightKg = weightKg;
        this.restSeconds = restSeconds;
    }
}
