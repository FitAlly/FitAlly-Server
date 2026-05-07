package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.exercise.ExerciseDetailResponse;
import com.fitally.backend.dto.exercise.ExerciseListResponse;
import com.fitally.backend.dto.exercise.ExercisePreviewResponse;
import com.fitally.backend.entity.Exercise;
import com.fitally.backend.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private static final int PREVIEW_SIZE = 5;

    private final ExerciseRepository exerciseRepository;

    /** 홈화면용 미리보기 (5개, 이름/카테고리/이모지만) */
    public List<ExercisePreviewResponse> getPreview() {
        return exerciseRepository.findAll().stream()
                .limit(PREVIEW_SIZE)
                .map(ExercisePreviewResponse::new)
                .collect(Collectors.toList());
    }

    /** 전체보기 - keyword 검색 / category·difficulty 필터 (조합 가능) */
    public List<ExerciseListResponse> getExercises(String keyword, String category, String difficulty) {
        List<Exercise> exercises;

        if (keyword != null && !keyword.isBlank()) {
            exercises = exerciseRepository.findByNameContainingIgnoreCase(keyword);
        } else if (category != null && !category.isBlank() && difficulty != null && !difficulty.isBlank()) {
            exercises = exerciseRepository.findByCategoryAndDifficulty(category, difficulty);
        } else if (category != null && !category.isBlank()) {
            exercises = exerciseRepository.findByCategory(category);
        } else if (difficulty != null && !difficulty.isBlank()) {
            exercises = exerciseRepository.findByDifficulty(difficulty);
        } else {
            exercises = exerciseRepository.findAll();
        }

        return exercises.stream()
                .map(ExerciseListResponse::new)
                .collect(Collectors.toList());
    }

    /** 단건 상세 조회 */
    public ExerciseDetailResponse getExercise(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));
        return new ExerciseDetailResponse(exercise);
    }
}
