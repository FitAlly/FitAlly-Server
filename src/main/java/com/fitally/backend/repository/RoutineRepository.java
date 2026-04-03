package com.fitally.backend.repository;

import com.fitally.backend.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findByUserId(Long userId);
    List<Routine> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
    List<Routine> findByAiGeneratedTrue();
}
