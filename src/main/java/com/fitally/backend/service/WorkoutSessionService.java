package com.fitally.backend.service;

import com.fitally.backend.dto.workout.SetCompleteResponse;
import com.fitally.backend.dto.workout.WorkoutFinishResponse;
import com.fitally.backend.dto.workout.WorkoutStartRequest;
import com.fitally.backend.dto.workout.WorkoutStartResponse;
import com.fitally.backend.entity.Exercise;
import com.fitally.backend.entity.WorkoutSession;
import com.fitally.backend.entity.WorkoutSet;
import com.fitally.backend.repository.ExerciseRepository;
import com.fitally.backend.repository.WorkoutSessionRepository;
import com.fitally.backend.repository.WorkoutSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutSetRepository setRepository;
    private final ExerciseRepository exerciseRepository;

    /** 운동 세션 시작 */
    @Transactional
    public WorkoutStartResponse startSession(Long userId, WorkoutStartRequest request) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운동입니다."));

        WorkoutSession session = WorkoutSession.builder()
                .userId(userId)
                .exercise(exercise)
                .targetSets(request.getTargetSets())
                .targetReps(request.getTargetReps())
                .weightKg(request.getWeightKg())
                .build();

        return new WorkoutStartResponse(sessionRepository.save(session));
    }

    /** 세트 완료 */
    @Transactional
    public SetCompleteResponse completeSet(Long sessionId) {
        WorkoutSession session = getSessionOrThrow(sessionId);

        if (session.getStatus() == WorkoutSession.SessionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 세션입니다.");
        }

        session.completeSet();

        WorkoutSet set = WorkoutSet.builder()
                .session(session)
                .setNumber(session.getCompletedSets())
                .build();
        setRepository.save(set);

        return new SetCompleteResponse(session);
    }

    /** 운동 완료 */
    @Transactional
    public WorkoutFinishResponse finishSession(Long sessionId, int totalSeconds) {
        WorkoutSession session = getSessionOrThrow(sessionId);
        session.finish(totalSeconds);
        return new WorkoutFinishResponse(session);
    }

    private WorkoutSession getSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다."));
    }
}
