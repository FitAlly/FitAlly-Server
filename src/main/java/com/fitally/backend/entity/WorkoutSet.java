package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workout_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    private int setNumber;
    private LocalDateTime completedAt;

    @Builder
    public WorkoutSet(WorkoutSession session, int setNumber) {
        this.session = session;
        this.setNumber = setNumber;
        this.completedAt = LocalDateTime.now();
    }
}
