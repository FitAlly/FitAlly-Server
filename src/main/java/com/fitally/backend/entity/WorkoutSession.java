package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    /** 임시 userId (JWT 도입 전까지 X-User-Id 헤더로 받음) */
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private int targetSets;
    private int targetReps;
    private double weightKg;
    private int completedSets;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
    private Integer totalSeconds;
    private Integer calories;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<WorkoutSet> sets = new ArrayList<>();

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED
    }

    @Builder
    public WorkoutSession(Long userId, Exercise exercise, int targetSets, int targetReps, double weightKg) {
        this.userId = userId;
        this.exercise = exercise;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.weightKg = weightKg;
        this.completedSets = 0;
        this.status = SessionStatus.IN_PROGRESS;
    }

    public void completeSet() {
        this.completedSets++;
    }

    public boolean isAllSetsCompleted() {
        return this.completedSets >= this.targetSets;
    }

    public void finish(int totalSeconds) {
        this.completedAt = LocalDateTime.now();
        this.totalSeconds = totalSeconds;
        this.calories = calculateCalories(totalSeconds);
        this.status = SessionStatus.COMPLETED;
    }

    private int calculateCalories(int totalSeconds) {
        // MET 5.0 (중강도 근력 운동) 기준, 체중 70kg 기본값
        double met = 5.0;
        double bodyWeightKg = 70.0;
        double hours = totalSeconds / 3600.0;
        return (int) (met * bodyWeightKg * hours);
    }
}
