package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routine_session_exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineSessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routineSessionExerciseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_session_id", nullable = false)
    private RoutineSession routineSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private int orderIndex;
    private int targetSets;
    private int targetReps;
    private double weightKg;
    private int restSeconds;
    private int completedSets;

    @Builder
    public RoutineSessionExercise(RoutineSession routineSession, Exercise exercise,
                                  int orderIndex, int targetSets, int targetReps,
                                  double weightKg, int restSeconds) {
        this.routineSession = routineSession;
        this.exercise = exercise;
        this.orderIndex = orderIndex;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.weightKg = weightKg;
        this.restSeconds = restSeconds;
        this.completedSets = 0;
    }

    public void completeSet() {
        this.completedSets++;
    }

    public boolean isAllSetsCompleted() {
        return this.completedSets >= this.targetSets;
    }

    public void update(int targetSets, int targetReps, int restSeconds) {
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.restSeconds = restSeconds;
    }
}
