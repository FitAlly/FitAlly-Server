package com.fitally.backend.dto.routinesession;

import com.fitally.backend.entity.RoutineSession;
import com.fitally.backend.entity.RoutineSessionExercise;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoutineSessionStateResponse {
    private final Long routineSessionId;
    private final String routineName;
    private final CurrentExerciseInfo currentExercise;
    private final List<RoutineSessionExerciseResponse> exercises;
    private final boolean allCompleted;

    public RoutineSessionStateResponse(RoutineSession session) {
        this.routineSessionId = session.getRoutineSessionId();
        this.routineName = session.getRoutineName();
        this.allCompleted = session.isAllExercisesCompleted();

        RoutineSessionExercise current = session.getCurrentExercise();
        this.currentExercise = current != null ? new CurrentExerciseInfo(current) : null;

        this.exercises = session.getExercises().stream()
                .map(RoutineSessionExerciseResponse::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class CurrentExerciseInfo {
        private final String exerciseName;
        private final String emoji;
        private final int targetReps;
        private final int completedSets;
        private final int targetSets;
        private final int restSeconds;
        private final List<String> tips;

        public CurrentExerciseInfo(RoutineSessionExercise e) {
            this.exerciseName = e.getExercise().getName();
            this.emoji = e.getExercise().getEmoji();
            this.targetReps = e.getTargetReps();
            this.completedSets = e.getCompletedSets();
            this.targetSets = e.getTargetSets();
            this.restSeconds = e.getRestSeconds();
            this.tips = e.getExercise().getNotes();
        }
    }
}
