package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.routinesession.*;
import com.fitally.backend.entity.*;
import com.fitally.backend.repository.RoutineRepository;
import com.fitally.backend.repository.RoutineSessionExerciseRepository;
import com.fitally.backend.repository.RoutineSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineSessionService {

    private final RoutineSessionRepository routineSessionRepository;
    private final RoutineSessionExerciseRepository exerciseRepository;
    private final RoutineRepository routineRepository;

    /** 루틴 시작 */
    @Transactional
    public RoutineSessionStateResponse startSession(Long userId, Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTINE_NOT_FOUND));

        RoutineSession session = RoutineSession.builder()
                .userId(userId)
                .routineName(routine.getName())
                .build();

        routine.getRoutineWorkouts().forEach(rw -> {
            RoutineSessionExercise exercise = RoutineSessionExercise.builder()
                    .routineSession(session)
                    .exercise(rw.getExercise())
                    .orderIndex(rw.getOrderIndex())
                    .targetSets(rw.getTargetSets())
                    .targetReps(rw.getTargetReps())
                    .weightKg(rw.getWeightKg())
                    .restSeconds(rw.getRestSeconds())
                    .build();
            session.getExercises().add(exercise);
        });

        return new RoutineSessionStateResponse(routineSessionRepository.save(session));
    }

    /** 현재 세션 상태 조회 */
    public RoutineSessionStateResponse getSessionState(Long sessionId) {
        return new RoutineSessionStateResponse(getSessionOrThrow(sessionId));
    }

    /** 세트 완료 */
    @Transactional
    public RoutineSetCompleteResponse completeSet(Long sessionId) {
        RoutineSession session = getSessionOrThrow(sessionId);

        if (session.getStatus() == RoutineSession.SessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_COMPLETED);
        }

        RoutineSessionExercise current = session.getCurrentExercise();
        if (current == null) {
            throw new BusinessException(ErrorCode.SESSION_NO_CURRENT_EXERCISE);
        }

        session.completeSet();
        return new RoutineSetCompleteResponse(session, current);
    }

    /** 운동 중 운동 항목 수정 (세트/횟수/휴식) */
    @Transactional
    public RoutineSessionStateResponse updateExercise(Long sessionId, Long routineSessionExerciseId,
                                                      RoutineSessionExerciseUpdateRequest request) {
        RoutineSession session = getSessionOrThrow(sessionId);

        RoutineSessionExercise exercise = session.getExercises().stream()
                .filter(e -> e.getRoutineSessionExerciseId().equals(routineSessionExerciseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTINE_EXERCISE_NOT_FOUND));

        exercise.update(request.getTargetSets(), request.getTargetReps(), request.getRestSeconds());
        return new RoutineSessionStateResponse(session);
    }

    /** 운동 중 운동 항목 삭제 */
    @Transactional
    public RoutineSessionStateResponse deleteExercise(Long sessionId, Long routineSessionExerciseId) {
        RoutineSession session = getSessionOrThrow(sessionId);

        session.getExercises().removeIf(e ->
                e.getRoutineSessionExerciseId().equals(routineSessionExerciseId));

        return new RoutineSessionStateResponse(session);
    }

    /** 루틴 운동 완료 */
    @Transactional
    public RoutineSessionFinishResponse finishSession(Long sessionId, int totalSeconds) {
        RoutineSession session = getSessionOrThrow(sessionId);
        session.finish(totalSeconds);
        return new RoutineSessionFinishResponse(session);
    }

    private RoutineSession getSessionOrThrow(Long sessionId) {
        return routineSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }
}
