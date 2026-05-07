package com.fitally.backend.repository;

import com.fitally.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByCategory(String category);
    List<Exercise> findByDifficulty(String difficulty);
    List<Exercise> findByCategoryAndDifficulty(String category, String difficulty);
    List<Exercise> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
}
