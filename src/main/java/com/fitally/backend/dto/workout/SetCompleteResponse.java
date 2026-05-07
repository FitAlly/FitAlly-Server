package com.fitally.backend.dto.workout;

import com.fitally.backend.entity.WorkoutSession;
import lombok.Getter;

@Getter
public class SetCompleteResponse {
    private final int completedSets;
    private final int targetSets;
    private final int remainingSets;
    private final boolean allCompleted;
    private final int restSeconds;

    public SetCompleteResponse(WorkoutSession session) {
        this.completedSets = session.getCompletedSets();
        this.targetSets = session.getTargetSets();
        this.remainingSets = session.getTargetSets() - session.getCompletedSets();
        this.allCompleted = session.isAllSetsCompleted();
        this.restSeconds = allCompleted ? 0 : 90;
    }
}
