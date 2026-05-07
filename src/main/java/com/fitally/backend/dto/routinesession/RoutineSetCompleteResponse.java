package com.fitally.backend.dto.routinesession;

import com.fitally.backend.entity.RoutineSession;
import com.fitally.backend.entity.RoutineSessionExercise;
import lombok.Getter;

@Getter
public class RoutineSetCompleteResponse {
    private final int completedSets;
    private final int targetSets;
    private final boolean allSetsForCurrentExercise;
    private final int restSeconds;
    private final String nextExerciseName;
    private final boolean allCompleted;

    public RoutineSetCompleteResponse(RoutineSession session, RoutineSessionExercise completedExercise) {
        this.completedSets = completedExercise.getCompletedSets();
        this.targetSets = completedExercise.getTargetSets();
        this.allSetsForCurrentExercise = completedExercise.isAllSetsCompleted();
        this.allCompleted = session.isAllExercisesCompleted();

        if (allSetsForCurrentExercise && !allCompleted) {
            RoutineSessionExercise next = session.getCurrentExercise();
            this.nextExerciseName = next != null ? next.getExercise().getName() : null;
            this.restSeconds = completedExercise.getRestSeconds();
        } else {
            this.nextExerciseName = null;
            this.restSeconds = allCompleted ? 0 : completedExercise.getRestSeconds();
        }
    }
}
