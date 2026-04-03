package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routineId;

    /** 임시 userId (JWT 도입 전까지 X-User-Id 헤더로 받음), AI 루틴은 null */
    private Long userId;

    @Column(nullable = false)
    private String name;

    /** AI가 생성한 루틴 여부 */
    @Column(nullable = false)
    private boolean aiGenerated;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoutineWorkout> routineWorkouts = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Routine(Long userId, String name, boolean aiGenerated) {
        this.userId = userId;
        this.name = name;
        this.aiGenerated = aiGenerated;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void clearWorkouts() {
        this.routineWorkouts.clear();
    }
}
