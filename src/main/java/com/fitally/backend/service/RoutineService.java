package com.fitally.backend.service;

import com.fitally.backend.dto.routine.RoutineCreateRequest;
import com.fitally.backend.dto.routine.RoutineResponse;
import com.fitally.backend.dto.routine.RoutineWorkoutRequest;
import com.fitally.backend.entity.Exercise;
import com.fitally.backend.entity.Routine;
import com.fitally.backend.entity.RoutineWorkout;
import com.fitally.backend.repository.ExerciseRepository;
import com.fitally.backend.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;

    /** 내 루틴 목록 조회 */
    public List<RoutineResponse> getMyRoutines(Long userId) {
        return routineRepository.findByUserId(userId).stream()
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    /** 루틴 이름 검색 */
    public List<RoutineResponse> searchRoutines(Long userId, String name) {
        return routineRepository.findByUserIdAndNameContainingIgnoreCase(userId, name).stream()
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    /** AI 추천 루틴 목록 조회 */
    public List<RoutineResponse> getAiRoutines() {
        return routineRepository.findByAiGeneratedTrue().stream()
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    /** AI 추천 루틴 랜덤 N개 */
    public List<RoutineResponse> getRandomAiRoutines(int count) {
        List<Routine> all = routineRepository.findByAiGeneratedTrue();
        Collections.shuffle(all);
        return all.stream()
                .limit(count)
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    /** 직접 만들기 루틴 생성 */
    @Transactional
    public RoutineResponse createRoutine(Long userId, RoutineCreateRequest request) {
        Routine routine = Routine.builder()
                .userId(userId)
                .name(request.getName())
                .aiGenerated(false)
                .build();

        addWorkoutsToRoutine(routine, request.getWorkouts());
        return new RoutineResponse(routineRepository.save(routine));
    }

    /** AI 추천 루틴 내 루틴에 추가 */
    @Transactional
    public RoutineResponse saveAiRoutine(Long userId, Long routineId) {
        Routine aiRoutine = getRoutineOrThrow(routineId);

        Routine newRoutine = Routine.builder()
                .userId(userId)
                .name(aiRoutine.getName())
                .aiGenerated(true)
                .build();

        aiRoutine.getRoutineWorkouts().forEach(rw -> {
            RoutineWorkout copied = RoutineWorkout.builder()
                    .routine(newRoutine)
                    .exercise(rw.getExercise())
                    .orderIndex(rw.getOrderIndex())
                    .targetSets(rw.getTargetSets())
                    .targetReps(rw.getTargetReps())
                    .weightKg(rw.getWeightKg())
                    .restSeconds(rw.getRestSeconds())
                    .build();
            newRoutine.getRoutineWorkouts().add(copied);
        });

        return new RoutineResponse(routineRepository.save(newRoutine));
    }

    /** 루틴 수정 */
    @Transactional
    public RoutineResponse updateRoutine(Long routineId, RoutineCreateRequest request) {
        Routine routine = getRoutineOrThrow(routineId);
        routine.updateName(request.getName());
        routine.clearWorkouts();
        addWorkoutsToRoutine(routine, request.getWorkouts());
        return new RoutineResponse(routine);
    }

    /** 루틴 삭제 */
    @Transactional
    public void deleteRoutine(Long routineId) {
        routineRepository.delete(getRoutineOrThrow(routineId));
    }

    private void addWorkoutsToRoutine(Routine routine, List<RoutineWorkoutRequest> workoutRequests) {
        if (workoutRequests == null || workoutRequests.isEmpty()) return;

        IntStream.range(0, workoutRequests.size()).forEach(i -> {
            RoutineWorkoutRequest req = workoutRequests.get(i);
            Exercise exercise = exerciseRepository.findById(req.getExerciseId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운동입니다."));

            RoutineWorkout rw = RoutineWorkout.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(i)
                    .targetSets(req.getTargetSets())
                    .targetReps(req.getTargetReps())
                    .weightKg(req.getWeightKg())
                    .restSeconds(req.getRestSeconds())
                    .build();
            routine.getRoutineWorkouts().add(rw);
        });
    }

    private Routine getRoutineOrThrow(Long routineId) {
        return routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루틴입니다."));
    }
}
