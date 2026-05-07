package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routine_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RoutineSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routineSessionId;

    private Long userId;

    private String routineName;

    /** 현재 진행 중인 운동 인덱스 */
    private int currentExerciseIndex;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
    private Integer totalSeconds;
    private Integer calories;

    @OneToMany(mappedBy = "routineSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoutineSessionExercise> exercises = new ArrayList<>();

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED
    }

    @Builder
    public RoutineSession(Long userId, String routineName) {
        this.userId = userId;
        this.routineName = routineName;
        this.currentExerciseIndex = 0;
        this.status = SessionStatus.IN_PROGRESS;
    }

    public RoutineSessionExercise getCurrentExercise() {
        return exercises.stream()
                .filter(e -> e.getOrderIndex() == currentExerciseIndex)
                .findFirst()
                .orElse(null);
    }

    public boolean completeSet() {
        RoutineSessionExercise current = getCurrentExercise();
        if (current == null) return false;

        current.completeSet();

        if (current.isAllSetsCompleted()) {
            currentExerciseIndex++;
        }

        return isAllExercisesCompleted();
    }

    public boolean isAllExercisesCompleted() {
        return currentExerciseIndex >= exercises.size();
    }

    public void finish(int totalSeconds) {
        this.completedAt = LocalDateTime.now();
        this.totalSeconds = totalSeconds;
        this.calories = calculateCalories(totalSeconds);
        this.status = SessionStatus.COMPLETED;
    }

    private int calculateCalories(int totalSeconds) {
        double met = 5.0;
        double bodyWeightKg = 70.0;
        double hours = totalSeconds / 3600.0;
        return (int) (met * bodyWeightKg * hours);
    }
}
