package com.fitally.backend.service;

import com.fitally.backend.dto.calendar.response.*;
import com.fitally.backend.entity.RoutineSession;
import com.fitally.backend.entity.WorkoutSession;
import com.fitally.backend.repository.RoutineSessionRepository;
import com.fitally.backend.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final RoutineSessionRepository routineSessionRepository;

    // 월별 운동한 날짜 목록 조회
    public CalendarMonthlyResponse getMonthlyMarkedDates(Long userId, int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<WorkoutSession> workoutSessions = workoutSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);
        List<RoutineSession> routineSessions = routineSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);

        // 운동한 날짜 숫자만 추출 후 중복 제거
        List<Integer> markedDates = new ArrayList<>();

        workoutSessions.stream()
                .map(w -> w.getStartedAt().getDayOfMonth())
                .forEach(markedDates::add);

        routineSessions.stream()
                .map(r -> r.getStartedAt().getDayOfMonth())
                .forEach(markedDates::add);

        List<Integer> distinctDates = markedDates.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new CalendarMonthlyResponse(distinctDates);
    }

    // 특정 날짜 운동 기록 조회
    public CalendarDailyResponse getDailyWorkouts(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<WorkoutSession> workoutSessions = workoutSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);
        List<RoutineSession> routineSessions = routineSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);

        List<CalendarWorkoutItem> workouts = new ArrayList<>();

        // 단일 운동 변환
        for (WorkoutSession w : workoutSessions) {
            workouts.add(CalendarWorkoutItem.ofSingle(
                    w.getExercise().getName(),
                    w.getTargetSets(),
                    w.getTargetReps()
            ));
        }

        // 루틴 운동 변환
        for (RoutineSession r : routineSessions) {
            List<CalendarExerciseItem> exercises = r.getExercises().stream()
                    .map(e -> new CalendarExerciseItem(
                            e.getExercise().getName(),
                            e.getTargetSets(),
                            e.getTargetReps()
                    ))
                    .collect(Collectors.toList());

            workouts.add(CalendarWorkoutItem.ofRoutine(r.getRoutineName(), exercises));
        }

        // 총 운동 종목 수 계산
        int totalExerciseCount = workoutSessions.size()
                + routineSessions.stream().mapToInt(r -> r.getExercises().size()).sum();

        return new CalendarDailyResponse(date.toString(), totalExerciseCount, workouts);
    }

    // 이번 주 요약 조회
    public WeeklySummaryResponse getWeeklySummary(Long userId) {
        // 이번 주 월요일 ~ 일요일 계산
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = sunday.plusDays(1).atStartOfDay();

        List<WorkoutSession> workoutSessions = workoutSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);
        List<RoutineSession> routineSessions = routineSessionRepository.findCompletedByUserIdAndDateRange(userId, start, end);

        // 완료 횟수 = 단일 운동 세션 수 + 루틴 세션 수
        int completedCount = workoutSessions.size() + routineSessions.size();

        // 총 운동 시간 합산
        int totalSeconds = workoutSessions.stream().mapToInt(w -> w.getTotalSeconds() != null ? w.getTotalSeconds() : 0).sum()
                + routineSessions.stream().mapToInt(r -> r.getTotalSeconds() != null ? r.getTotalSeconds() : 0).sum();

        // 소모 칼로리 합산
        int totalCalories = workoutSessions.stream().mapToInt(w -> w.getCalories() != null ? w.getCalories() : 0).sum()
                + routineSessions.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();

        // 운동 종목 수 = 단일 운동 수 + 루틴 내 전체 종목 수
        int exerciseCount = workoutSessions.size()
                + routineSessions.stream().mapToInt(r -> r.getExercises().size()).sum();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new WeeklySummaryResponse(
                monday.format(formatter),
                sunday.format(formatter),
                completedCount,
                totalSeconds,
                totalCalories,
                exerciseCount
        );
    }
}
